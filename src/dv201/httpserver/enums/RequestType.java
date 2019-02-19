package dv201.httpserver.enums;

public enum RequestType {
    GET("GET"),
    POST("POST"),
    PUT("PUT");

    private final String text;

    RequestType(String text) {
        this.text = text;
    }

}
