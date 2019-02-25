package dv201.httpserver;


import dv201.httpserver.enums.RequestType;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import static dv201.httpserver.HTTPServerLib.FILE_CONTENTS;

class InboundRequestHeader {



    private RequestType requestType;
    private String requestedResource;
    private final Map<String,String> payload = new HashMap<>();

    public InboundRequestHeader(BufferedReader in) {
        HandleInputStream(in);
    }
    private void HandleInputStream(BufferedReader buffer){
        try {
            //Get the header from the buffer and handle the request (GET,POST,or PUT)
            String header = getHeader(buffer);
            ParseHeader(header);

            //Get the payload and handle it (either nothing or the data for POST and PUT)
            String payload = getPayload(buffer);
            ParsePayload(payload);

        } catch (IOException e) {
            System.err.println("Unable to Read Input Stream");
            e.printStackTrace();
        }
    }
    private String getPayload(BufferedReader buffer) throws IOException {
        //code to read the post payload data
        StringBuilder payload = new StringBuilder();
        while(buffer.ready()){
            payload.append((char) buffer.read());
        }
        String payloadResult = payload.toString();
        System.out.println("Payload data is: "+ payloadResult);
        return payloadResult;
    }
    private String getHeader(BufferedReader reader) throws IOException {
        //Read the header until the line is empty
        String line;
        StringBuilder request = new StringBuilder();
        while ((line = reader.readLine()) != null){
            request.append(line).append("\r\n");
            if (line.isEmpty()) {
                break;
            }
        }
        String header = request.toString();
        System.out.println("Header: \n" + header);
        return header;
    }
    private void ParsePayload(String inboundPayload){
        //Split each key/value pair in the body of the payload
        StringTokenizer stAND = new StringTokenizer(inboundPayload, "&");
        while (stAND.hasMoreTokens()) {
            //Split each key/value pair into the key and the value
            String currentToken = stAND.nextToken();
            StringTokenizer stEQUAL = new StringTokenizer(currentToken, "=");
            //If there is a valid key and value (POST requests)
            if(stEQUAL.countTokens() == 2){
                //Retrieve the key and value and place them into a hashmap
                String key = stEQUAL.nextToken();
                String value = stEQUAL.nextToken();
                payload.put(key,value);
            }
            else if (stEQUAL.countTokens() == 1){
                //If there is only one token it is a PUT request and the key is already defined in the program
                payload.put(FILE_CONTENTS,stEQUAL.nextToken());
            }

        }
    }
    private void ParseHeader(String inboundHeader) {
        //Get the first word without a space in the header
        String[] words = inboundHeader.split("\\s");
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

}
