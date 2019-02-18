package dv201.labb2.libs;

import java.io.IOException;
import java.net.*;
import java.util.regex.Pattern;
//UDP Implementation of network lib
public class UDPClientLib extends NetworkLib{
    private DatagramSocket socket;
    private SocketAddress remoteBindPoint;
    //Regular expression to check valid ip address
    private static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public static boolean validate(final String ip) {
        return PATTERN.matcher(ip).matches();
    }

    @Override
    public boolean connect() {

        /* Create socket */
        socket= null;
        try {
            socket = new DatagramSocket(null);
        } catch (SocketException e) {
            System.err.println("Unable to create socket object ");
            e.printStackTrace();
            return false;
        }

        /* Create local endpoint using bind() */
        SocketAddress localBindPoint= new InetSocketAddress(MYPORT);
        try {
            socket.bind(localBindPoint);
        } catch (SocketException e) {
            System.err.println("Unable to create localBindPoint");
            e.printStackTrace();
            return false;
        }

        /* Create remote endpoint */
        if(validate(getIpAddress())){
            remoteBindPoint=
                    new InetSocketAddress(getIpAddress(),getPort());
        }
        else{
            System.err.println("Invalid ip Address entered");
            return false;
        }

        return true;
    }
    @Override
    public boolean send(String msg) {

        /* Create datagram packet for sending message */
        if (buf.length< msg.length()){
            System.err.println("Buffer is not large enough to send message");
        }
        DatagramPacket sendPacket=
                null;
        try {
            sendPacket = new DatagramPacket(msg.getBytes(),
                    MSG.length(),
                    remoteBindPoint);
        } catch (SocketException e) {
            System.err.println("Unable to create DatagramPacket");
            e.printStackTrace();
            return false;
        }
        try {
            socket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Unable to send packet");
            return false;
        }
        return true;
    }

    @Override
    public String receive() {
        /* Create datagram packet for receiving echoed message */
        DatagramPacket receivePacket= new DatagramPacket(buf, buf.length);
        try {
            socket.receive(receivePacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String receivedString=
                new String(receivePacket.getData(),
                        receivePacket.getOffset(),
                        receivePacket.getLength());
        if (receivedString.compareTo(NetworkLib.MSG) == 0)
            System.out.printf("%d bytes sent and received\n", receivePacket.getLength());
        else
            System.out.printf("Sent and received msg not equal!\n");

        return receivedString;
    }

    @Override
    public boolean disconnect() {
        socket.close();
        return true;
    }

    @Override
    public boolean message(String msg) {
        send(msg);
        receive();
        return true;
    }

}
