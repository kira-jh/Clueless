/*
The Server class is a Thread that accepts or rejects connection requests, assigns sockets, and starts ServerWorker
Threads for each accepted connection. It also acts as the "go-between" between the GameController Instance and the
ServerWorker Threads.

This implementation is borrowed from a tutorial at https://fullstackmastery.com/ep4 written by Jim Liao. It was
adapted for use in this system by Andrew Johnson.
 */
package edu.jhu.teamundecided.clueless.serverApp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server extends Thread
{

   private final int _serverPort;
   private GameController _gc; // TODO make into a singleton class
   private int _numConnections = 0;

   private ArrayList<ServerWorker> _workerList = new ArrayList<>();


   public Server(int serverPort)
   {

      _serverPort = serverPort;
      _gc = new GameController(this);
   }


   public ArrayList<ServerWorker> getWorkerList()
   {

      return _workerList;
   }


   @Override
   public void run()
   {

      try
      {
         ServerSocket serverSocket = new ServerSocket(_serverPort);
         while (true)
         {
            System.out.println("About to accept client connection...");

            if (_numConnections < 6)
            {
               Socket clientSocket = serverSocket.accept();
               System.out.println("Accepted connection from " + clientSocket);
               ServerWorker worker = new ServerWorker(this, clientSocket);
               addWorker(worker);
            } else
            {
               // connection rejected procedures
            }

         }
      } catch (IOException e)
      {
         e.printStackTrace();
      }
   }


   public void removeWorker(ServerWorker serverWorker)
   {
      _workerList.remove(serverWorker);
      _numConnections--;
   }


   public void addWorker(ServerWorker serverWorker)
   {
      _workerList.add(serverWorker);
      _numConnections++;
      serverWorker.start();
   }


   public void broadcast(String msg)
   {

      StringBuilder msgToSend = new StringBuilder();

      msgToSend.append("msg ");
      msgToSend.append("system ");
      msgToSend.append(msg);
      msgToSend.append("\n");

      for (ServerWorker worker : _workerList)
      {
         try
         {
            worker.send(msgToSend.toString());
         } catch (IOException e)
         {
            e.printStackTrace();
         }
      }
   }


   public void relayMsg(String userName, String msgToSend)
   {

      for (ServerWorker worker : _workerList)
      {
         if (worker.getUserName().equalsIgnoreCase(userName))
         {
            try
            {
               worker.send(msgToSend);
            } catch (IOException e)
            {
               e.printStackTrace();
            }
         }
      }
   }

   public void requestGameStart() throws InterruptedException, IOException
   {
      _gc.startGame();
   }


}
