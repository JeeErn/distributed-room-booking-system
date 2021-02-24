package Test.Entities;

import Server.Entities.AbstractFacility;
import Server.Entities.Concrete.ObservationSession;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class AbstractFacilityTest {
    // serverPort and serverSocket are static so that @BeforeClass static function can access attributes
    TestFacility facility;
    static int serverPort = 17;
    static DatagramSocket serverSocket = null;

    // Before class is run ONCE ONLY before entire test starts
    @BeforeClass
    public static void createServerSocket() {
        try {
            serverSocket = new DatagramSocket(serverPort);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    // Before is run before every test
    @Before
    public void resetFacility() {
        facility = new TestFacility("Test", serverSocket);
        facility.setObservationSessions(createExpiredObservations());
    }

    @Test
    public void testAllExpiredObservations() {
        try {
            // Initial heap size is correct
            assertEquals(6, facility.getObservationSessions().size());
            // Function call should remove all observation sessions
            facility.sendUpdateToObservingClients();
            // Ending heap should be empty
            assertEquals(0, facility.getObservationSessions().size());
        } catch (IOException exception) {
            System.out.println("Program should not reach here!");
            exception.printStackTrace();
        }
    }

    @Test
    public void testOneValidObservation() {
        try {
            // Create server reply worker and receive server reply
            int clientPort = serverPort + 10;
            DatagramSocket clientSocket = new DatagramSocket(clientPort);
            DatagramReceiveWorker receiveWorker = new DatagramReceiveWorker(clientSocket);
            Thread receiveThread = new Thread(receiveWorker);
            receiveThread.start();

            // Create valid observation session
            InetAddress clientAddress = InetAddress.getLocalHost();
            facility.addObservationSession(clientAddress, clientPort, System.currentTimeMillis() + 100000L);

            // Send updates to clients
            assertEquals(7, facility.getObservationSessions().size());
            facility.sendUpdateToObservingClients();
            assertEquals(1, facility.getObservationSessions().size());

            // Ensure thread has ended and assert server reply is as expected
            receiveThread.join();
            assertEquals(facility.getServerReplyString(), receiveWorker.getServerReply());
        } catch (Exception e) {
            System.out.println("Program should not reach here!");
            e.printStackTrace();
        }
    }

    private PriorityQueue<ObservationSession> createExpiredObservations() {
        long currentSystemTime = System.currentTimeMillis();
        return new PriorityQueue<>(createObservations(Arrays.asList(
                currentSystemTime - 50L,
                currentSystemTime - 100L,
                currentSystemTime - 2L,
                currentSystemTime - 400L,
                currentSystemTime - 1000L,
                currentSystemTime - 5L
        )));
    }

    private List<ObservationSession> createObservations(List<Long> expiryTimes) {
        return expiryTimes.stream()
                .map(expiryTime -> new ObservationSession(expiryTime, "Expired client session"))
                .collect(Collectors.toList());
    }
}

class TestFacility extends AbstractFacility {
    public TestFacility(String facilityName, DatagramSocket socket) {
        super.setFacilityName(facilityName);
        super.setSocket(socket);
        super.setObservationSessions(new PriorityQueue<>());
    }
}
