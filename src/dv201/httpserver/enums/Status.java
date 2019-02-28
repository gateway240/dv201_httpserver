package dv201.httpserver.enums;

public enum Status{
    STATUS100("HTTP/1.1 100 CONTINUE"),
    STATUS200("HTTP/1.1 200 OK"),
    STATUS201("HTTP/1.1 201 CREATED"),
    STATUS204("HTTP/1.1 204 NO CONTENT"),
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
