package Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.Callable;

public class TimeoutWorker implements Callable<String> {
    DatagramSocket socket;
    String request;

    public TimeoutWorker(DatagramSocket socket, String request) {
        this.socket = socket;
        this.request = request;
    }

    @Override
    public String call() throws IOException {
        // Sending the request
        DatagramPacket requestPacket = new DatagramPacket(request.getBytes(), request.getBytes().length);
        socket.send(requestPacket);
        // Receiving the reply
        byte[] buffer = new byte[512];
        DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
        socket.receive(reply);
        // Unmarshall response
        return new String(buffer, 0, reply.getLength()); // TODO: Properly unmarshall response
    }
}
