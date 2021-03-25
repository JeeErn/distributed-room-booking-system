package Server.Application;

import Client.ClientRequest;
import Marshaller.Marshallable;
import Server.BusinessLogic.FacilitiesBookingSystem;
import Server.BusinessLogic.IBookingSystem;
import Server.DataAccess.IServerDB;
import Server.DataAccess.ServerDB;
import Server.Exceptions.*;

import java.io.IOException;
import java.net.*;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

public class Server {
    private DatagramSocket socket;
    private IServerDB serverDB;
    private IBookingSystem facilitiesBookingSystem;
    private IRequestCache cache;



    public Server(int port) {
        try {
            System.out.println("Starting a service at port " + port);
            socket = new DatagramSocket(port);
            serverDB = new ServerDB();
            facilitiesBookingSystem = new FacilitiesBookingSystem(serverDB);
            cache = new ServerCache();
            printIp();
        } catch (Exception e){
            e.printStackTrace();
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
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts udp service
     * @throws IOException if unable to connect to socket
     */
    private void service() throws IOException, IllegalAccessException {
        while (true) {

            byte[] buffer = new byte[256];

            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
            socket.receive(request);

            // Unmarshall the client request
            byte[] bytesArr = request.getData();
            ClientRequest clientRequest = Marshallable.unmarshall(bytesArr, ClientRequest.class);

            int functionCode = clientRequest.getRequestMethod(); // Warning, this might produce null pointer exception
            List<String> arguments = clientRequest.getArguments();
            String clientRequestId = Integer.toString(clientRequest.getId()) + request.getSocketAddress();

            // TODO: explore using enums
            String responseMessage;
            switch (functionCode) {
                case 0:
                default:
                    responseMessage = handleHeartbeat();
                    break;
                case 2:
                    responseMessage = handleGetAvailability(arguments);
                    break;
                case 3:
                    responseMessage = handleCreateBooking(request, clientRequestId, arguments);
                    break;
                case 4:
                    responseMessage = handleUpdateBooking(request, clientRequestId, arguments);
                    break;
                case 5:
                    responseMessage = handleAddObservingClient(request, clientRequestId, arguments);
                    break;

            }

            ServerResponse serverResponse = new ServerResponse(responseMessage);

            InetAddress clientAddress = request.getAddress();
            int clientPort = request.getPort();

            // Pseudo server response
            buffer = serverResponse.marshall();
            DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
            socket.send(response);
        }
    }

    // ===================================
    // Handler functions
    // ===================================
    private String handleGetAvailability(List<String> arguments) {
        try {
            String facilityName = arguments.get(0);
            System.out.println("server's facility name: " + facilityName);
            List<Integer> days = Arrays.asList(Integer.valueOf(arguments.get(1)), Integer.valueOf(arguments.get(1)));
            String availability = facilitiesBookingSystem.getAvailability(facilityName, days);
            return "Facility availability: " + availability;
        } catch (FacilityNotFoundException e) {
            return "404: Facility not found";
        } catch (ParseException e) {
            return "500: Error occurred retrieving availability";
        }
    }

    private String handleCreateBooking(DatagramPacket request, String clientRequestId, List<String> arguments) {
        try {
            if(cache.hasRequest(clientRequestId)){
                return cache.getResponse(clientRequestId);
            }

            String facilityName = arguments.get(0);
            String startDateTime = arguments.get(1);
            String endDateTime = arguments.get(2);
            String clientId = generateClientIdFromOrigin(request);
            String confirmationId = facilitiesBookingSystem.createBooking(facilityName, startDateTime, endDateTime, clientId, socket);
            String serverResponse = "Booking confirmation ID: " + confirmationId;
            cache.addRequest(clientRequestId, serverResponse);
            return serverResponse;
        } catch (InvalidDatetimeException | ParseException e) {
            return "400: Invalid datetime provided";
        } catch (TimingUnavailableException e) {
            return "409: Booking time not available";
        } catch (FacilityNotFoundException e) {
            return"404: Facility not found";
        }
    }

    private String handleUpdateBooking(DatagramPacket request, String clientRequestId, List<String> arguments) {
        try {
            if(cache.hasRequest(clientRequestId)){
                return cache.getResponse(clientRequestId);
            }

            String confirmationId = arguments.get(0);
            System.out.println("server confimration id: " + confirmationId);
            String clientId = generateClientIdFromOrigin(request);
            int offset = Integer.parseInt(arguments.get(1));
            System.out.println("offset server: " + offset);
            facilitiesBookingSystem.updateBooking(confirmationId, clientId, offset, socket);

            String serverResponse =  "Booking updated successfully";
            cache.addRequest(clientRequestId, serverResponse);
            return serverResponse;
        } catch (WrongClientIdException | BookingNotFoundException e) {
            return "404: Invalid confirmation ID";
        } catch (InvalidDatetimeException e) {
            return "400: Invalid offset provided";
        } catch (TimingUnavailableException e) {
            return "409: New booking time not available";
        } catch (ParseException e) {
            return "500: Error occurred updating booking";
        }
    }

    private String handleAddObservingClient(DatagramPacket request, String clientRequestId, List<String> arguments) {
        try {
            if(cache.hasRequest(clientRequestId)){
                return cache.getResponse(clientRequestId);
            }
            String facilityName = arguments.get(0);
            InetAddress clientAddress = request.getAddress();
            int clientPort = request.getPort();
            int durationInMin = Integer.parseInt(arguments.get(1));
            facilitiesBookingSystem.addObservingClient(facilityName, clientAddress, clientPort, durationInMin);

            String serverReponse =  "Successfully added to observing list";
            cache.addRequest(clientRequestId, serverReponse);
            return serverReponse;
        } catch (FacilityNotFoundException e) {
            return "404: Facility not found";
        }
    }

    private String handleHeartbeat() {
        return "Request received by server";
    }

    private String generateClientIdFromOrigin(DatagramPacket request) {
        return request.getAddress().getHostAddress() + ":" + request.getPort();
    }

    /**
     * Prints private IP address
     * @throws IOException if unable to connect to socket
     */
    private void printIp () throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("google.com", 80)); // Creates a pseudo connection to return the private IP address. Reference: https://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java
        System.out.println("Server started on: " + socket.getLocalAddress());
    }
}