package dv201.httpserver;

import dv201.httpserver.enums.ContentType;
import dv201.httpserver.enums.Status;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dv201.httpserver.HTTPServerLib.FILE_CONTENTS;
import static dv201.httpserver.HTTPServerLib.FILE_NAME;
import static dv201.httpserver.InboundRequestHeader.BUFF_SIZE;

class InboundRequest implements Runnable {
    private Socket socket;
    private InputStream in;
    private PrintWriter out;

    private static final String DIR_PREFIX = "root/";

    private static final String FILE_404_FILE = DIR_PREFIX + "404.html";
    private static final String FILE_403_FILE = DIR_PREFIX + "403.html";
    private static final String FILE_500_FILE = DIR_PREFIX + "500.html";
    private static final String UPLOAD_DIR = DIR_PREFIX + "upload/";

    private final static Map<String, String> REDIRECT302 = new HashMap<String, String>() {
        {
            put(DIR_PREFIX + "alex.html", "alexTemp.html");
        }
    };

    private final static List<File> FORBIDDEN = new ArrayList<File>() {
        {
            add(new File(DIR_PREFIX + "forbidden.html"));
        }
    };

    public InboundRequest(Socket socket) {

        this.socket = socket;
        try {
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
            System.err.println("Error while handling the connection --> 500");
            e.printStackTrace();
            System.err.println(e);
        } finally {
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
        File fileToSend = Paths.get(FILE_500_FILE).toFile();
        new ReplyHeader(status, contentType, fileToSend.length()).SendHeader(out);
        try {
            Files.copy(fileToSend.toPath(), socket.getOutputStream());
        } catch (IOException e) {
            System.err.println("Unable to send 500 Status. Maybe Client Disconnected");
        }
    }

    private void HandleHeader(InboundRequestHeader requestHeader) throws IOException {
        switch (requestHeader.getRequestType()) {
        case GET:
            HandleGet(DIR_PREFIX + requestHeader.getRequestedResource());
            break;

        case POST:
            HandlePost(requestHeader);
            break;

        case PUT:
            HandlePut(requestHeader);
            break;

        default:
            break;
        }
    }

    private void HandleGet(String requestedGetFile) throws IOException {

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
            new ReplyHeader(status, location).SendHeader(out);
        } else {

            if (!(fileToSend != null && fileToSend.isFile() && Files.isReadable(fileToSend.toPath()))) {
                System.err.println("File not found: " + fileToSend.getPath());
                status = Status.STATUS404;
                contentType = ContentType.HTML;
                fileToSend = new File(FILE_404_FILE);
            } else if (FORBIDDEN.contains(fileToSend)) {
                System.err.println("Forbidden: " + fileToSend.getPath());
                status = Status.STATUS403;
                contentType = ContentType.HTML;
                fileToSend = new File(FILE_403_FILE);
            }
            new ReplyHeader(status, contentType, fileToSend.length()).SendHeader(out);
            try {
                Files.copy(fileToSend.toPath(), socket.getOutputStream());
                socket.getOutputStream().flush();
            } catch (IOException e) {
                socket.getOutputStream().write("Something went wrong after already sending the header".getBytes());
            }
        }
    }

    private void HandlePost(InboundRequestHeader requestHeader) throws IOException {
        if (requestHeader.isExpectContinue()) {
            new ReplyHeader(Status.STATUS100).SendHeader(out);
        }
        WriteToFile(requestHeader);
        // new ReplyHeader(status, contentType).SendHeader(out);
        HandleGet(DIR_PREFIX + requestHeader.getRequestedResource());
    }

    private void WriteToFile(InboundRequestHeader requestHeader) throws IOException {
        FileOutputStream os = new FileOutputStream(UPLOAD_DIR + requestHeader.startPNGReadingAndFilename());
        while (true) {
            byte[] buffer = new byte[1024];
            int len = requestHeader.readPNG(buffer);
            // System.out.println(buffer);
            os.write(buffer, 0, len);
            if (len != 1024)
                break;
        }
        os.close();
    }

    private void HandlePut(InboundRequestHeader requestHeader) throws IOException {
        if (requestHeader.isExpectContinue()) {
            new ReplyHeader(Status.STATUS100).SendHeader(out);
        }

        String fileName = requestHeader.getRequestedResource();
        System.out.println(fileName);
        Status status;
        String fileLocationHeader;
        if (fileName != null) {
            File f = new File(DIR_PREFIX + fileName);
            if (f.exists() && f.isFile()) {
                status = Status.STATUS204;
                fileLocationHeader = fileName;
            } else if (!f.exists()) {
                status = Status.STATUS201; // Morgan said we can send a 204 in both cases (created and overridden)
                fileLocationHeader = fileName;
            } else if (f.exists() && f.isDirectory()) {
                f = new File(f, "index.html");
                if (f.exists()) {
                    status = Status.STATUS204;
                } else {
                    status = Status.STATUS201;
                }
                // Morgan said we can send a 204 in both cases (created and overridden)
                fileLocationHeader = Paths.get(DIR_PREFIX).relativize(f.toPath()).toString();
            } else {
                // not reachable
                throw new RuntimeException("this should not be reachable");
            }
            FileOutputStream os = new FileOutputStream(f.getPath());

            while (true) {
                byte[] buffer = new byte[1024];
                int len = requestHeader.readPNGPut(buffer);
                if (len == -1)
                    break;
                os.write(buffer, 0, len);
                if (len != 1024)
                    break;
            }
            os.close();
            new ReplyHeader(status, fileLocationHeader).SendHeader(out);
        } else {
            // ?? why do you say 200 OK and not 204 NO CONTENT
            // status = Status.STATUS200;
            // new ReplyHeader(status, contentType).SendHeader(out);
            send500();
        }
    }

    @Override
    public void run() {
        AcceptIncomingConnection();
    }
}
