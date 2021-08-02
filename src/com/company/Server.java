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
}
}

