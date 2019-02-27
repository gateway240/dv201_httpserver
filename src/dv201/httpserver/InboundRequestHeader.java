package dv201.httpserver;


import dv201.httpserver.enums.RequestType;

import java.io.*;
import java.util.*;

import static dv201.httpserver.HTTPServerLib.FILE_CONTENTS;

class InboundRequestHeader {


    private RequestType requestType;
    private byte[] rawPayload;
    private byte[] filePayload;
    private int contentLength;
    private boolean expectContinue = false;
    private String requestedResource;
    private final Map<String, String> payload = new HashMap<>();

    public InboundRequestHeader(InputStream in) {

        rawPayload = HandleInputStream(in);
    }

    private byte[] HandleInputStream(InputStream in) {
        try {

            byte[] rawPayload = toByteArray(in);

            //Get the header from the buffer and handle the request (GET,POST,or PUT)
            ParseHeader(rawPayload);
            //Get the payload and handle it (either nothing or the data for POST and PUT)
            ParsePayload(rawPayload);
            filePayload = ParseFile(rawPayload);

        } catch (IOException e) {
            System.err.println("Unable to Read Input Stream");
            e.printStackTrace();
        }
        return rawPayload;
    }

    public static byte[] toByteArray(InputStream in) throws IOException {

        byte[] buffer = new byte[1000000];    //If you handle larger data use a bigger buffer size
        int i = in.read(buffer);
        System.out.println(i);
        System.out.println(new String(buffer));
        return buffer;
    }


    private byte[] ParseFile(byte[] bytePayload) {
        int counter = 0;
        for (int i = 0; i < contentLength; i++) {
            if (bytePayload[i] == -119) {
                break;
            }
//            char c = (char) bytePayload[i];
//            System.out.println(counter +" :c: "+c + " :b: " + bytePayload[i]);
            counter++;
            //Process char
        }
        return Arrays.copyOfRange(bytePayload, counter, bytePayload.length);

    }

    private void ParsePayload(byte[] inboundPayload) {
        String input = new String(inboundPayload);
        //Split each key/value pair in the body of the payload
        StringTokenizer stAND = new StringTokenizer(input, "&");
        while (stAND.hasMoreTokens()) {
            //Split each key/value pair into the key and the value
            String currentToken = stAND.nextToken();
            StringTokenizer stEQUAL = new StringTokenizer(currentToken, "=");
            //If there is a valid key and value (POST requests)
            if (stEQUAL.countTokens() == 2) {
                //Retrieve the key and value and place them into a hashmap
                String key = stEQUAL.nextToken();
                String value = stEQUAL.nextToken();
                payload.put(key, value);
            } else if (stEQUAL.countTokens() == 1) {
                //If there is only one token it is a PUT request and the key is already defined in the program
                payload.put(FILE_CONTENTS, stEQUAL.nextToken());
            }

        }

    }

    private void ParseHeader(byte[] inboundHeader) {
        String input = new String(inboundHeader);
        //Get the first word without a space in the header
        String[] words = input.split("\\s");
        switch (words[0]) {
            case "GET":
                requestType = RequestType.GET;
                requestedResource = words[1].substring(1);
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

        Scanner scanner = new Scanner(input);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.contains("Content-Length:")) {
//                System.out.println("Line: " + line);
                String[] lineSep = line.split(": ");
                contentLength = Integer.parseInt(lineSep[1]);
//                System.out.println("ConLen: "+ lineSep[1]);
            }
            if(line.contains("Expect:")){
                String[] lineSep = line.split(": ");
                if(lineSep[1].equals("100-continue")){
                    expectContinue = true;
                }
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

    public byte[] getRawPayload() {
        return rawPayload;
    }

    public byte[] getFilePayload() {
        return filePayload;
    }

    public int getContentLength() {
        return contentLength;
    }

    public boolean isExpectContinue() {
        return expectContinue;
    }
}
