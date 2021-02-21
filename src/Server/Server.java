package Server;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private DatagramSocket socket;
    private List<String> listQuotes = new ArrayList<String>();
    private Random random;

    public Server(int port) throws SocketException {
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e){
            System.out.println(e);
        }
        random = new Random();
    }

    public static void main(String[] args) {
        try {
            int port = 17;
            System.out.println("Starting a port at port " + port);
            Server server = new Server(port);
            server.service(port);
        } catch (SocketException ex) {
            System.out.println("Socket error: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }

    private void service(int port) throws IOException {
        while (true) {

            byte[] buffer = new byte[256];

            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
            socket.receive(request);

            System.out.println(request);

            InetAddress clientAddress = request.getAddress();
            int clientPort = request.getPort();

            String data = "Message from server";
            buffer = data.getBytes();

            DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
            socket.send(response);
        }
    }
}