package Client;

import Marshaller.Marshallable;
import Server.Application.ServerResponse;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.Callable;

public class TimeoutWorker implements Callable<String> {
    DatagramSocket socket;
    ClientRequest clientRequest;

    public TimeoutWorker(DatagramSocket socket, ClientRequest clientRequest) {
        this.socket = socket;
        this.clientRequest = clientRequest;
    }

    @Override
    public String call() throws IOException, IllegalAccessException {
        System.out.println("timeout worker called");
        // Marshall clientRequest then send the request
        byte[] request = clientRequest.marshall();
        DatagramPacket requestPacket = new DatagramPacket(request, request.length);
        socket.send(requestPacket);

        System.out.println("requestPacket sent");

        // Receiving the reply
        byte[] buffer = new byte[512];
        DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
        socket.receive(reply);

        System.out.println("reply received");

        // Unmarshall response
        byte[] dataToUnmarshall = reply.getData();
        ServerResponse serverResponse = Marshallable.unmarshall(dataToUnmarshall, ServerResponse.class);
        String response = serverResponse.getData();
        System.out.println("response: " + response);
        return response;
    }
}
