package dv201.labb2.libs;

import java.io.*;
import java.net.*;
//TCP Implementation of networking lib
public class TCPClientLib extends NetworkLib {
    private Socket socket;


    @Override
    public boolean connect() {

        /* Create socket */
        try {
            socket = new Socket(getIpAddress(), getPort());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public boolean send(String msg) {

        try {
//            byte[] data = msg.getBytes();
            OutputStream output = socket.getOutputStream();
//            output.write(data);
            PrintWriter writer = new PrintWriter(output, true);
            writer.println(msg);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    @Override
    public String receive() {
        /* Create datagram packet for receiving echoed message */
        String receivedString = null;
        try {
            InputStream input = socket.getInputStream();
            input.read(buf);
            receivedString = new String(buf).replaceAll("[\\n\\r\\u0000]", ""); // Replace extra line ending characters in buffer
            System.out.println("FROM Server: " + receivedString);
            if (!receivedString.equals(NetworkLib.MSG)){
                System.out.println("Sent and received msg not equal!");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return receivedString;
    }

    @Override
    public boolean disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean message(String msg) {
        connect();
        send(msg);
        receive();
        disconnect();
        return false;
    }


}
