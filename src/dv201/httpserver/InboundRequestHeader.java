package dv201.httpserver;


import dv201.httpserver.enums.RequestType;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

public class InboundRequestHeader {
    private RequestType requestType;
    private String requestedResource;

    public InboundRequestHeader(BufferedReader in) {
        HandleInputStream(in);
    }
    private void HandleInputStream(BufferedReader buffer){
        try {

            String line = buffer.readLine();

            ParseHeader(line);
        } catch (IOException e) {
            System.err.println("Unable to Read Input Stream");
            e.printStackTrace();
        }
    }
    public String readAllLines(BufferedReader reader) throws IOException {
        StringBuilder content = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            content.append(line);
            content.append(System.lineSeparator());
        }
//        reader.close();
        return content.toString();
    }

    private void ParseHeader(String inboundHeader) {
        String[] words = inboundHeader.split("\\s");
        System.out.println(words[0]);
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

    public String getRequestedResource() {
        return requestedResource;
    }

    public RequestType getRequestType() {
        return requestType;
    }

}
