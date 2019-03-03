package dv201.httpserver;

import dv201.httpserver.enums.RequestType;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class InboundRequestHeader {
    private RequestType requestType;
    private int contentLength;
    private boolean expectContinue = false;
    private String requestedResource;
    public String header;
    private InputStream inpStr;
    // boundary for the post body
    private String boundary;

    public InboundRequestHeader(InputStream in) throws IOException {
        inpStr = in;

        header = "";
        while (true) {
            String line = readLine(in);
            header += line + "\n"; 

            if (line == null || line.equals("") || line.equals("\r") || line.equals("\n") || line.equals("\n\r")){
                break;
            }
        }
        // trim removes the problems you have with \r and \n on different os's
        header = header.trim();
        ParseHeader();
    }

    private static String readLine(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int c;
        for (c = inputStream.read(); c != '\n' && c != -1; c = inputStream.read()) {
            byteArrayOutputStream.write(c);
        }
        //-i indicates that there is no more left to be read
        if (c == -1 && byteArrayOutputStream.size() == 0) {
            return null;
        }
        String line = byteArrayOutputStream.toString("UTF-8");
        return line.trim();
    }


    // reads the input stream to the position where the picture starts and extracts the filename
    public String startPNGReadingAndFilename() throws IOException {
        String pictureHeader = "";
        while (true) {
            String line = readLine(inpStr);
            pictureHeader += line + "\n";

            if (line == null || line.equals("") || line.equals("\r") || line.equals("\n") || line.equals("\n\r")) {
                break;
            }
        }
        pictureHeader = pictureHeader.trim();

        Pattern p = Pattern.compile("filename=\"([^\"]*)\"");
        Matcher m = p.matcher(pictureHeader);
        m.find();
        return(m.group(1));
        
    }

    // reads the input stream until the boundary occurs
    public int readPNG(byte[] buffer) throws IOException {
        String usedBound = "--" + boundary;
        int len = inpStr.read(buffer);
        for (int i = 0; i < len; i++) {
            if (i + usedBound.length() < buffer.length - 1) {
                byte[] nextChars = Arrays.copyOfRange(buffer, i, i + usedBound.length());
                if (new String(nextChars).equals(usedBound)) {
                    // end
                    len = i;
                    break;
                }
            }
        }
        return len;
    }

    public int readPNGPut(byte[] buffer) throws IOException {
        int len = inpStr.read(buffer);
        return len;
    }


    // extracts the important information out of the header
    private void ParseHeader() {
        Scanner scanner = new Scanner(header);
        if (!scanner.hasNextLine()){
            scanner.close();
            throw new RuntimeException("A strange error, because the header is just empty");
        }
        String line = scanner.nextLine();
        System.out.println(line);
        String[] words = line.split("\\s");
        switch (words[0]) {
        case "GET":
            requestType = RequestType.GET;
            break;

        case "POST":
            requestType = RequestType.POST;
            break;

        case "PUT":
            requestType = RequestType.PUT;
            break;

        default:
            break;
        }
        requestedResource = words[1].substring(1);

        while (scanner.hasNextLine()) {
            String headerLine = scanner.nextLine();
            if (headerLine.contains("Content-Length:")) {
                String[] lineSep = headerLine.split(": ");
                contentLength = Integer.parseInt(lineSep[1]);
            }
            if (headerLine.contains("Expect:")) {
                String[] lineSep = headerLine.split(": ");
                if (lineSep[1].equals("100-continue")) {
                    expectContinue = true;
                }
            }
            if (headerLine.contains("boundary=")) {
                String[] lineSep = headerLine.split("boundary=");
                boundary = lineSep[lineSep.length - 1];
            }
        }
        scanner.close();
    }

    public String getRequestedResource() {
        return requestedResource;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public int getContentLength() {
        return contentLength;
    }

    public boolean isExpectContinue() {
        return expectContinue;
    }
}
