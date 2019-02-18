package dv201.httpserver;

public class HTTPServerLib {

    public static Integer ParsePort(String port){
        try {
            Integer myPort = Integer.valueOf(port);
            if(myPort <= 0 || myPort > 65535){
                System.err.printf("your Port is not between 0 and 65535\n");
                return 0;
            }
           else{
               return myPort;
            }
        } catch (Exception e) {
            System.err.println("Problem occurred while processing your serverport input: "+ port);
            return 0;
        }
    }
    public static String GenerateOKHeader(){
        String OKHeader = "HTTP/1.1 200 OK\n" +
                "Content-Type: text/html;charset=UTF-8\n\n";
        return OKHeader;
    }


}
