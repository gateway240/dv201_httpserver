package dv201.httpserver;


import dv201.httpserver.enums.RequestType;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import static dv201.httpserver.HTTPServerLib.FILE_CONTENTS;

public class InboundRequestHeader {



    private RequestType requestType;
    private String requestedResource;
    private Map<String,String> payload = new HashMap<>();

    public InboundRequestHeader(BufferedReader in) {
        HandleInputStream(in);
    }
    private void HandleInputStream(BufferedReader buffer){
        try {
            String header = getHeader(buffer);
            ParseHeader(header);

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
        StringTokenizer stAND = new StringTokenizer(inboundPayload, "&");
        while (stAND.hasMoreTokens()) {
            String currentToken = stAND.nextToken();
//            System.out.println(currentToken);
            StringTokenizer stEQUAL = new StringTokenizer(currentToken, "=");
            if(stEQUAL.countTokens() == 2){
                String key = stEQUAL.nextToken();
                String value = stEQUAL.nextToken();
//                System.out.println(key);
                payload.put(key,value);
            }
            else if (stEQUAL.countTokens() == 1){
                payload.put(FILE_CONTENTS,stEQUAL.nextToken());
            }

        }
    }
    private void ParseHeader(String inboundHeader) {
        String[] words = inboundHeader.split("\\s");
//        System.out.println(words[0]);
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
