/*
  UDPEchoClient.java
  A simple echo client with no error handling
*/

package dv201.labb2;
import dv201.labb2.libs.NetworkLib;
import dv201.labb2.libs.TCPClientLib;
import dv201.labb2.libs.UDPClientLib;

public class TCPEchoClient {


    //Proper usage: server_name port [optional]: buffer_size message_transfer_rate
    public static void main(String[] args) {
		TCPClientLib tcpClient = new TCPClientLib();

		String ipAddress;
		Integer port;
		Integer bufSize = tcpClient.getBufSize();
		Integer msgTRate ;

	if (args.length < 2 || args.length > 4) {
	    System.err.printf("Error with command line args. Proper usage: server_name port [optional]: buffer_size message_transfer_rate \n");
	    System.exit(1);
	}
		ipAddress = args[0];
		port = Integer.valueOf(args[1]);
		tcpClient.setIpAddress(ipAddress);
		tcpClient.setPort(port);
		switch (args.length){
			case 2:
				break;
			case 3:
				bufSize = Integer.valueOf(args[2]);
				tcpClient.setBufSize(bufSize);
				break;
			case 4:
				bufSize = Integer.valueOf(args[2]);
				msgTRate = Integer.valueOf(args[3]);
				tcpClient.setBufSize(bufSize);
				tcpClient.setMsgTRate(msgTRate);
				break;
				default:
					System.err.print("Invalid number of console args.");
					break;
		}

		tcpClient.setBuf(bufSize);
	tcpClient.send(NetworkLib.MSG, tcpClient.getMsgTRate());


    }


}