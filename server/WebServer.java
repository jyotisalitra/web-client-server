/**
 * CSE 5344 – Project 1
 * @author JYOTI SALITRA
 * Spring 2014
 */

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

//WebServer class starting serverSocket and listens client's request
//WebServer class implements Runnable interface and override it's public void run() method.

//Reference: Thread Tutorial from http://docs.oracle.com/javase/tutorial/essential/concurrency/runthread.html
//Reference: Socket Communications from http://www.oracle.com/technetwork/java/socket-140484.html
public class WebServer implements Runnable {

	private ServerSocket serverSocket; //Reference to serverSocket where server will be started
	private String serverHost; //hostname or IP address of the server where server has to start
	private int serverPort; //port where server has to start
	
	//Default host and port values for the serverSocket
	private final String DEFAULT_HOST = "localhost";
	private final int DEFAULT_PORT = 8080;
	
	//Default constructor if no port is passed
	public WebServer ()
	{
		this.serverHost = DEFAULT_HOST; //hostname of the server
		this.serverPort = DEFAULT_PORT; //default port 8080
	}
	
	
	//Parameterized constructor if a port and serverHost are passed
	public WebServer (String sHost, int port)
	{
		this.serverHost = sHost; //hostname of the server
		this.serverPort = port; //default port 8080
	}
	
	
	//Parameterized constructor if a port is passed
	public WebServer (int port)
	{
		this.serverHost = DEFAULT_HOST; //hostname of the server
		this.serverPort = port; //port passed by the ServerInitializer
	}

	
	@Override
	public void run() {
		
		try {

			//get inet address of the host
			InetAddress serverInet = InetAddress.getByName(serverHost);
			
			
			//now using serverInet address and serverPort, initialize serverSocket
			//using a default backlog value which depends on the implementation
			serverSocket = new ServerSocket(serverPort, 0, serverInet);

			System.out.println("[SERVER]> SERVER started at host: " + serverSocket.getInetAddress() + " port: " + serverSocket.getLocalPort() + "\n");
			
			//provide each client an ID, starting with zero
			int clientID=0;
			
			//multithreaded server
			while(true){
				
				//wait for a client to get connected
				Socket clientSocket = serverSocket.accept();
				
				//a new client has connected to this server
				System.out.println("[SERVER - CLIENT"+clientID+"]> Connection established with the client at " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
				
				//pass clientSocket and clientID to RequestHandler object
				RequestHandler rh = new RequestHandler(clientSocket, clientID);
				
				//handover processing for the newly connected client to RequestHandler in a separate thread
				new Thread(rh).start();
				
				//increment clientID for the next client;
				clientID++;
			}
			
		} catch (UnknownHostException e) {
			System.err.println("[SERVER]> UnknownHostException for the hostname: " + serverHost);
		} catch (IllegalArgumentException iae) {
			System.err.println("[SERVER]> EXCEPTION in starting the SERVER: " + iae.getMessage());
		}
		catch (IOException e) {
			System.err.println("[SERVER]> EXCEPTION in starting the SERVER: " + e.getMessage());
		}
		finally {
				try {
					if(serverSocket != null){
						serverSocket.close();
					}
				} catch (IOException e) {
					System.err.println("[SERVER]> EXCEPTION in closing the server socket." + e);
				}
		}
	}
}
