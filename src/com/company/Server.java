package com.company;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

public static void sendBroadcastMessage(Message message){ //отправляем всем сообщение
   for (Connection connection: connectionMap.values()){
      try {
         connection.send(message);
      } catch (IOException ioException) {
         ConsoleHelper.writeMessage("Ошибка при отправке сообщения");
      }
   }
}

public static void main(String[] args){

   ConsoleHelper.writeMessage("Введите номер порта");
   int portAddress = ConsoleHelper.readInt();
   try (ServerSocket serverSocket = new ServerSocket(portAddress)) {
      ConsoleHelper.writeMessage("Сервер запущен");
      while (true){
         // Ожидаем входящее соединение и запускаем отдельный поток при его принятии
         Socket socket = serverSocket.accept();
         new Handler(socket).start();
      }
   } catch (Exception ex) {
      ConsoleHelper.writeMessage("Произошла ошибка при запуске или работе сервера.");
   }

}

private static class Handler extends Thread{
   private Socket socket;

   public Handler(Socket socket){
      this.socket = socket;
   }

   private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException{
      while (true){
         connection.send(new Message(MessageType.NAME_REQUEST));
         Message message = connection.receive();

         if (message.getType() != MessageType.USER_NAME){
            ConsoleHelper.writeMessage("Получено сообщение от " + socket.getRemoteSocketAddress() + ". Тип сообщения " +
                                          "не соответсвует протоколу.");
            continue;
         }
         String userName = message.getData();

         if (userName.isEmpty()){
            ConsoleHelper.writeMessage("Попытка подключения к серверу с пустым именем от " + socket.getRemoteSocketAddress());
            continue;
         }
         if (connectionMap.containsKey(userName)){
            ConsoleHelper.writeMessage("Попытка подключения к серверу с уже использованным именем от " + socket.getRemoteSocketAddress());
            continue;
         }
         connectionMap.put(userName, connection);

         connection.send(new Message(MessageType.NAME_ACCEPTED));
         return userName;
      }
   }
   private void notifyUsers(Connection connection, String userName) throws IOException{
      for (String name:connectionMap.keySet()){
         if (name.equals(userName)){
            continue;
         }
         connection.send(new Message(MessageType.USER_ADDED, name));
      }
   }
   private void serverMainLoop (Connection connection, String userName) throws IOException, ClassNotFoundException{
      while (true){
         Message message = connection.receive();
         if (message.getType() == MessageType.TEXT){
            String data = message.getData();
            sendBroadcastMessage(new Message(MessageType.TEXT, userName + ": " + data));
         }else {
            ConsoleHelper.writeMessage("Получено сообщение от " + socket.getRemoteSocketAddress() + ". Тип сообщения не соответствует формату.");
         }
      }
   }

   @Override
   public void run(){
      ConsoleHelper.writeMessage("Установленно новое соединение с " + socket.getRemoteSocketAddress());
      String userName = null;
      try(Connection connection = new Connection(socket)) {
         userName = serverHandshake(connection);
         sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName)); //сообщение всем об подключение нового пользователя
         notifyUsers(connection, userName);
         serverMainLoop(connection, userName);


      } catch (IOException | ClassNotFoundException e){
         ConsoleHelper.writeMessage("Ошибка при обмене данных");
      }
      if (userName != null){
         connectionMap.remove(userName);
         sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
      }
      ConsoleHelper.writeMessage("Соединение с " + socket.getRemoteSocketAddress() + " закрыто.");

   }
}
}

