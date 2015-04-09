/**
 * CSE 5344 – Project 1
 * @author JYOTI SALITRA
 * Spring 2014
 */

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

//RequestHandler class handles processing for each of the client in a separate thread
//RequestHandler class implements Runnable interface and override it's public void run() method.

//Reference: Thread Tutorial from http://docs.oracle.com/javase/tutorial/essential/concurrency/runthread.html
//Reference: Socket Communications from http://www.oracle.com/technetwork/java/socket-140484.html
public class RequestHandler implements Runnable {

	private Socket clientSocket; //reference to the clientSocket passed by the WebServer after client's connection
	private int clientID; //unique clientID passed by WebServer for logging purpose

	private final String CRLF = "\r\n"; //carriage return line feed
	private final String SP = " "; //status line parts separator
	
	/**
	 * Constructor for RequestHandler to set clientSocket and clientID
	 * @param cs
	 * @param cID
	 */
	public RequestHandler(Socket cs, int cID) {
		this.clientSocket = cs;
		this.clientID = cID;
	}

	@Override
	public void run() {
		
		//define input and output streams
		BufferedReader socketInStream = null; //reads data received over the socket's inputStream
		DataOutputStream socketOutStream = null; //writes data over the socket's outputStream
		
		FileInputStream fis = null; //reads file from the local file system
		
		try {
			//get a reference to clientSocket's inputStream
			socketInStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
			//get a reference to clientSocket's outputStream
			socketOutStream = new DataOutputStream(clientSocket.getOutputStream());

			//read a request from socket inputStream
			String packet = socketInStream.readLine();
			
			//check if request is not null
			if(packet != null)
			{
				System.out.println("[SERVER - CLIENT"+clientID+"]> Received a request: " + packet);

				/** HTTP Request Format:
				 * GET index.htm HTTP/1.0 CRLF
				 */
				
				//split request line based on single whitespace into three parts
				String[] msgParts = packet.split(SP);
				
				// check if the request type is GET
				if (msgParts[0].equals("GET") && msgParts.length == 3) {
					
					//now get the path of the requested file from the request
					String filePath = msgParts[1];
					
					//check if filePath starts with a forward slash "/"
					//if not, add a forward slash and make it relative to the current file path
					if(filePath.indexOf("/") != 0)
					{	//filePath does not start with a forward slash
						//hence add one
						filePath = "/" + filePath;
					}
					
					
					System.out.println("[SERVER - CLIENT"+clientID+"]> Requested filePath: " + filePath);
					
					//if requested filePath is null or requesting a default index file
					if(filePath.equals("/"))
					{
						System.out.println("[SERVER - CLIENT"+clientID+"]> Respond with default /index.htm file");
						
						//set filePath to the default index.htm file
						filePath = filePath + "index.htm";
					}
					
					//make the filePath relative to the current location
					filePath = "." + filePath;

					//initialize a File object using filePath
					File file = new File(filePath);
					try {
						//check if file with filePath exists on this server
						if (file.isFile() && file.exists()) {
							
							//we are good.
							//now create a HTTP response and send it back to the client
							
							/**HTTP Response Format:
							 * HTTP/1.0 200 OK CRLF
							 * Content-type: text/html CRLF
							 * CRLF
							 * FILE_CONTENT....
							 * FILE_CONTENT....
							 * FILE_CONTENT....
							 */
							
							//write a status line on the response
							//since the requested file exists, we will send a 200 OK response
							String responseLine = "HTTP/1.0" + SP + "200" + SP + "OK" + CRLF;
							socketOutStream.writeBytes(responseLine);

							//write content type header line
							socketOutStream.writeBytes("Content-type: " + getContentType(filePath) + CRLF);
							
							//write a blank line representing end of response header
							socketOutStream.writeBytes(CRLF);
							
							//open the requested file
							fis = new FileInputStream(file);

							// initialize a buffer of size 1K.
							byte[] buffer = new byte[1024];
							int bytes = 0;
							
							// start writing content of the requested file into the socket's output stream.
							while((bytes = fis.read(buffer)) != -1 ) {
								socketOutStream.write(buffer, 0, bytes);
							}
							
							System.out.println("[SERVER - CLIENT"+clientID+"]> Sending Response with status line: " + responseLine);
							//flush outputstream
							socketOutStream.flush();
							System.out.println("[SERVER - CLIENT"+clientID+"]> HTTP Response sent");
							
						} else {
							//The requested file does not exist on this server
							System.out.println("[SERVER - CLIENT"+clientID+"]> ERROR: Requested filePath " + filePath + " does not exist");

							//write a status line on the response with 404 Not Found response
							String responseLine = "HTTP/1.0" + SP + "404" + SP + "Not Found" + CRLF;
							socketOutStream.writeBytes(responseLine);

							//write content type header line
							socketOutStream.writeBytes("Content-type: text/html" + CRLF);
							
							//write a blank line representing end of response header
							socketOutStream.writeBytes(CRLF);
							
							//send content of the errorFile
							socketOutStream.writeBytes(getErrorFile());
							
							System.out.println("[SERVER - CLIENT"+clientID+"]> Sending Response with status line: " + responseLine);
							
							//flush outputstream
							socketOutStream.flush();
							System.out.println("[SERVER - CLIENT"+clientID+"]> HTTP Response sent");
						}
						
					} catch (FileNotFoundException e) {
						System.err.println("[SERVER - CLIENT"+clientID+"]> EXCEPTION: Requested filePath " + filePath + " does not exist");
					} catch (IOException e) {
						System.err.println("[SERVER - CLIENT"+clientID+"]> EXCEPTION in processing request." + e.getMessage());
					}
				} else {
					System.err.println("[SERVER - CLIENT"+clientID+"]> Invalid HTTP GET Request. " + msgParts[0]);
				}
			}
			else
			{
				//While testing with the browser, I found that sometimes browser send other request like favicon etc.
				//Therefore I discard those unknown requests.
				System.err.println("[SERVER - CLIENT"+clientID+"]> Discarding a NULL/unknown HTTP request.");
			}

		} catch (IOException e) 
		{
			System.err.println("[SERVER - CLIENT"+clientID+"]> EXCEPTION in processing request." + e.getMessage());
			
		} finally {
			//close the resources
			try {
				if (fis != null) {
					fis.close();
				}
				if (socketInStream != null) {
					socketInStream.close();
				}
				if (socketOutStream != null) {
					socketOutStream.close();
				}
				if (clientSocket != null) {
					clientSocket.close();
					System.out.println("[SERVER - CLIENT"+clientID+"]> Closing the connection.\n");
				}
			} catch (IOException e) {
				System.err.println("[SERVER - CLIENT"+clientID+"]> EXCEPTION in closing resource." + e);
			}
		}
	}
	
	/**
	 * Get Content-type of the file using its extension
	 * @param filePath
	 * @return content type
	 */
	private String getContentType(String filePath)
	{
		//check if file type is html
		if(filePath.endsWith(".html") || filePath.endsWith(".htm"))
		{
			return "text/html";
		}
		//otherwise, a binary file
		return "application/octet-stream";
	}
	
	/**
	 * Get content of a general 404 error file
	 * @return errorFile content
	 */
	private String getErrorFile ()
	{
		String errorFileContent = 	"<!doctype html>" + "\n" +
									"<html lang=\"en\">" + "\n" +
									"<head>" + "\n" +
									"    <meta charset=\"UTF-8\">" + "\n" +
									"    <title>Error 404</title>" + "\n" +
									"</head>" + "\n" +
									"<body>" + "\n" +
									"    <b>ErrorCode:</b> 404" + "\n" +
									"    <br>" + "\n" +
									"    <b>Error Message:</b> The requested file does not exist on this server." + "\n" +
									"</body>" + "\n" +
									"</html>";
		return errorFileContent;
	}
}
