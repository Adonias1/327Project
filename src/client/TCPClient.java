package client;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import server.TCPServer.ServerSocketThread;


class Data{
	public Lock lock;
	public Condition q;
	public long threadID; 
	public String command;
	public int respond = 0;
	public Data(Lock l, Condition q, long t, String c){
		this.lock = l;
		this.q = q;
		this.threadID = t;
		this.command = c;
	}
}

class TCPClient{
	public static runtimeThr runtime = new runtimeThr();
	static ArrayList<uThr> upperThreads = new ArrayList<>();
	private static int odd = 1;
	private static int even = 0;
	public static void main(String argv[]) throws Exception{
		runtime.start();
		for(int i = 0; i < 1; i++){
			upperThreads.add(new uThr());
			upperThreads.get(i).start();
		}
	}
	static class localThr extends Thread {
		private String command;
		private Data data;
		public localThr(Data d){
			data = d;
			command = d.command;
		}
		
		public void run(){
			switch(command){
			case"1":
				data.lock.lock();
				try{
					data.respond = nextEven();
					runtime.returnQueue.add(data);
				}finally{
					data.lock.unlock();
				}
				break;
			case"2":
				data.respond = nextOdd();
				runtime.returnQueue.add(data);
				break;
			}
		}
		public int nextEven(){
			return even += 2;
		}
		public int nextOdd(){
			return odd += 2;
		}
		
	}
	static class uThr extends Thread {
		public Lock lock;
		public Condition q;
		private long threadID;
		public uThr(){
			threadID = this.getThreadId();
			lock = new ReentrantLock();
			q = lock.newCondition();
		}
		public void run(){
			for(int i = 0; i < 1; i++){
				Data data = new Data(lock, q, threadID, "");
				Random rand = new Random();
				String command =  "" + (rand.nextInt(5) + 1);
				//System.out.println(command);
				data.command = "1";
				runtime.requestQueue.add(data);
				lock.lock();
				try {
					q.await();
					Data dataResponse = runtime.getResponse();
					System.out.println("Thread: "+ data.threadID + " Respond: " + dataResponse.respond);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					lock.unlock();
				}
			}
		}
		public long getThreadId(){
			return this.threadID;
		}
	}
	public static class runtimeThr extends Thread {
		public ConcurrentLinkedQueue<Data> requestQueue;
		public ConcurrentLinkedQueue<Data> returnQueue;
		public runtimeThr(){
			requestQueue = new ConcurrentLinkedQueue<>();
			returnQueue = new ConcurrentLinkedQueue<>();
		}
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
						net.start();
					}
				}
				if(returnQueue.peek() != null || returnQueue.size() != 0){
					returnQueue.peek().lock.lock();
					try{
						returnQueue.peek().q.signal();
					}finally{
						returnQueue.peek().lock.unlock();
					}
				}
			}
		}
		public void add(Data command){
			requestQueue.add(command);
		}
		public Data getResponse(){
			return returnQueue.poll();
		}
	}

	static class networkThr extends Thread {
		private Data data;
		private String command;
		public networkThr(Data d){
			data = d;
			command = d.command;
		}
		public void run(){
			String serverResponse; // String to hold response from server
			try {
				Socket clientSocket = new Socket("localhost", 4279); // Establishing connection to server
				DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // Creating a BufferedReader  to receive responses from the server
				outToServer.writeBytes(command + "\n"); // Send command "1" to the server
				serverResponse = inFromServer.readLine(); // Receiving response from server
				
				// TODO send the response back to uThr 
				
				data.respond = Integer.parseInt(serverResponse);
				
				clientSocket.close(); //Close client connection with the server
			} catch (IOException e) {
				e.printStackTrace();
			} // Creating a DataOutputStream to send requests to the server
			
		}
	}
}
