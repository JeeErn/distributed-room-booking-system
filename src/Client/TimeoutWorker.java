package Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class TimeoutWorker implements Runnable {
    DatagramSocket socket;
    String request;
    volatile String response;

    public TimeoutWorker(DatagramSocket socket, String request) {
        this.socket = socket;
        this.request = request;
        this.response = null;
    }

    @Override
    public void run() {
        try {
            // Sending the request
            DatagramPacket requestPacket = new DatagramPacket(request.getBytes(), request.getBytes().length);
            socket.send(requestPacket);
            // Receiving the reply
            byte[] buffer = new byte[512];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            socket.receive(reply);
            // Unmarshall response
            response = new String(buffer, 0, reply.getLength()); // TODO: Properly unmarshall response
        } catch (IOException e) {
            response = "Failed to interact with server";
        }
    }
}
