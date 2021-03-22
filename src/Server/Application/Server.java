package Server.Application;

import Server.BusinessLogic.FacilitiesBookingSystem;
import Server.BusinessLogic.IBookingSystem;
import Server.DataAccess.IServerDB;
import Server.DataAccess.ServerDB;
import Server.Entities.Concrete.CallbackTestFacility;
import Server.Entities.IObservable;

import java.io.IOException;
import java.net.*;

public class Server {
    private DatagramSocket socket;
    private IServerDB serverDB;
    private IBookingSystem facilitiesBookingSystem;
    private IObservable facility; // TODO: Remove after testing phase
    private IRequestCache cache;

    public Server(int port) {
        try {
            System.out.println("Starting a service at port " + port);
            socket = new DatagramSocket(port);
            serverDB = new ServerDB();
            facilitiesBookingSystem = new FacilitiesBookingSystem(serverDB);
            facility = new CallbackTestFacility("Test Facility");
            cache = new ServerCache();
            printIp();
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

            // Test callback
            long expiration = System.currentTimeMillis() + 30000;
            facility.addObservationSession(clientAddress, clientPort, expiration);
            facility.sendUpdateToObservingClients(socket);

            // Pseudo server response
            String data = "Message from server";
            buffer = data.getBytes();
            
            DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
            socket.send(response);



        }
    }

    /**
     * Prints private IP address
     * @throws IOException
     */
    private void printIp () throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("google.com", 80)); // Creates a pseudo connection to return the private IP address. Reference: https://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java
        System.out.println("Server started on: " + socket.getLocalAddress());
    }
}