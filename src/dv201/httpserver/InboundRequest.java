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


class InboundRequest implements Runnable {
    private Socket socket;
    private InputStream in;
    private PrintWriter out;

    private static final String DIR_PREFIX = "root/";

    private static final String FILE_404_FILE = DIR_PREFIX + "404.html";
    private static final String FILE_403_FILE = DIR_PREFIX +"403.html";
    private static final String FILE_500_FILE = DIR_PREFIX +"500.html";
    private static final String UPLOAD_DIR = DIR_PREFIX +"upload/";


    private final static Map<String, String> REDIRECT302 = new HashMap<String, String>() {
        {
            put(DIR_PREFIX +"alex.html",DIR_PREFIX + "alexTemp.html");
        }
    };

    public InboundRequest(Socket socket) {

        this.socket = socket;
        try {
//            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            in = socket.getInputStream();
            out = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void AcceptIncomingConnection() {
        try {
            InboundRequestHeader requestHeader = new InboundRequestHeader(in);
            HandleHeader(requestHeader);

        } catch (Exception e) {
            send500();
            System.err.println("Error with sending the Echo, maybe the Client is dead");
//            System.err.println(e);
        }
        finally {
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void send500() {
        Status status = Status.STATUS500;
        ContentType contentType = ContentType.HTML;
        //Get the 500 error
        // file and send it
        File fileToSend = Paths.get(FILE_500_FILE).toFile();
        new ReplyHeader(status, contentType).SendHeader(out);
        try {
            Files.copy(fileToSend.toPath(), socket.getOutputStream());
        } catch (IOException e) {
            System.err.println("Unable to send 500 Status. Maybe Client Disconnected" );
        }
    }

    private void HandleHeader(InboundRequestHeader requestHeader) {
        switch (requestHeader.getRequestType()) {
            case GET:
                HandleGet(requestHeader.getRequestedResource());
                break;

            case POST:
                HandlePost(requestHeader.getPayload(), requestHeader.getFilePayload());
                break;

            case PUT:
                HandlePut(requestHeader.getPayload());
                break;

            default:
                break;
        }
    }

    private void HandleGet(String requestedGetFile) {

        Status status;
        ContentType contentType = ContentType.HTML;
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
                fileToSend = new File(FILE_404_FILE);
            } else if (false) { // ToDo Forbidden
                System.err.println("Forbidden: " + fileToSend.getPath());
                status = Status.STATUS403;
                contentType = ContentType.HTML;
                fileToSend = new File(FILE_403_FILE);
            }
            new ReplyHeader(status, contentType).SendHeader(out);
            try {
                Files.copy(fileToSend.toPath(), socket.getOutputStream());
                socket.getOutputStream().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void HandlePost(Map<String, String> postParams, byte[] payload) {
        Status status = Status.STATUS200;
        ContentType contentType = ContentType.PNG;
        new ReplyHeader(status, contentType).SendHeader(out);


        try {
            WriteToFile(payload);

//            socket.close();/
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void WriteToFile(byte[] data) throws IOException {
        DataOutputStream os = new DataOutputStream(new FileOutputStream(DIR_PREFIX  + "Images/test.png"));
        os.write(data);
        os.close();
    }
    private void HandlePut(Map<String, String> postParams) {
        String fileContents = postParams.get(FILE_CONTENTS);
        String fileName = (postParams.get(FILE_NAME));

        ContentType contentType = ContentType.URLENCODED;
        Status status;
        if(fileContents != null && fileName != null){
            File f = new File(UPLOAD_DIR + fileName + ".txt");
            if(f.exists() && !f.isDirectory()){ //?? why is it no content, if it exists and is not a directory, becaue than it is a file 
                status = Status.STATUS204;
                new ReplyHeader(status, contentType).SendHeader(out);
            }
            else{
                // I would try to send that OK message *after* you actual wrote the file without an error
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
            //?? why do you say 200 OK and not 204 NO CONTENT
            status = Status.STATUS200;
            new ReplyHeader(status, contentType).SendHeader(out);
        }
    }


    @Override
    public void run() {
        AcceptIncomingConnection();
    }
}
