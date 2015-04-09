web-client-server
-----------
A Java implementation of Simple HTTP Web Client and a Multithreaded Web Server.

###Development Tools:    
1. **Programming Language:** Java (jdk 1.7)
2. **IDE:** Eclipse Juno (4.2)
3. **External Packages:** No external packages are required other than default Java packages like java.io and java.net.
4. **OS:** Windows 7
5. **Command Line Interface:** Windows command prompt used to run/test the program

###Directory Structure
1. **server:** Contains source files for the server implementation along with a default `index.htm` file.   
    * `ServerInitializer.java`: Initializes the WebServer at either a default 8080 or a user provided port.
    * `WebServer.java`: Implements a multhreaded server and initializes a serverSocket to listens to the client requests. Once a client is connected, the processing is handed over to a separate RequestHandler thread.
    * `RequestHandler.java`: Communicates with and processes a client's HTTP request in a separate thread.
	* `index.htm`: A default html file which is sent to the client in case a GET request contains "/" filepath.

2. **client:** Contains source files for the client implemenetation.
    * `WebClient.java`: Implements a single threaded web client which communicates with the server on a specific ip:port address and requests a file on the server.

###Compile & Run Instructions (Run on Windows cmd prompt):
1. Clear previously compiled files using    
    `del *.class`

2. Compile all server and client code located in separate directories.  
	`cd server`     
	`javac *.java`      
	`cd client`     
	`javac *.java`      

3. Run web server in server directory. If no port is passed, a default `8080` port is used. If port is already in use, a proper error message is displayed.
    * Default port `8080` will be used if following command is run  
	`java ServerInitializer`

	* Given port will be used if following command is run       
	`java ServerInitializer 12345`

4. Run web client in client directory by passing at least one argument i.e. serverHost/IP address. Other optional arguments are port and the path of the file to request from the server. If server cannot be reached due to a network issue or incorrect port value, a proper error message is displayed.

	* Default port `8080` and default file path "/" will be used if following command is run      
	`java WebClient localhost`

	* Given port and default file path "/" will be used if following command is run     
	`java WebClient localhost 12345`

	* Default port 8080 and given file path will be used if following command is run     
	`java WebClient localhost /path/to/file`

	* Given arguments will be used if following command is run      
	`java WebClient localhost 12345 /path/to/file`

5. If `/path/to/file` exists on the server, the server will return a `HTTP/1.0 200 OK` response with appropriate content-type and file content. The web client will extract status line and show it on the command prompt. The content of the response body (requested file) will be extracted and written to a local html file at the same location where WebClient was run from. The name of this html file will be created using the value of `<title>` tag of the html returned. If `<title>` tag does not contain any value, a default filename `index.html` is used. This is how browsers behave while naming a file when user saves it.

6. If `/path/to/file` does NOT exist on the server, the server will return a `HTTP/1.0 404 Not Found` response with a general error html file. Other behavior will be similar to what is explained	in the #5 above.

7. For nice printing, I have used `[SERVER - CLIENT<id>]>` prefix (id is a unique sequence number given to each client) on the server and `[CLIENT]> prefix` on the client for each output line.

8. All requested file paths must be relative to the `RequestHandler.java` class. If not, 404 error will be returned.


###References
1. Lab1 document available on Course Materials section of `2142-CSE-5344-002-COMPUTER-NETWORKS--2014-Spring` class on the UTA.
2. Book: Computer Networking. A Top Down Approach. Fifth Edition by James F. Kurose, Keith W. Ross. Chapter 2.
3. Format of the HTTP GET request and response from [Wikepedia](http://en.wikipedia.org/wiki/Hypertext_Transfer_Protocol)
4. Thread Tutorial from [Oracle](http://docs.oracle.com/javase/tutorial/essential/concurrency/runthread.html)
5. Socket Communications from [Oracle](http://www.oracle.com/technetwork/java/socket-140484.html)
