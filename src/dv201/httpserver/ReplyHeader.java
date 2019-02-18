package dv201.httpserver;


import java.io.PrintWriter;

enum ContentType{ HTML("Content-Type: text/html;charset=UTF-8"),
    PNG ("Content-Type: image/png");

    private final String text;
    ContentType(final String text){
        this.text = text;
    }
    @Override
    public String toString(){
        return text;
    }
}
enum Status{ STATUS200("HTTP/1.1 200 OK"),
    STATUS302("HTTP/1.1 302 FOUND"),
    STATUS403("HTTP/1.1 403 FORBIDDEN"),
    STATUS404("HTTP/1.1 404 NOT FOUND"),
    STATUS500("HTTP/1.1 500 INTERNAL SERVER ERROR");

private final String text;
Status(final String text){
    this.text = text;
}
@Override
    public String toString(){
    return text;
}
}

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

    public ReplyHeader(Status status, ContentType contentType) {
        this.status = status;
        this.contentType = contentType;
    }
    public void SendHeader(PrintWriter out){
        out.println(getStatus().toString());
        out.println(getContentType().toString());
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
