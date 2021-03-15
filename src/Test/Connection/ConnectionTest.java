package Test.Connection;

import Server.Entities.Concrete.Booking;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import java.util.PriorityQueue;


public class ConnectionTest {
    @Test
    public void testConnection() throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("google.com", 80));
        System.out.println(socket.getLocalAddress());
    }
}
