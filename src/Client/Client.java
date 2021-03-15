package Client;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Client {
    Scanner in;
    int requestNum;
    DatagramSocket socket;

    public Client() {
        in = new Scanner(System.in);
        requestNum = 1;
    }

    private String getIpFromCli() {
        System.out.println("Input server IP address: ");
        String input = in.nextLine();
        if (input.equals("default") || input.equals("localhost")) return "127.0.0.1";
        return input;
    }

    private void connectToServerRoutine(String hostname, int port) throws IOException {
        // Establishing a connection with the socket
        socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName(hostname);
        socket.connect(new InetSocketAddress(address, port));

        // Preparing the request string
        String requestString = "Sending heartbeat from: " + socket.getLocalAddress();
        // Using timeout
        System.out.println(requestString);
        String response = sendRequest(requestString);
        System.out.println(response);
        System.out.println("-- Connected --");
    }

    private void printMenu() {
        System.out.println("");
        System.out.println("=====================================");
        System.out.println("Welcome to facilities booking system!");
        System.out.println("1: View facility names");
        System.out.println("2: View availability of a facility");
        System.out.println("3: Book a facility");
        System.out.println("4: Update your booking");
        System.out.println("5: Register to observe a facility's availability");
        System.out.println("6: Exit");
        System.out.print("Please enter your choice: ");
    }

    private int getMenuChoice() {
        try {
            return Integer.parseInt(in.nextLine());
        } catch (NumberFormatException e) {
            return 6; // Default to exit
        }
    }

    private void getFacilityNames() {
        System.out.println("Facility Type | Facility Name:");
        System.out.println("Lecture Theatre | LT1");
        System.out.println("Lecture Theatre | LT2");
        System.out.println("Tennis Court | TC1");
        System.out.println("Badminton Court | BTC1");
        System.out.println("Badminton Court | BTC2");
        System.out.println("Software Lab | SWLAB1");
    }

    private void getFacilityAvailability() {
        // Get params
        System.out.println("Facility name to view availability:");
        String facilityName = in.nextLine();
        System.out.println("Enter which day(s) to view availability, separated by commas:");
        System.out.println("0 - Sunday, 6 - Saturday");
        String daysString = in.nextLine();
        List<String> days = Arrays.asList(daysString.split(","));

        // Send request
        String request = "request"; // TODO: Craft message and send to server
        String response = sendRequest(request);
        System.out.println(response);
    }

    private String sendRequest(String request) {
        TimeoutWorker requestWorker = new TimeoutWorker(socket, request);
        // While response is not logged, try to send request again
        do {
            Thread workerThread = new Thread(requestWorker);
            workerThread.start();
            try {
                workerThread.join(5000); // Wait for max 5s
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (requestWorker.response == null) {
                System.out.println("Failed to receive response. Sending request again...");
            }
        } while (requestWorker.response == null);
        return requestWorker.response;
    }

    private void mainRoutine() {
        int choice;
        do {
            printMenu();
            choice = getMenuChoice(); // Defaults to exit if illegal input provided

            switch (choice) {
                case 1:
                    getFacilityNames();
                    requestNum++;
                    break;
                case 2:
                    getFacilityAvailability();
                    break;
                case 3:
//                    bookFacility();
                    requestNum++;
                    break;
                case 4:
//                    updateBooking();
                    requestNum++;
                    break;
                case 5:
//                    observeFacility();
                    requestNum++;
                    break;
                case 6:
                    break;
                default:
                    System.out.println("Invalid option");
            }
        } while (choice != 6);
        System.out.println("Exiting application...");
        System.out.println("You may close this tab now");
    }

    public static void main(String[] args){
        Client client = new Client();
        String hostname = client.getIpFromCli();
        int port = 17;

        // TODO: Establish a connection with the server address
        try {
            client.connectToServerRoutine(hostname, port);

            // Run main routine
            client.mainRoutine();
        } catch (IOException e) {
            System.out.println("Failed to connect to server");
        }
    }
}