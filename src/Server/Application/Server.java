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
import java.util.ArrayList;
import java.util.List;

public class Server {
    private DatagramSocket socket;
    private IServerDB serverDB;
    private IBookingSystem facilitiesBookingSystem;
    private IRequestCache cache;
    private final double SIMULATE_NETWORK_FAILURE_PROBABILITY_THRESHOLD = 0.7;


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

            String responseMessage;
            switch (functionCode) {
                case 0:
                default:
                    responseMessage = handleHeartbeat();
                    break;
                case 2:
                    responseMessage = handleGetAvailability(clientRequestId, arguments);
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
            if (shouldSimulateNetworkFailure()) {
                System.out.println("Simulating network failure: Withholding response...");
            } else {
                socket.send(response);
            }
        }
    }

    // ===================================
    // Handler functions
    // ===================================
    private String handleGetAvailability(String clientRequestId, List<String> arguments) {
        String serverResponse;
        boolean useAtMostOnce = Integer.parseInt(arguments.get(arguments.size() - 1)) == 1;
        try {
            if (useAtMostOnce && cache.hasRequest(clientRequestId)) {
                System.out.println("RequestId found in cache. Retrieving response from cache instead...");
                return cache.getResponse(clientRequestId);
            }
            String facilityName = arguments.get(0);
            List<Integer> days = new ArrayList<>();
            for (int i = 1; i < arguments.size() - 1; i++) {
                days.add(Integer.parseInt(arguments.get(i)));
            }
            String availability = facilitiesBookingSystem.getAvailability(facilityName, days);
            serverResponse = "Facility availability: " + availability;
        } catch (FacilityNotFoundException e) {
            serverResponse = "404: Facility not found";
        } catch (ParseException e) {
            serverResponse = "500: Error occurred retrieving availability";
        }
        if (useAtMostOnce) {
            cache.addRequest(clientRequestId, serverResponse);
        }
        return serverResponse;
    }

    private String handleCreateBooking(DatagramPacket request, String clientRequestId, List<String> arguments) {
        String serverResponse;
        boolean useAtMostOnce = Integer.parseInt(arguments.get(arguments.size() - 1)) == 1;
        try {
            if(useAtMostOnce && cache.hasRequest(clientRequestId)){
                System.out.println("RequestId found in cache. Retrieving response from cache instead...");
                return cache.getResponse(clientRequestId);
            }

            String facilityName = arguments.get(0);
            String startDateTime = arguments.get(1);
            String endDateTime = arguments.get(2);
            String clientId = generateClientIdFromOrigin(request);
            String confirmationId = facilitiesBookingSystem.createBooking(facilityName, startDateTime, endDateTime, clientId, socket);
            serverResponse = "Booking confirmation ID: " + confirmationId;
        } catch (InvalidDatetimeException | ParseException e) {
            serverResponse = "400: Invalid datetime provided";
        } catch (TimingUnavailableException e) {
            serverResponse = "409: Booking time not available";
        } catch (FacilityNotFoundException e) {
            serverResponse = "404: Facility not found";
        }
        if (useAtMostOnce) {
            cache.addRequest(clientRequestId, serverResponse);
        }
        return serverResponse;
    }

    private String handleUpdateBooking(DatagramPacket request, String clientRequestId, List<String> arguments) {
        String serverResponse;
        boolean useAtMostOnce = Integer.parseInt(arguments.get(arguments.size() - 1)) == 1;
        try {
            if(useAtMostOnce && cache.hasRequest(clientRequestId)){
                System.out.println("RequestId found in cache. Retrieving response from cache instead...");
                return cache.getResponse(clientRequestId);
            }

            String confirmationId = arguments.get(0);
            String clientId = generateClientIdFromOrigin(request);
            int offset = Integer.parseInt(arguments.get(1));
            facilitiesBookingSystem.updateBooking(confirmationId, clientId, offset, socket);

            serverResponse =  "Booking updated successfully";

        } catch (WrongClientIdException | BookingNotFoundException e) {
            serverResponse = "404: Invalid confirmation ID";
        } catch (InvalidDatetimeException e) {
            serverResponse = "400: Invalid offset provided";
        } catch (TimingUnavailableException e) {
            serverResponse = "409: New booking time not available";
        } catch (ParseException e) {
            serverResponse = "500: Error occurred updating booking";
        }
        if (useAtMostOnce) {
            cache.addRequest(clientRequestId, serverResponse);
        }
        return serverResponse;
    }

    private String handleAddObservingClient(DatagramPacket request, String clientRequestId, List<String> arguments) {
        String serverResponse;
        boolean useAtMostOnce = Integer.parseInt(arguments.get(arguments.size() - 1)) == 1;
        try {
            if(useAtMostOnce && cache.hasRequest(clientRequestId)){
                System.out.println("RequestId found in cache. Retrieving response from cache instead...");
                return cache.getResponse(clientRequestId);
            }
            String facilityName = arguments.get(0);
            InetAddress clientAddress = request.getAddress();
            int clientPort = request.getPort();
            int durationInMin = Integer.parseInt(arguments.get(1));
            facilitiesBookingSystem.addObservingClient(facilityName, clientAddress, clientPort, durationInMin);

            serverResponse =  "Successfully added to observing list";

        } catch (FacilityNotFoundException e) {
            serverResponse = "404: Facility not found";
        }
        if (useAtMostOnce) {
            cache.addRequest(clientRequestId, serverResponse);
        }
        return serverResponse;
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

    private boolean shouldSimulateNetworkFailure() {
        return Math.random() > SIMULATE_NETWORK_FAILURE_PROBABILITY_THRESHOLD;
    }
}