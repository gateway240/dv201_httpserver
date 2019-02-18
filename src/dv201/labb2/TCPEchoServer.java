/*
  UDPEchoServer.java
  A simple echo server with no error handling
*/

package dv201.labb2;
import java.io.*;
import java.net.*;

public class TCPEchoServer implements Runnable {

	Socket socket;
    public static final int MYPORT= 5555;

    TCPEchoServer(Socket socket){
    	this.socket = socket;
	}

    public static void main(String[] args) throws IOException {
    	System.out.println("Starting TCP Server on port: " + MYPORT);
		String clientMsg;
		ServerSocket welcomeSocket = new ServerSocket(MYPORT);
		Integer conCount= 0;
	while (true) {
		//Accept incoming connections
		Socket sock = welcomeSocket.accept();
		conCount++;
		System.out.println("New Connection: " + conCount);
		//Create a new thread and pass on the socket for handling on a separate thread
		new Thread(new TCPEchoServer(sock)).start();

	}
    }
	public void run() {
		try {
			//Read message from the socket
			BufferedReader inFromClient =
					new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String returnMessage = inFromClient.readLine();
			OutputStream output = socket.getOutputStream();
			PrintWriter writer = new PrintWriter(output, true);
			writer.println(returnMessage);
			System.out.println("RECEIVED: " + returnMessage);
			socket.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}