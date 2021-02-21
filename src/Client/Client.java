package Client;

import java.io.*;
import java.net.*;

// Name : Lim Tian Jun Collin
// Group: SSP2
// IP Address:
public class Client {

    public static void main(String[] args){
        String hostname = "127.0.0.1";
        int port = 17;

        // TODO: Establish a connection with the server address

        try {
            // Establishing a connection with the socket
            InetAddress address = InetAddress.getByName(hostname);
            DatagramSocket socket = new DatagramSocket();
            socket.connect(new InetSocketAddress(address, port));

            // Preparing the request string
            String requestString = "Lim Tian Jun Collin, SSP2, " + socket.getLocalAddress();
            System.out.println(requestString);

            // Sending the request
            DatagramPacket request = new DatagramPacket(requestString.getBytes(), requestString.getBytes().length);
            socket.send(request);

            // Receiving the reply
            byte[] buffer = new byte[512];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            socket.receive(reply);

            String quote = new String(buffer, 0, reply.getLength());
            System.out.println(quote);

        } catch (SocketTimeoutException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }
}