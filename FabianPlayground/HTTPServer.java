/*
  UDPEchoServer.java
  A simple echo server with no error handling
*/

//package dv201.labb2;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HTTPServer {
	private static final String argsUsage = "usage: MyPort\n";

	public static void main(String[] args) {
		int myPort;

		if (args.length != 1) {
			System.err.printf(argsUsage);
			System.exit(1);
			return;
		}


		// check the port number
		try {
			myPort = Integer.valueOf(args[0]);
		} catch (Exception e) {
			System.err.printf(argsUsage);
			System.err.printf("problem occured while process your serverport input\n");
			System.exit(1);
			return;
		}

		if (myPort < 0 || myPort > 65535) {
			System.err.printf(argsUsage);
			System.err.printf("your Port is not between 0 and 65535\n");
			System.exit(1);
			return;
		}


		// create the server socket
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(myPort);
		} catch (Exception e) {
			System.err.println("Exception while creating the socket");
			System.exit(1);
			return;
		}

		//accept a incoming connection and run the async echo reply
		ExecutorService ex = Executors.newCachedThreadPool();
		try {
			while (true) {
				Socket socket = serverSocket.accept();
				System.out.println("accept start");
				CompletableFuture.runAsync(() -> doTheEcho(socket), ex);
			}
		} catch (Exception e) {
			System.err.println("Exception while waiting for connections");
			try {
				serverSocket.close();
			} catch (Exception f) {
				// Don't care
			}

		}

	}

	private static void doTheEcho(Socket socket) {
		try {

			BufferedReader in = null;
			PrintWriter out = null;
			BufferedOutputStream dataOut = null;
			String fileRequested = null;


			// we read characters from the client via input stream on the socket
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			// we get character output stream to client (for headers)
			out = new PrintWriter(socket.getOutputStream());
			// get binary output stream to client (for requested data)
			dataOut = new BufferedOutputStream(socket.getOutputStream());

			
			String line = in.readLine();
			String[] words = line.split("\\s");
			switch (words[0]) {
				case "GET":
					String requestedGetFile = words[1];
					
					//FileInputStream file = new FileInputStream(requestedGetFile);

					

					

			
					if (requestedGetFile.endsWith(".html")){
						out.println("HTTP/1.1 200 OK");
						out.println("Content-Type: text/html;charset=UTF-8");
						out.println();
						out.flush();
						Files.copy(Paths.get(requestedGetFile.substring(1)), socket.getOutputStream());
					}else if (requestedGetFile.endsWith(".png")){
						out.println("HTTP/1.1 200 OK");
						out.println("Content-Type: image/png");
						out.println();
						out.flush();
						Files.copy(Paths.get(requestedGetFile.substring(1)), socket.getOutputStream());
					}
					socket.getOutputStream().flush();
					//out.print(Files.isReadable(Paths.get(requestedGetFile)));
					break;

				case "POST":

					break;

				case "PUT":

					break;
			
				default:
					break;
			}
			

			//RequestHeader reqHead = extractRequestHeader(rec);

			String header 	= "HTTP/1.1 200 OK\n"
							+ "Date: Wed, 13 Feb 2019 14:44:00 GMT\n"
							+"Server: Apache-Coyote/1.1\n"
							+"Content-Type: text/html;charset=UTF-8\n"
							+"Content-Language: sv-SE\n"
							+"Connection: close\n"
							+"Set-Cookie: f5_cspm=1234;\n"
							//+"Transfer-Encoding: chunked\n"
							+"\r\n"
							+"Hallo Welt";
			//socket.getOutputStream().write(header.getBytes());
			//socket.getOutputStream().flush();
			//System.out.println(header);
		} catch (IOException e) {
			//System.err.println("Error with sending the Echo, maybe the Client is dead");
			System.err.println(e);
		}

		try {
			socket.close();
		} catch (Exception e) {
			//TODO: handle exception
		}
		

	}

/* 	public static File getPath(String requestedGetFile){
		String[] directories = requestedGetFile.split("/");
		try {
			File file = new File(Paths.get("", directories));
			if (file.isFile()){
				return file;
			}
			file = new File(Paths.get("",  "index.html"));
			if (file.isFile()) {
				return file;
			}
		} catch (NoSuchFileException e) {}

		
	} */

	/*public static ResponseHeader extractRequestHeader(String message){
		return new RequestHeader(message);
	}*/
}