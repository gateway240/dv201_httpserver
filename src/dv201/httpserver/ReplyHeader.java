package dv201.httpserver;


import dv201.httpserver.enums.ContentType;
import dv201.httpserver.enums.Status;

import java.io.PrintWriter;

class ReplyHeader {

    private final Status status;
    private final ContentType contentType;
    private String location = null;

    public ReplyHeader(Status status, ContentType contentType) {
        this.status = status;
        this.contentType = contentType;
    }

    public ReplyHeader(Status status, ContentType contentType, String location)  {
        this.status = status;
        this.contentType = contentType;
        this.location = location;
    }

    public void SendHeader(PrintWriter out){
        if (status == Status.STATUS302 && location == null){
            throw new RuntimeException("No location set for 302 reply");
        }
        out.println(getStatus().toString());
        if (status == Status.STATUS302) {
            out.println("Location: " + location);
        }else{
            out.println(getContentType().toString());
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
