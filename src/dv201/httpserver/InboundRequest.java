package dv201.httpserver;

import dv201.httpserver.enums.ContentType;
import dv201.httpserver.enums.Status;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class InboundRequest implements Runnable {
    private final Socket socket;
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
            add(new File(FILE_404_FILE));
            add(new File(FILE_403_FILE));
            add(new File(FILE_500_FILE));
        }
    };

    public InboundRequest(Socket socket) throws IOException {
        this.socket = socket;
        in = socket.getInputStream();
        out = new PrintWriter(socket.getOutputStream());
    }

    private void AcceptIncomingConnection() {
        try {
            InboundRequestHeader requestHeader = new InboundRequestHeader(in);
            HandleHeader(requestHeader);

        } catch (Exception e) {
            send500();
            System.err.println("Error while handling the connection --> HTTP 500");
            e.printStackTrace();
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
        // all problems and error in the server should lead to this
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
            // redirect 302
            status = Status.STATUS302;
            location = REDIRECT302.get(requestedGetFile);
        } else if (requestedGetFile.endsWith(".html") || requestedGetFile.endsWith(".htm")) {
            // normal html file
            status = Status.STATUS200;
            contentType = ContentType.HTML;
            fileToSend = Paths.get(requestedGetFile).toFile();
        } else if (requestedGetFile.endsWith(".png")) {
            // normal png file
            status = Status.STATUS200;
            contentType = ContentType.PNG;
            fileToSend = Paths.get(requestedGetFile).toFile();
        } else {
            // not a file, so maybe a folder for which we can serve a index file
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
            // 302 has no body so it is just that little header that is send
            new ReplyHeader(status, location).SendHeader(out);
        } else {
            if (!(fileToSend != null && fileToSend.isFile() && Files.isReadable(fileToSend.toPath()))) {
                // if the file does not exist or is not readable for any reason, send a 404
                System.err.println("File not found: " + fileToSend.getPath());
                status = Status.STATUS404;
                contentType = ContentType.HTML;
                fileToSend = new File(FILE_404_FILE);
            } else if (FORBIDDEN.contains(fileToSend)) {
                // if the file is forbidden to access, send a 403
                System.err.println("Forbidden: " + fileToSend.getPath());
                status = Status.STATUS403;
                contentType = ContentType.HTML;
                fileToSend = new File(FILE_403_FILE);
            }
            // send the header
            new ReplyHeader(status, contentType, fileToSend.length()).SendHeader(out);

            // send the body
            try {
                Files.copy(fileToSend.toPath(), socket.getOutputStream());
                socket.getOutputStream().flush();
            } catch (IOException e) {
                // if this happens, we unfortunately already sent the OK header and no we should send a 500
                // we accept this because it should be very unlikely to happen because we already checked the isReadable()
                // a way to avoid this, would be to read in first the file, than send the header, and then the file content.
                // this would use more RAM because the whole file would be temporarily saved
                // and even then there could be a error in sending after the OK is already sent.
                // This is why we choose this way.
                socket.getOutputStream().write("Something went wrong after already sending the header".getBytes());
            }
        }
    }

    private void HandlePost(InboundRequestHeader requestHeader) throws IOException {
        // send the continue header, if the client asked for it
        if (requestHeader.isExpectContinue()) {
            new ReplyHeader(Status.STATUS100).SendHeader(out);
        }
        // save the sent png file to the upload folder
        WriteToFile(requestHeader);
        
        // serve the requested file like it has been a GET request. 
        // in our case it should always the uploadFile.html at this point
        HandleGet(DIR_PREFIX + requestHeader.getRequestedResource());
    }

    private void WriteToFile(InboundRequestHeader requestHeader) throws IOException {
        // uses the original filename of the sent file and saves it in the upload folder
        // additionally the current timestamp is added because with POST there should nothing be overwrite like with PUT
        File fileToSave = new File(UPLOAD_DIR + System.currentTimeMillis() + "-" + requestHeader.startPNGReadingAndFilename());
        while (fileToSave.exists()){
            // really unlikely; if the file already exists use a later timestamp
            fileToSave = new File(
                    UPLOAD_DIR + System.currentTimeMillis() + "-" + requestHeader.startPNGReadingAndFilename());
        }
        FileOutputStream os = new FileOutputStream(fileToSave);
        while (true) {
            byte[] buffer = new byte[1024];
            int len = requestHeader.readPNG(buffer);
            os.write(buffer, 0, len);
            if (len != 1024)
                break;
        }
        os.close();
    }

    private void HandlePut(InboundRequestHeader requestHeader) throws IOException {
        // send the continue header, if the client asked for it
        if (requestHeader.isExpectContinue()) {
            new ReplyHeader(Status.STATUS100).SendHeader(out);
        }

        // the name after the PUT
        String fileName = requestHeader.getRequestedResource();
        System.out.println(fileName);
        Status status;
        String fileLocationHeader;
        if (fileName != null) {
            File f = new File(DIR_PREFIX + fileName);
            if (f.exists() && f.isFile()) {
                // file already exists, so it will be updated
                status = Status.STATUS204;
                fileLocationHeader = fileName;
            } else if (!f.exists()) {
                // file does not exist so it will be created
                status = Status.STATUS201; // Morgan said we can send a 204 in both cases (created and overridden)
                fileLocationHeader = fileName;
            } else if (f.exists() && f.isDirectory()) {
                // file exists but it is directory, a index.html will be created in this
                // directory with the content of the sent file. The new location will be seen
                // in the content-location header field
                f = new File(f, "index.html");
                if (f.exists()) {
                    status = Status.STATUS204;
                } else {
                    status = Status.STATUS201;
                }
                fileLocationHeader = Paths.get(DIR_PREFIX).relativize(f.toPath()).toString();
            } else {
                // not reachable
                throw new RuntimeException("this should not be reachable");
            }
            FileOutputStream os = new FileOutputStream(f.getPath());

            // save the sendet file on the server machine
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

            // send the header. The reason why sending the header is delayed until now is that if something
            // went wrong during the saving of the file, we can still send the 500
            new ReplyHeader(status, fileLocationHeader).SendHeader(out);
        } else {
            send500();
        }
    }

    @Override
    public void run() {
        AcceptIncomingConnection();
    }
}
