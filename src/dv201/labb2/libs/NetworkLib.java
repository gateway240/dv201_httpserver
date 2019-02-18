package dv201.labb2.libs;
//Abstract class for UDP and TCP networking
public abstract class NetworkLib {
    public static final int BUFSIZE= 1024;
    public static final int MYPORT= 0;
    public static final String MSG= "An Echo Message!";
    protected String ipAddress;
    protected Integer port = MYPORT;
    protected Integer bufSize = BUFSIZE;
    protected Integer msgTRate = 0;
    protected byte[] buf;


    public NetworkLib() {
    }

    public abstract boolean connect();
    public abstract boolean send(String msg);
    public abstract String receive();
    public abstract boolean disconnect();
    public abstract boolean message(String msg);

    //Sends as many messages as possible with the given transfer rate in one second
    public boolean send(String msg, Integer tRate)   {
        long start = System.currentTimeMillis();
        long end = start + 1000; // 60 seconds * 1000 ms/sec
        Integer msgSent = 0;
        while (System.currentTimeMillis() < end)
        {
            message(msg);
            msgSent++;
            try {
                Thread.sleep(1000 / tRate);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        System.out.println("Sent: "+ msgSent + " messages in one second with a transfer rate of " + tRate +" messages/second");
        System.out.println((tRate - msgSent) + " messages remaining");
        return false;
    }

    public byte[] getBuf() {
        return buf;
    }

    public void setBuf(Integer bufSize) {
        this.buf = new byte[bufSize];
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getBufSize() {
        return bufSize;
    }

    public void setBufSize(Integer bufSize) {
        this.bufSize = bufSize;
    }

    public Integer getMsgTRate() {
        return msgTRate;
    }

    public void setMsgTRate(Integer msgTRate) {
        this.msgTRate = msgTRate;
    }
}
