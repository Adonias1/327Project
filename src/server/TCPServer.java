package server;

/*
 * Hector De La Vega, Henna Gohil, Gary Nunez, Adonias Lopez
 * CECS 327 Assignment #7 - TCP Socket Assignment
 * Professor Ratana Ngo
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * @author Hector, Adonias, Henna, Gary, This class will contain the 
 * functionalities of the TCP Server. The server connection will be established
 * through port 4279, and each new Socket connection to clients will be added
 * to an arraylist of existing clients. The socket client interactions are 
 * carried out through the use of the ServerSocketThread nested-class. The Server
 * is Stateful and concurrent through the use of shared memory variables, 
 * threads, and synchronized functions. Sockets with ended connections will
 * remove themselves from the ArrayList of connections. 
 *
 */
public class TCPServer {
	//Server Socket
	private static ServerSocket serverSocket;
	//primeNumber keeps track of the current largest primeNumber
	private static int primeNumber = 1;
	//fibNumber keeps track of server current fibNumber
	private static int fibNumber = 1;
	//prevFibNum keeps track of the previous fibNumber
	private static int prevFibNum = -1;
	//prevRandNum keeps track of the previous largest Random Number
	private static int prevRandNum = 0;
	//LargeRandomNum keeps track of the current largest Number
	private static int LargeRandNum = 0;
	
	//ArrayList will contain a collection of client SocketThread connections.
	private static ArrayList<ServerSocketThread> socketList = new ArrayList<ServerSocketThread>();
	
	
	
	/**
	 * In this function the server is established and set in a loop listening
	 * for and accepting new client connections. Everytime a new client connection
	 * is established, a ServerSocketThread object is created storing the 
	 * connection details between the server and the client. These objects stored
	 * in the socketList ArrayList will utilize the functions of the outer 
	 * class to manipulate and work with the private Stateful data members of 
	 * the server.
	 * @param argv
	 * @throws Exception
	 */
	public static void main(String argv[]) throws Exception {
		
		//try-catch for establishing server and creating connection threads.
		try{
			//new server
			serverSocket = new ServerSocket(4279);
			//loop listening to new connections.
			while(true){
				//create accept socket connection and spawn and store thread.
				System.out.println("Waiting...");
				socketList.add(new ServerSocketThread(serverSocket.accept()));
				System.out.println("Connected: ");
				socketList.get(socketList.size()-1).start();
			}	
		}catch(Exception E){
			E.printStackTrace();
		}
	}
	
	/**
	 * This function is used by the server socket threads to work with the
	 * Stateful information on the server. This function is synchronized
	 * to prevent threads from ruining data and preserve correct concurrency.
	 * The next fib number is generated relative to the current fib number
	 * stored on server.
	 * @return - the next fib number
	 */
	public synchronized static int getNextFib(){
		try{
			int nextFibNumber = 0;	
			do{
				//calculate next fib number using fib sequence formula.
				nextFibNumber = fibNumber + prevFibNum;
				//override the current and prev fib numbers
				prevFibNum = fibNumber;
				fibNumber = nextFibNumber;
				//keep doing this until a fib number that is even is found.
			}while(nextFibNumber % 2 != 0);
		}
		catch(Exception e){
			//If we produce a fib number that is too large, reset values.
			fibNumber = 1;
			prevFibNum = -1;
		}
		//return the new even fib number
		return fibNumber;
		
	}
	
	/**
	 * This function will be utilized to return the next largest random number
	 * Relevant to the current largest random number. The function is synchronized
	 * to preserve data correctness.
	 * @return - return next largest random number
	 */
	public synchronized static int nextLargetRand(){
		// nextLargerRand: Computes a random integer that is larger than the previous one 
		// (wrap around when max is reached) and sends it to the client.
		Random rndNum = new Random();
		try{
			//generate the next largest number between 0 and 500 and add
			//to prev largest.
			LargeRandNum = rndNum.nextInt(500) + prevRandNum;
			prevRandNum = LargeRandNum;
		}
		catch(Exception e){ //If the sum of the previous random number and larger random number is over the range of integer, restart them.
			System.out.println("Maximum Number Reached");
			//if values are too large, reset values.
			LargeRandNum = rndNum.nextInt(500);
			prevRandNum = LargeRandNum;
		}
		//return next largest number.
		return LargeRandNum;	
	}
	
	/**
	 * This function is used to generate and return the next Prime number
	 * relative to the current prime number. The function is synchronized to
	 * Preserve data correctness.
	 * @return - the next prime number
	 */
	public synchronized static int nextPrimeNumb(){
		try{
			boolean isPrime = false;

		    int start = 2; // start at 2 and omit your if statement
		    if(primeNumber ==1){
		    	primeNumber++;
		    	return primeNumber;
		    }
		    while (!isPrime) {
		        // always increment n at the beginning to check a new number
		        primeNumber += 1;
		        // redefine max boundary here
		        int m = (int) Math.ceil(Math.sqrt(primeNumber));

		        isPrime = true;
		        // increment i by 1, not 2 (you're skipping numbers...)
		        for (int i = start; i <= m; i++) {
		            if (primeNumber % i == 0) {
		                isPrime = false;
		                break;
		            } 
		        }
		        // you don't need your "if (!isPrime)..." because you always increment
		    }
		    return primeNumber;
		}
		catch(Exception e){
			primeNumber = 1;
		}
		return primeNumber;
	}
	
	/**
	 * @author Hector, Gary, Henna, Adonias
	 * This sub-class will possess the threads that utilize spawned socket
	 * connections to interact between the Stateful and Concurrent Server with
	 * the Clients. The class stores all relevant attributes necessary for 
	 * a client Socket connection. The class is implemented as a nested-class
	 * so that the threads have access to the synchronized functions and server
	 * features.
	 *
	 */
	public static class ServerSocketThread extends Thread {
		private Socket sock;
		private DataOutputStream out;
		private BufferedReader in;
		
		/**
		 * Constructor for the sub-class ServerSocketThread. New objects
		 * are created using a socket connection that is obtained when the 
		 * ServerSocket accepts a new connection.
		 * @param sock - The socket connection with the client.
		 */
		public ServerSocketThread(Socket sock){
			this.sock = sock;
			try {
				//Declare & Initialize BufferedReader and PrintWriter associated
				//with connection socket.
				this.in = new BufferedReader(new InputStreamReader(this.sock.getInputStream()));
				this.out = new DataOutputStream(this.sock.getOutputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Something went wrong");
			}
			
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		public void run(){
			try {
				String userInput;
				
				//Keeps the client socket running until termined by the client.
				while(true){
					userInput = in.readLine();
					
					//nextEvenFib
					if(userInput.compareTo("1") == 0){
						int fib = getNextFib();
						out.writeBytes(fib + "\n");
					//nextLargerRand
					}else if(userInput.compareTo("2") == 0){
						int nxtLRand = nextLargetRand();
						out.writeBytes(nxtLRand + "\n");
					//nextPrime
					}else if(userInput.compareTo("3") == 0){
						out.writeBytes("Prime: " + nextPrimeNumb() + "\n");
					}else{
						out.writeBytes("Unknown Command\n");
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch(NullPointerException e){
				System.out.println("Client Connection Closed");
			}
			finally{
				//The ServerSocketThread object removes itself from the list
				//when the connection is closed.
				socketList.remove(this);
			}
		}
	}
}
