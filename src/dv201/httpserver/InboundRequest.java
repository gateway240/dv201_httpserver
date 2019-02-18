package dv201.httpserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.HashMap;

public class InboundRequest implements Runnable {
    Socket socket;
    BufferedReader in;
    PrintWriter out;

    private final String file404File = "root/404.html";
    private final String file403File = "root/403.html";
    private final String file500File = "root/500.html";

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
            String line = in.readLine();
            ParseHeader(line);

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
        File fileToSend = new File(file500File);
        new ReplyHeader(status, contentType).SendHeader(out);
        try {
            Files.copy(fileToSend.toPath(), socket.getOutputStream());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } // maybe bad
    }

    public void ParseHeader(String inboundHeader) {
        String[] words = inboundHeader.split("\\s");
        switch (words[0]) {
        case "GET":
            String requestedGetFile = words[1].substring(1);
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

        if (status == Status.STATUS302){
            new ReplyHeader(status, contentType, location).SendHeader(out);
        }else{

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
            } // maybe bad
        }
    }

    private void HandlePut() {

    }

    private void HandlePost() {

    }

    @Override
    public void run() {
        AcceptIncomingConnection();
    }
}
