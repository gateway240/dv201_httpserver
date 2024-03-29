package dv201.httpserver;

class HTTPServerLib {
    public static final String ARGS_USAGE = "usage: MyPort\n";

    public static Integer ParsePort(String port){
        try {
            Integer myPort = Integer.valueOf(port);
            if(myPort < 1 || myPort > 65535){
                System.err.println("your Port is not between 1 and 65535");
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
    public static void FatalError(String errorMsg){
        System.err.println(errorMsg);
        System.exit(1);
    }


}
