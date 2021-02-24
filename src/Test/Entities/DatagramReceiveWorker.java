package Test.Entities;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class DatagramReceiveWorker implements Runnable {
    DatagramSocket socket;
    byte[] bufferIn;
    volatile String serverReply;

    public DatagramReceiveWorker(DatagramSocket socket) {
        this.socket = socket;
        bufferIn = new byte[512];
    }

    @Override
    public void run() {
        try {
            DatagramPacket reply = new DatagramPacket(bufferIn, bufferIn.length);
            socket.receive(reply);
            serverReply = new String(bufferIn, 0, reply.getLength());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public String getServerReply() {
        return serverReply;
    }
}
