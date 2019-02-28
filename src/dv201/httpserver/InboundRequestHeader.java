package dv201.httpserver;

import dv201.httpserver.enums.RequestType;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dv201.httpserver.HTTPServerLib.FILE_CONTENTS;

class InboundRequestHeader {
    public static final int BUFF_SIZE = 4096;

    private RequestType requestType;
    private byte[][] rawPayload;
    private int passNum;
    // private byte[] filePayload;
    private int contentLength;
    private boolean expectContinue = false;
    private String requestedResource;
    private int fileStart = 0;
    private final Map<String, String> payload = new HashMap<>();
    public String header;
    private InputStream inpStr;
    private String boundary;
    DataInputStream inDataread;

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
        header = header.trim();
        System.out.println("###header");
        System.out.println(header);
        System.out.println("###header ende");
        ParseHeader();
    }

    private static String readLine(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int c;
        for (c = inputStream.read(); c != '\n' && c != -1; c = inputStream.read()) {
            byteArrayOutputStream.write(c);
        }
        if (c == -1 && byteArrayOutputStream.size() == 0) {
            return null;
        }
        String line = byteArrayOutputStream.toString("UTF-8");
        return line.trim();
    }


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

    public int readPNG(byte[] buffer) throws IOException {
        String usedBound = "--" + boundary;
        int len = inpStr.read(buffer);
        for (int i = 0; i < len; i++) {
            if (i + usedBound.length() < buffer.length - 1) {
                byte[] nextChars = Arrays.copyOfRange(buffer, i, i + usedBound.length());
                if (new String(nextChars).equals(usedBound)) {
                    // ende
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

    private void ParsePayload(byte[] inboundPayload) {
        String input = new String(inboundPayload);
        // Split each key/value pair in the body of the payload
        StringTokenizer stAND = new StringTokenizer(input, "&");
        while (stAND.hasMoreTokens()) {
            // Split each key/value pair into the key and the value
            String currentToken = stAND.nextToken();
            StringTokenizer stEQUAL = new StringTokenizer(currentToken, "=");
            // If there is a valid key and value (POST requests)
            if (stEQUAL.countTokens() == 2) {
                // Retrieve the key and value and place them into a hashmap
                String key = stEQUAL.nextToken();
                String value = stEQUAL.nextToken();
                payload.put(key, value);
            } else if (stEQUAL.countTokens() == 1) {
                // If there is only one token it is a PUT request and the key is already defined
                // in the program
                payload.put(FILE_CONTENTS, stEQUAL.nextToken());
            }

        }

    }

    private void ParseHeader() {
        System.out.println("Parse Header: ");
        System.out.println(header);
        Scanner scanner = new Scanner(header);
        if (!scanner.hasNextLine()){
            scanner.close();
            throw new RuntimeException("A strange error, because the header is just empty");
        }
        String line = scanner.nextLine();
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
                // System.out.println("Line: " + line);
                String[] lineSep = headerLine.split(": ");
                contentLength = Integer.parseInt(lineSep[1]);
                // System.out.println("ConLen: "+ lineSep[1]);
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

    public Map<String, String> getPayload() {
        return payload;
    }

    public String getRequestedResource() {
        return requestedResource;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    // public byte[] getFilePayload() {
    // return filePayload;
    // }

    public int getContentLength() {
        return contentLength;
    }

    public boolean isExpectContinue() {
        return expectContinue;
    }

    public byte[][] getRawPayload() {
        return rawPayload;
    }

    public int getPassNum() {
        return passNum;
    }

    public int getFileStart() {
        return fileStart;
    }
}
