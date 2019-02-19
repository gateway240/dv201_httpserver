package dv201.httpserver;

import dv201.httpserver.enums.ContentType;
import dv201.httpserver.enums.Status;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static dv201.httpserver.HTTPServerLib.FILE_CONTENTS;
import static dv201.httpserver.HTTPServerLib.FILE_NAME;


public class InboundRequest implements Runnable {
    Socket socket;
    BufferedReader in;
    PrintWriter out;

    private final String file404File = "root/404.html";
    private final String file403File = "root/403.html";
    private final String file500File = "root/500.html";
    private final String uploadDir = "root/upload/";


    private final static Map<String, String> REDIRECT302 = new HashMap<String, String>() {
        {
            put("root/alex.html", "/root/alexTemp.html");
        }
    };

    InboundRequest(Socket socket) {

        this.socket = socket;
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
            InboundRequestHeader requestHeader = new InboundRequestHeader(in);
            HandleHeader(requestHeader);
        } catch (Exception e) {
            send500();
            // System.err.println("Error with sending the Echo, maybe the Client is dead");
            System.err.println(e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void send500() {
        Status status = Status.STATUS500;
        ContentType contentType = ContentType.HTML;
        File fileToSend = Paths.get(file500File).toFile();
        new ReplyHeader(status, contentType).SendHeader(out);
        try {
            Files.copy(fileToSend.toPath(), socket.getOutputStream());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } // maybe bad
    }

    public void HandleHeader(InboundRequestHeader requestHeader) {
        switch (requestHeader.getRequestType()) {
            case GET:
                HandleGet(requestHeader.getRequestedResource());
                break;

            case POST:
                HandlePost(requestHeader.getPayload());
                break;

            case PUT:
                HandlePut(requestHeader.getPayload());
                break;

            default:
                break;
        }
    }

    private void HandleGet(String requestedGetFile) {

        // ReplyHeader replyHeader = new ReplyHeader(Status.STATUS500, contentType);

        Status status = Status.STATUS500;
        ContentType contentType = null;
        String location = null;
        File fileToSend = null;

        if (REDIRECT302.containsKey(requestedGetFile)) {
            status = Status.STATUS302;
            location = REDIRECT302.get(requestedGetFile);
        } else if (requestedGetFile.endsWith(".html") || requestedGetFile.endsWith(".htm")) {
            status = Status.STATUS200;
            contentType = ContentType.HTML;
            fileToSend = Paths.get(requestedGetFile).toFile();
        } else if (requestedGetFile.endsWith(".png")) {
            status = Status.STATUS200;
            contentType = ContentType.PNG;
            fileToSend = Paths.get(requestedGetFile).toFile();
        } else {
            File mayFolder = Paths.get(requestedGetFile).toFile();

            File tryIndexFile = new File(mayFolder, "index.html");
            if (!tryIndexFile.exists()) {
                tryIndexFile = new File(mayFolder, "index.htm");
            }
            fileToSend = tryIndexFile;
            status = Status.STATUS200;
            contentType = ContentType.HTML;
        }

        if (status == Status.STATUS302) {
            new ReplyHeader(status, contentType, location).SendHeader(out);
        } else {

            if (!(fileToSend != null && fileToSend.isFile() && Files.isReadable(fileToSend.toPath()))) {
                System.err.println("File not found: " + fileToSend.getPath());
                status = Status.STATUS404;
                contentType = ContentType.HTML;
                fileToSend = new File(file404File);
            } else if (false) { // ToDo Forbidden
                System.err.println("Forbidden: " + fileToSend.getPath());
                status = Status.STATUS403;
                contentType = ContentType.HTML;
                fileToSend = new File(file403File);
            }
            new ReplyHeader(status, contentType).SendHeader(out);
            try {
                Files.copy(fileToSend.toPath(), socket.getOutputStream());
                socket.getOutputStream().flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void HandlePost(Map<String, String> postParams) {
//        System.out.println(postParams.get("myImage"));
        Status status = Status.STATUS200;
        ContentType contentType = ContentType.PNG;


        new ReplyHeader(status, contentType).SendHeader(out);

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void HandlePut(Map<String, String> postParams) {
        String fileContents = postParams.get(FILE_CONTENTS);
        String fileName = (postParams.get(FILE_NAME));

        ContentType contentType = ContentType.URLENCODED;
        Status status;
        if(fileContents != null && fileName != null){
            File f = new File(uploadDir + fileName + ".txt");
            if(f.exists() && !f.isDirectory()){
                status = Status.STATUS204;
                new ReplyHeader(status, contentType).SendHeader(out);
            }
            else{
                status = Status.STATUS201;
                new ReplyHeader(status, contentType).SendHeader(out);
            }
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(f);
                fileWriter.write(fileContents);
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally{
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else{
            status = Status.STATUS200;
            new ReplyHeader(status, contentType).SendHeader(out);
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void run() {
        AcceptIncomingConnection();
    }
}
