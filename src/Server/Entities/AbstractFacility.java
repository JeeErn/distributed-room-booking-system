package Server.Entities;

import Server.Entities.Concrete.ObservationSession;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.PriorityQueue;

public abstract class AbstractFacility implements IObservable {
    protected static String clientAddressSeparator = "&=";
    protected String facilityName;
    protected PriorityQueue<ObservationSession> observationSessions;
    protected DatagramSocket socket;

    @Override
    public void addObservationSession(InetAddress clientAddress, int clientPort, long expirationTimeStamp) {
        String client = clientAddress.getHostAddress() + clientAddressSeparator + clientPort;
        ObservationSession session = new ObservationSession(expirationTimeStamp, client);
        observationSessions.add(session);
    }

    @Override
    public void sendUpdateToObservingClients() throws IOException {
        /* Set up variables
        ioExceptCaught: indicates if IOException was thrown
        updateInfoByteBuffer: update message string in byte format
         */
        boolean ioExceptCaught = false;
        String updateMessage = "Update from: " + facilityName;
        byte[] updateInfoByteBuffer = updateMessage.getBytes();

        /*
        - Remove expired observations based on current timestamp
        - Iterate through priority queue and send message to clients
        - If IOException caught, rethrow at end of function
         */
        removeExpiredObservationSessions();
        for (ObservationSession clientSession : observationSessions) {
            try {
                sendMessageTo(clientSession.getClient(), updateInfoByteBuffer);
            } catch (IOException e) {
                ioExceptCaught = true;
                System.out.println("Failed to send to: " + clientSession.getClient());
            }
        }
        if (ioExceptCaught) throw new IOException();
    }

    private void sendMessageTo(String client, byte[] updateInfo) throws IOException {
        String[] clientInfo = client.split(clientAddressSeparator);
        assert (clientInfo.length == 2);
        InetAddress clientAddress = InetAddress.getByName(clientInfo[0]);
        int clientPort = Integer.parseInt(clientInfo[1]);

        DatagramPacket message = new DatagramPacket(updateInfo, updateInfo.length, clientAddress, clientPort);
        socket.send(message);
    }

    private void removeExpiredObservationSessions() {
        /* Removes expired observation sessions
        - Min heap based on expiry timestamp is used to form queue
        - While queue is not empty and head of queue is expired, remove head of queue
         */
        while(observationSessions.size() > 0 && observationSessions.peek().getExpirationTimeStamp() < System.currentTimeMillis()) {
            observationSessions.poll();
        }
    }
}
