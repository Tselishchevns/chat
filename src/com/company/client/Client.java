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
}
