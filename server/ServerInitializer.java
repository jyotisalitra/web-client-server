/**
 * CSE 5344 – Project 1
 * @author JYOTI SALITRA
 * Spring 2014
 */

//Initializes WebServer by passing port from the command line 

//Reference: Thread Tutorial from http://docs.oracle.com/javase/tutorial/essential/concurrency/runthread.html

public class ServerInitializer {

	//main method
	public static void main(String[] args) {

		//initialize port to default 8080
		int port = 8080;
		
		//check command line arguments for port
		if(args.length == 1)
		{
			//port is provided
			try {
				port = Integer.parseInt(args[0]); //check if port is an integer
			}
			catch (NumberFormatException nfe)
			{
				System.err.println("[SERVER]> Integer Port is not provided. Server will start at default port.");
			}
		}

		System.out.println("[SERVER]> Using Server Port : " + port);
		
		//constructing WebServer object
		WebServer ws = new WebServer(port);
		
		//start WebServer in a new thread
		new Thread(ws).start();
	}
}
