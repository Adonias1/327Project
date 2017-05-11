package client;

/*
 * Hector De La Vega, Henna Gohil, Gary Nunez, Adonias Lopez
 * CECS 327 Assignment #6 - TCP Server-Client connection
 * Professor Ratana Ngo
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author hector, adonias, gary, henna. This class will contain the components
 * of the client. The components consist of the Runtime thread, the upper 
 * threads, the local thread, and the network thread. Uthreads will communicate
 * with runtime threads to queue requests for information and unqueue 
 * responses. The runtime thread will deploy requests by spawning either local
 * or network threads and then unqueueing the requests. The spawned local 
 * or network thread will execute the request, update response and then
 * queue the response. A data class will contain the request type and all
 * relevant information to identify the requesting uthread. Each uthread will
 * send 20 requests and wait for their response on each iteration.
 *
 */
class TCPClient{
	/**
	 * Runtime thread object. This is declared as global in order to interract
	 * with the various levels of the client.
	 */
	public static runtimeThr runtime = new runtimeThr();
	/**
	 * These are the uThreads that will determine the data request type. The
	 * data request type will be nextEven, nextOdd, nextLargeRand, nextFibb,
	 * nextPrime.
	 */
	static ArrayList<uThr> upperThreads = new ArrayList<>();
	/**
	 * This odd variable will keep track of the current odd value for the get
	 * odd local thread request.
	 */
	private static int odd = -1;
	/**
	 * This even variable will keep track of the current even value for the get
	 * even local thread request.
	 */
	private static int even = 0;
	
	/**
	 * The main function will start the runtime thread and initialize 8
	 * upper threads, storing each within the ArrayList of upper threads. 
	 * The upper threads will be immediately started after their initialization.
	 * @param argv - default string array
	 * @throws Exception - thread runtime exceptions.
	 */
	public static void main(String argv[]) throws Exception{
		runtime.start();
		for(int i = 0; i < 8; i++){
			upperThreads.add(new uThr(i));
			upperThreads.get(i).start();
		}
	}
	
	/**
	 * @author hector, adonias, gary, henna.
	 * The local thread will execute requests for next even and next odd 
	 * numbers. Responses are going to be saved within the response attribute
	 * of the data object. Furthermore, the current running value of the even
	 * and odd sequences will be updated on a global level. Local threads will
	 * only be spawned when they are requested. After updating the data object,
	 * the updated data object will be stored within the ReturnQueue of the
	 * runtime thread. Locks will be used to make sure that no two local threads
	 * will access the modifiers to nextEven and nextOdd at the same time.
	 */
	static class localThr extends Thread {
		private String command;
		private Data data;
		private ReentrantLock lock;
		private Condition q;
		public localThr(Data d){
			data = d;
			command = d.command;
			lock = new ReentrantLock();
			q = lock.newCondition();
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		public void run(){
			lock.lock();
			try{
				switch(command){
				case "1":
					data.respond = "Next Even: "+ nextEven();
					runtime.returnQueue.add(data);
					break;
				case "2":
					data.respond = "Next Odd: "+ nextOdd();
					runtime.returnQueue.add(data);
					break;
				}
				//signal all will be used to try and
				//awaken any other local threads
				//that are currently locked and waiting.
				q.signalAll();
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				lock.unlock();
			}
		}
		public int nextEven(){
			return even += 2;
		}
		public int nextOdd(){
			return odd += 2;
		}
		
	}
	/**
	 * @author hector, adonias, gary, henna - uThr will be the upper threads 
	 * of the client. Here the request type for information will be determined
	 * through the use of random number generation. The possible request types:
	 * nextEven, nextOdd, nextFib, nextLargeRand, nextPrime will be numbered
	 * 1-5. Once the request type is set, it will be stored as a string named
	 * "command" in the Data object. The data object with the command will then
	 * be added to the RequestQueue of the runtime thread. The uThr will then
	 * enter a spin lock until its requested data object is at the head of the
	 * responseQueue in the runtime thread. Once the response is found, it 
	 * will print out the result and loop back to its next iteration.
	 *
	 */
	static class uThr extends Thread {
		private long threadID;
		private Random rand;
		public uThr(int tID){
			threadID = tID;
			rand = new Random();
		}
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		public void run(){
			for(int i = 0; i < 20; i++){
				Data data = new Data(threadID, "");
				int c = (rand.nextInt(5) + 1);
				switch(c){
				case 1:
					data.command = "" + 1;
					break;
				case 2:
					data.command = "" + 2;
					break;
				case 3:
					data.command = "" + 3;
					break;
				case 4:
					data.command = "" + 4;
					break;
				case 5:
					data.command = "" + 5;
					break;
				}
				runtime.requestQueue.add(data);
				
				while(runtime.returnQueue.peek() == null || 
						threadID != runtime.returnQueue.peek().threadID){
					try {
						sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				data = runtime.getResponse();
				System.out.println("Thread: "+ data.threadID + " Response: " + data.respond);
		
			}
			System.out.println(threadID +" is done");
		}
	}
	/**
	 * @author hector, adonias, gary, henna - The runtime thread will serve as 
	 * the connection between the upper threads and the local and network 
	 * threads. The ConcurrentLinkedQueue requestQueue and returnQueue will
	 * be stored here. These Queues will store data objects that specify the 
	 * requesting thread, the requested command, and the response. The 
	 * runtime thread will execute an infinite loop, checking for new data
	 * object requests and spawning either a network thread, or a local thread
	 * depending on the request type. Once spawned, the request will be 
	 * considered as processing and will be de-queued from the Queue.
	 *
	 */
	public static class runtimeThr extends Thread {
		//The queue storing submitted requests.
		public ConcurrentLinkedQueue<Data> requestQueue;
		//The queue storing process requests and their responses.
		public ConcurrentLinkedQueue<Data> returnQueue;
		public runtimeThr(){
			requestQueue = new ConcurrentLinkedQueue<>();
			returnQueue = new ConcurrentLinkedQueue<>();
		}
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		public void run(){
			while(true){
				if(requestQueue.peek() != null){
					String command = requestQueue.peek().command;
					if(command == "1" || command == "2"){
						localThr local = new localThr(requestQueue.peek());
						requestQueue.poll();
						local.start();
					}
					else if(command == "3" || command == "4" 
							|| command == "5"){
						networkThr net = new networkThr(requestQueue.peek());
						requestQueue.poll();
						net.start();
					}
				}
			}
		}
		/**
		 * This function will be used by upper threads to add their data 
		 * requests to the runtime thread's ConcurrentQueueList.
		 * @param command - The data object containing the information request
		 * and information of the requesting upper thread.
		 */
		public void add(Data command){
			requestQueue.add(command);
		}
		
		/**
		 * This function will be used by upper threads to retrieve their 
		 * requests from the runtime thread's ConcurrentQueueList responseQueue.
		 * @param command - The data object containing the updated response
		 * to the initial information request.
		 */
		public Data getResponse(){
			return returnQueue.poll();
		}
	}

	/**
	 * @author hector, adonias, henna, gary - This network thread class will
	 * contain the components necessary to communicate with the server and
	 * request server information. Runtime threads will spawn network threads
	 * to process requests for server data. Responses will be saved to the
	 * initial data object request, and then stored back into the responseQueue
	 * in the runtime thread. There upper threads will retrieve the responses
	 * when they are at the head of the queue.
	 *
	 */
	static class networkThr extends Thread {
		private Data data;
		private String command;
		//lock and condition used to preserve data integrity among multiple
		//network threads.
		private ReentrantLock lock;
		private Condition q;
		public networkThr(Data d){
			data = d;
			command = d.command;
			lock = new ReentrantLock();
			q = lock.newCondition();
		}
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		public void run(){
			lock.lock();
			String serverResponse; // String to hold response from server
			try {
				Socket clientSocket = new Socket("localhost", 4279); // Establishing connection to server
				DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // Creating a BufferedReader  to receive responses from the server
				outToServer.writeBytes(command + "\n"); // Send command "1" to the server
				serverResponse = inFromServer.readLine(); // Receiving response from server
				
				// TODO send the response back to uThr 
				
				data.respond = serverResponse;
				runtime.returnQueue.add(data);
				
				clientSocket.close(); //Close client connection with the server
				//signal all will be used to try and
				//awaken any other network threads
				//that are currently locked and waiting.
				q.signalAll();
			} catch (IOException e) {
				e.printStackTrace();
			} // Creating a DataOutputStream to send requests to the server
			finally{
				lock.unlock();
			}
			
		}
	}
}