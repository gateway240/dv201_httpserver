package dv201.httpserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;


public class InboundRequest implements Runnable {
    Socket socket;
    BufferedReader in;
    PrintWriter out;


    InboundRequest(Socket socket) {

        this.socket =socket;
        try {
            // we read characters from the client via input stream on the socket
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // we get character output stream to client (for headers)
            out = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void AcceptIncomingConnection() {
        try {
            String line = in.readLine();
            ParseHeader(line);

        } catch (IOException e) {
            //System.err.println("Error with sending the Echo, maybe the Client is dead");
            System.err.println(e);
        }
        finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
//    public void SendOKResponse() {
//        out.println(HTTPServerLib.GenerateOKHeader());
//        out.flush();
//    }
    public void ParseHeader(String inboundHeader){
        String[] words = inboundHeader.split("\\s");
        switch (words[0]) {
            case "GET":
                String requestedGetFile = words[1];
                HandleGet(requestedGetFile);
                break;

            case "POST":

                break;

            case "PUT":

                break;

            default:
                break;
        }
    }
    private void HandleGet(String Resource){

        if (Resource.endsWith(".html") || Resource.endsWith(".png")) {
            try {
                Files.copy(Paths.get(Resource.substring(1)), socket.getOutputStream());
                ContentType contentType = null;
                if(Resource.endsWith(".html")){
                    contentType = ContentType.HTML;
                }
                else{
                    contentType = ContentType.PNG;
                }
                ReplyHeader replyHeader = new ReplyHeader(Status.STATUS200,contentType);
                replyHeader.SendHeader(out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void HandlePut(){

    }
    private void HandlePost(){

    }

    @Override
    public void run() {
        AcceptIncomingConnection();
    }
}
