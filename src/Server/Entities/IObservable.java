package Server.Entities;

import java.io.IOException;
import java.net.InetAddress;

public interface IObservable {
    /**
     * Adds a client to a list of clients that are observing the activities of this interface
     * @param clientAddress: the IP address of the client
     * @param clientPort: the port number of the client socket
     * @param expirationTimeStamp: the UNIX timestamp that the observation will expire
     */
    void addObservationSession(InetAddress clientAddress, int clientPort, long expirationTimeStamp);

    /**
     * Goes through the list of observing clients and sends them an update
     * @throws IOException: Sending datagram packets may throw IOException
     */
    void sendUpdateToObservingClients() throws IOException;
}
