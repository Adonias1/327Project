package client;

/*
 * Hector De La Vega, Henna Gohil, Gary Nunez, Adonias Lopez
 * CECS 327 Assignment #6 - TCP Server-Client connection
 * Professor Ratana Ngo
 */

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author hector, adonias, gary, henna - This class will detail the attributes
 * of each data object. Data objects will be used to store information regarding
 * upper threads and their requests for server or local services. The data 
 * objects will contain a thread id which will identify which upper thread 
 * put in the data request. The data object will also contain information on 
 * the request type / command, and the response obtained from the server.
 *
 */
class Data{
	/**
	 * Unique identifying number that will distinguish which upper thread put
	 * in this request.
	 */
	public long threadID; 
	/**
	 * This string will identify the command / request for information by the
	 * the upper thread (either nextEven, nextOdd, nextFib, nextLargeRand, and
	 * nextPrime.
	 */
	public String command;
	/**
	 * This string will contain the response that was received for the upper
	 * thread's request for data.
	 */
	public String respond;
	
	/**
	 * Constructor for data objects. Takes in the long thread ID, and request
	 * command from the upper threads.
	 * @param t
	 * @param c
	 */
	public Data(long t, String c){
		this.threadID = t;
		this.command = c;
	}
	
}
