package dv201.httpserver;


import dv201.httpserver.enums.ContentType;
import dv201.httpserver.enums.Status;

import java.io.PrintWriter;

public class ReplyHeader {

//    public static final String STATUS200 = "HTTP/1.1 200 OK";
//    private static final String STATUS404 = "HTTP/1.1 404 NOT FOUND";
//    private static final String STATUS500 = "HTTP/1.1 500 INTERNAL SERVER ERROR";
//    private static final String STATUS302 = "HTTP/1.1 302 FOUND";
//    private static final String STATUS403 = "HTTP/1.1 403 FORBIDDEN";
//    private static final String CONTENT_TYPE_HTML = "Content-Type: text/html;charset=UTF-8";
//    private static final String CONTENT_TYPE_PNG = "Content-Type: image/png";
    Status status;
    ContentType contentType;
    String location = null;

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
            throw new RuntimeException("No location setted for 302 reply");
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
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }
}
