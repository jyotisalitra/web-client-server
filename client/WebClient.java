/**
 * CSE 5344 – Project 1
 * @author JYOTI SALITRA
 * Spring 2014
 */

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

//WebClient represents single web client

//Reference: Socket Communications from http://www.oracle.com/technetwork/java/socket-140484.html
public class WebClient {

	public static void main(String[] args) {
		final String CRLF = "\r\n"; //carriage return line feed
		final String SP = " "; //status line parts separator
		
		String serverHost = null;
		
		//initialize serverPort with default port value
		int serverPort = 8080;
		
		//initialize filePath with default file /
		String filePath = "/";
		
		//check command line arguments for serverhost, port, and filePath
		//at least serverHost is required
		if(args.length == 1)
		{
			//first argument is serverHost
			serverHost = args[0];
		}
		else if (args.length == 2){
			//first argument is serverHost
			serverHost = args[0];
			
			//second can be either serverPort or filePath
			try {
				serverPort = Integer.parseInt(args[1]); //check if port is an integer
			}
			catch (NumberFormatException nfe)
			{
				System.err.println("[CLIENT]> Integer Port is not provided. Default Server port will be used.");
				
				//then assume this string is filePath
				filePath = args[1];
			}
		}
		else if (args.length == 3){
			//first argument is serverHost
			serverHost = args[0];
			
			//second argument is serverPort
			try {
				serverPort = Integer.parseInt(args[1]); //check if port is an integer
			}
			catch (NumberFormatException nfe)
			{
				System.err.println("[CLIENT]> Integer Port is not provided. Default Server port will be used.");
			}
			
			//third argument is fileName
			filePath = args[2];
		}
		else
		{
			System.err.println("[CLIENT]> Not enough parameters provided. At least serverHost is required.");
			System.exit(-1);
		}
		
		System.out.println("[CLIENT]> Using Server Port: " + serverPort);
		System.out.println("[CLIENT]> Using FilePath: " + filePath);
		
		//define a socket
		Socket socket = null;
		
		//define input and output streams
		BufferedReader socketInStream = null; //reads data received over the socket's inputStream
		DataOutputStream socketOutStream = null; //writes data over the socket's outputStream
		
		FileOutputStream fos = null; //writes content of the responded file in a file
		
		try {
			
			//get inet address of the serverHost
			InetAddress serverInet = InetAddress.getByName(serverHost);
			
			//try to connect to the server
			socket = new Socket(serverInet, serverPort);
			System.out.println("[CLIENT]> Connected to the server at " + serverHost + ":" + serverPort);
			
			//get a reference to socket's inputStream
			socketInStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			//get a reference to socket's outputStream
			socketOutStream = new DataOutputStream(socket.getOutputStream());

			//now send a HTTP GET request
			String requestLine = "GET" + SP + filePath + SP +"HTTP/1.0" + CRLF;
			System.out.println("[CLIENT]> Sending HTTP GET request: " + requestLine);
			
			//send the requestLine
			socketOutStream.writeBytes(requestLine);
			
			//send an empty line
			socketOutStream.writeBytes(CRLF);
			
			//flush out output stream
			socketOutStream.flush();
			
			System.out.println("[CLIENT]> Waiting for a response from the server");
			//extract response Code
			String responseLine = socketInStream.readLine();
			System.out.println("[CLIENT]> Received HTTP Response with status line: " + responseLine);

			//extract content-type of the response
			String contentType = socketInStream.readLine();
			System.out.println("[CLIENT]> Received " + contentType);

			//read a blank line i.e. CRLF
			socketInStream.readLine();

			System.out.println("[CLIENT]> Received Response Body:");
			//start reading content body
			StringBuilder content = new StringBuilder();
			String res;
			while((res = socketInStream.readLine()) != null)
			{
				//save content to a buffer
				content.append(res + "\n");
				
				//print it as well
				System.out.println(res);
			}
			
			//get a name of the file from the response
			String fileName = getFileName(content.toString());
			
			//open a outputstream to the fileName
			//file will be created if it does not exist
			fos = new FileOutputStream(fileName);
			
			fos.write(content.toString().getBytes());
			fos.flush();
			
			System.out.println("[CLIENT]> HTTP Response received. File Created: " + fileName);

		} catch (IllegalArgumentException iae) {
			System.err.println("[CLIENT]> EXCEPTION in connecting to the SERVER: " + iae.getMessage());
		} catch (IOException e) {
			System.err.println("[CLIENT]> ERROR " + e);
		}
		finally {
			try {
				//close all resources
				if (socketInStream != null) {
					socketInStream.close();
				}
				if (socketOutStream != null) {
					socketOutStream.close();
				}
				if (fos != null) {
					fos.close();
				}
				if (socket != null) {
					socket.close();
					System.out.println("[CLIENT]> Closing the Connection.");
				}
			} catch (IOException e) {
				System.err.println("[CLIENT]> EXCEPTION in closing resource." + e);
			}
		}
	}

	/**
	 * Returns a file name from the html content.
	 * Generally it is the value of the <title> tag
	 * @param content
	 * @return fileName
	 */
	private static String getFileName(String content)
	{
		//default filename if <title> tag is empty
		String filename = "";
		
		filename = content.substring(content.indexOf("<title>")+("<title>").length(), content.indexOf("</title>"));
		
		if(filename.equals(""))
		{
			filename = "index";
		}
		
		filename = filename+".htm";
		
		return filename;
	}
}
