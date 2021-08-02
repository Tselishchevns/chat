package com.company.client;

import com.company.Connection;
import com.company.ConsoleHelper;
import com.company.Message;
import com.company.MessageType;

import java.io.IOException;

public class Client {
protected Connection connection;
private volatile boolean clientConnected = false;

protected String getServerAddress(){
   String address = " ";
   ConsoleHelper.writeMessage("Введите адрес сервера");
   address = ConsoleHelper.readString();
   return address;
}
protected int getServerPort(){
   ConsoleHelper.writeMessage("Введите номер порта");
   return ConsoleHelper.readInt();
}
protected String getUserName(){
   ConsoleHelper.writeMessage("Введите имя");
   return ConsoleHelper.readString();
}

public class SocketThread extends Thread{
   protected void processIncomingMessage(String message){
      ConsoleHelper.writeMessage(message);
   }
   protected void informAboutAddingNewUser(String userName){
      ConsoleHelper.writeMessage("К чату подклчился " + userName);
   }
   protected void informAboutDeletingNewUser(String userName){
      ConsoleHelper.writeMessage(userName + " вышел из чата.");
   }
   protected void notifyConnectionStatusChanged(boolean clientConnected){
      Client.this.clientConnected = clientConnected;
      synchronized (Client.this){
         Client.this.notify();
      }
   }

}

protected SocketThread getSocketThread(){
   return new SocketThread();
}
protected void sendTextMessage(String text){
   try {
      connection.send(new Message(MessageType.TEXT, text));
   } catch (IOException ioException) {
      ConsoleHelper.writeMessage("Не удалось отправить сообщение.");
      clientConnected = false;
   }
}
protected boolean shouldSendTextFromConsole() {
   return true;
}
public void run(){
   SocketThread socketThread = getSocketThread();
   socketThread.setDaemon(true);
   socketThread.start();
   try {
      synchronized (this){
         wait();
      }
   }catch (InterruptedException exception) {
      ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
      return;
   }
   if (clientConnected){
      ConsoleHelper.writeMessage("Соединение установленно. Для выхода нажмите 'exit' .");
   }else {
      ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента");
   }
   while (clientConnected){
      String text = ConsoleHelper.readString();
      if (text.equals("exit")){
         break;
      }
      if (shouldSendTextFromConsole())
         sendTextMessage(text);
   }
}

public static void main(String[] args){
   Client client = new Client();
   client.run();
}
}
