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
    InputStreamReader inread;

    public InboundRequestHeader(InputStream in) throws IOException {
        inpStr = in;
        inread = new InputStreamReader(in, "ISO-8859-1");

        StringBuilder stringbuld = new StringBuilder();
        while (true) {
            int r = inread.read();
            if (r == -1)
                break;
            // System.out.println((char) r);
            stringbuld.append((char) r);

            if (stringbuld.toString().endsWith("\n\r\n"))
                break;
        }
        header = stringbuld.toString();
        ParseHeader();
    }


    public String startPNGReadingAndFilename() throws IOException {
        StringBuilder stringbuild = new StringBuilder();
        while (true) {
            int r = inread.read();
            if (r == -1)
                break;
            // System.out.println((char) r);
            stringbuild.append((char) r);

            if (stringbuild.toString().endsWith("\n\r\n"))
                break;
        }

        Pattern p = Pattern.compile("filename=\"([^\"]*)\"");
        Matcher m = p.matcher(stringbuild.toString());
        m.find();
        return(m.group(1));
        
    }

    public int readPNG(byte[] buffer) throws IOException {
        char[] chbuf = new char[buffer.length];
        String usedBound = "--" + boundary;
        int len = inread.read(chbuf);
        for (int i = 0; i < len; i++) {
            if (i + usedBound.length() < chbuf.length - 1) {
                char[] nextChars = Arrays.copyOfRange(chbuf, i, i + usedBound.length());
                if (new String(nextChars).equals(usedBound)) {
                    // ende
                    len = i;
                    break;
                }
            }
            buffer[i] = (byte) chbuf[i];
        }

        return len;
    }

    public int readPNGPut(byte[] buffer) throws IOException {
        char[] chbuf = new char[buffer.length];
        int len = inread.read(chbuf);
        for (int i = 0; i < len; i++) {
            buffer[i] = (byte) chbuf[i];
        }

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
        Scanner scanner = new Scanner(header);
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
