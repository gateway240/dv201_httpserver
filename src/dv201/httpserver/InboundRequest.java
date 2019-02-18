package dv201.httpserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;


public class InboundRequest implements Runnable {
    Socket socket;

    InboundRequest(Socket socket) {this.socket =socket;}

    public void AcceptIncomingConnection() {
        try {

            BufferedReader in = null;

            BufferedOutputStream dataOut = null;
            String fileRequested = null;


            // we read characters from the client via input stream on the socket
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // we get character output stream to client (for headers)

            // get binary output stream to client (for requested data)
            dataOut = new BufferedOutputStream(socket.getOutputStream());


            String line = in.readLine();
            ParseHeader(line);



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
    public void SendOKResponse() {
        PrintWriter out = null;
        try {
            out = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: text/html;charset=UTF-8");
        out.println();
        out.flush();
    }
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            socket.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //out.print(Files.isReadable(Paths.get(requestedGetFile)));
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
