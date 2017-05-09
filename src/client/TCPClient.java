package client;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.*;

import server.TCPServer.ServerSocketThread;


class TCPClient{
	public static void main(String argv[]) throws Exception{
		String serverResponse; // String to hold response from server
		Socket clientSocket = new Socket("localhost", 4279); // Establishing connection to server
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream()); // Creating a DataOutputStream to send requests to the server
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // Creating a BufferedReader  to receive responses from the server
		
		for(int i = 1; i <= 5; i++){ // For loop for sending command "1" and receiving Fibonacci number
			outToServer.writeBytes("1\n"); // Send command "1" to the server
			serverResponse = inFromServer.readLine(); // Receiving response from server
			System.out.println("Fibonacci Number from Server: " + serverResponse); // Printing response from server
	    }
		for(int i = 1; i <= 5; i++){ // For loop for sending command "2" to receive a random number that is larger than the previous one
			outToServer.writeBytes("2\n"); // Send command "2" to the server
			serverResponse = inFromServer.readLine(); // Receiving response from server
			System.out.println("Larger random number from Server: " + serverResponse); // Printing response from server
	    }
		for(int i = 1; i <= 5; i++){ // For loop for sending command "3" to receive the next prime number
			outToServer.writeBytes("3\n"); // Send command "3" to the server
			serverResponse = inFromServer.readLine(); // Receiving response from server
			System.out.println("Prime Number from Server: " + serverResponse); // Printing response from server
	    }
		clientSocket.close(); //Close client connection with the server

	}
	class localThr extends Thread {
		public localThr(){
			
		}
		
		public void run(){
			
		}
	}
	class uThr extends Thread {
		private runtimeThr runtime;
		public uThr(){
			runtime = new runtimeThr();
		}
		public void run(){
			for(int i = 0; i < 20; i++){
				Random rand = new Random();
				String command = "" + (rand.nextInt(5) + 1);
				runtime.requestQueue.add(command);
			}
			for(int i = 0; i < 20; i++){
				System.out.println(runtime.returnQueue);
			}
		}
	}
	class runtimeThr extends Thread {
		private ConcurrentLinkedQueue<String> requestQueue;
		private ConcurrentLinkedQueue<String> returnQueue;
		public runtimeThr(){
			requestQueue = new ConcurrentLinkedQueue<>();
			returnQueue = new ConcurrentLinkedQueue<>();
		}
		public void run(){
			
		}
	}

	class networkThr extends Thread {
		public networkThr(){
			
		}
		public void run(){
			
		}
	}
}
