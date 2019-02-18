package dv201.httpserver;

import java.net.ServerSocket;
import java.net.Socket;



public class HTTPServer {

    private static final String ARGS_USAGE = "usage: MyPort\n";
    public static void main(String[] args){

        int myPort;

        if (args.length != 1) {
           FatalError(ARGS_USAGE);
            return;
        }
        String _myPortStr = args[0];

        // check the port number
        myPort = HTTPServerLib.ParsePort(_myPortStr);
        if(myPort <= 0){
            FatalError(ARGS_USAGE);
            return;
        }

        // create the server socket
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(myPort);
        } catch (Exception e) {
            FatalError("Exception while creating the socket");
            return;
        }
        System.out.println("Server started on port: " + myPort);
        //accept a incoming connection and run the async echo reply
        int requestCount = 0;
//        ExecutorService ex = Executors.newCachedThreadPool();
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Accepting New Request: " + requestCount++);
                new Thread(new InboundRequest(socket)).start();
//                CompletableFuture.runAsync(() -> AcceptIncomingConnection(socket), ex);
            }
        } catch (Exception e) {
            System.err.println("Exception while waiting for connections" + e.toString());
            try {
                serverSocket.close();
            } catch (Exception f) {
                System.err.println("Execption closing socket" + e.toString());
            }
        }

    }
    private static void FatalError(String errorMsg){
        System.err.println(errorMsg);
        System.exit(1);
    }

}
