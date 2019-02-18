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

        try (ServerSocket serverSocket = new ServerSocket(myPort)) {
            int requestCount = 0;
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Accepting New Request: " + requestCount++);
                new Thread(new InboundRequest(socket)).start();
            }
        } catch (Exception e) {
            FatalError("Error creating or accepting server socket." + e.getMessage());
        }


    }
    private static void FatalError(String errorMsg){
        System.err.println(errorMsg);
        System.exit(1);
    }

}
