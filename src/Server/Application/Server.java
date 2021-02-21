package Server.Application;

import Server.BusinessLogic.FlightBookingSystem;
import Server.DataAccess.IServerDB;
import Server.DataAccess.ServerDB;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private DatagramSocket socket;
    private IServerDB serverDB;
    private FlightBookingSystem flightBookingSystem;

    public Server(int port) throws SocketException {
        try {
            System.out.println("Starting a service at port " + port);
            socket = new DatagramSocket(port);
            serverDB = new ServerDB();
            flightBookingSystem = new FlightBookingSystem(serverDB);
        } catch (SocketException e){
            System.out.println(e);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) {
        try {
            int port = 17;
            Server server = new Server(port);
            server.service();
        } catch (SocketException ex) {
            System.out.println("Socket error: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }

    /**
     * Starts udp service
     * @throws IOException
     */
    private void service() throws IOException {
        while (true) {

            byte[] buffer = new byte[256];

            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
            socket.receive(request);

            // Pseudo client request
            System.out.println(request);

            // TODO: Unmarshall the client request
            // TODO: Process the client request and call the business logic layer

            InetAddress clientAddress = request.getAddress();
            int clientPort = request.getPort();

            // Pseudo server response
            String data = "Message from server";
            buffer = data.getBytes();

            DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
            socket.send(response);
        }
    }
}