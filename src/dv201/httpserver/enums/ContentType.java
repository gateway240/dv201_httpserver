package dv201.httpserver.enums;

public enum ContentType{ HTML("Content-Type: text/html;charset=UTF-8"),
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
