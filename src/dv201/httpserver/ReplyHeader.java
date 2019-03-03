package dv201.httpserver;


import dv201.httpserver.enums.ContentType;
import dv201.httpserver.enums.Status;

import java.io.PrintWriter;

class ReplyHeader {

    private final Status status;
    private ContentType contentType = null;
    private String location = null;
    private String contentLocation = null;
    private Long contentLength = null;

    public ReplyHeader(Status status, ContentType contentType, long contentLength) {
        this.contentLength = contentLength;
        this.status = status;
        this.contentType = contentType;
    }

    public ReplyHeader(Status status) {
        this.status = status;
    }

    public ReplyHeader(Status status, String location) {
        this.status = status;
        if (status == Status.STATUS302){
            this.location = location;
        }else{
            this.contentLocation = location;
        }
        
    }

    public void SendHeader(PrintWriter out){
        if (status == Status.STATUS302 && location == null){
            throw new RuntimeException("No location set for 302 reply");
        }

        out.println(getStatus().toString());

        if (location != null) {
            out.println("Location: " + location);
        }
        if (contentType != null){
            out.println(getContentType().toString());
        }        
        if (contentLocation!= null) {
            out.println("Content-Location: " + contentLocation);
        }
        if (contentLength != null){
            out.println("Content-Length: " + contentLength);
        }

        out.println();
        out.flush();

    }
    private Status getStatus() {
        return status;
    }

    private ContentType getContentType() {
        return contentType;
    }

}
