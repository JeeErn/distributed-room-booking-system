package Test.Entities;

import Server.Entities.Concrete.Facility;
import Server.Entities.Concrete.ObservationSession;
import Server.Entities.IBooking;
import Server.Exceptions.BookingNotFoundException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FacilityTest {
    Facility facility;
    static int serverPort = 18;
    static DatagramSocket serverSocket = null;

    @Before
    public void createFacility() {
        facility = new Facility("Test Facility", "Tester");
        facility.setObservationSessions(createExpiredObservations());
    }

    @BeforeClass
    public static void createServerSocket() {
        try {
            serverSocket = new DatagramSocket(serverPort);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAddBooking() {
        int day = 3;
        String clientId = "Dummy client";
        String startTime = "00:00";
        String endTime = "23:59";
        String confirmationId = facility.addBooking(day, clientId, startTime, endTime);
        try {
            IBooking booking = facility.getBookingByConfirmationId(confirmationId);
            assertEquals(day, booking.getDay());
            assertEquals(clientId, booking.getClientId());
            assertEquals(startTime, booking.getStartTime());
            assertEquals(endTime, booking.getEndTime());

        } catch (BookingNotFoundException e) {
            System.out.println("You should not reach here");
        }
    }

    @Test()
    public void testGetUncreatedBooking() {
        String falseConfirmationId = "Fake";
        Exception e = assertThrows(BookingNotFoundException.class, () -> facility.getBookingByConfirmationId(falseConfirmationId));
        assertEquals("No booking under confirmationId: Fake", e.getMessage());
    }

    @Test
    public void testUpdateBooking() {
        int day = 3;
        String clientId = "Dummy client";
        String startTime = "00:00";
        String endTime = "23:59";
        String newStartTime = "10:00";
        String newEndTime = "12:00";
        try {
            String confirmationId = facility.addBooking(day, clientId, startTime, endTime);
            boolean success = facility.updateBooking(day, confirmationId, newStartTime, newEndTime);
            assertTrue(success);
            // Test booking is updated in both data structures storing bookings
            // Get booking by confirmation id
            IBooking bookingByConfirmationId = facility.getBookingByConfirmationId(confirmationId);
            assertEquals(newStartTime, bookingByConfirmationId.getStartTime());
            assertEquals(newEndTime, bookingByConfirmationId.getEndTime());
            // Get booking by priority queue
            List<IBooking> sortedBookingsByDay = facility.getBookingsSorted(day);
            assertEquals(1, sortedBookingsByDay.size());
            IBooking booking = sortedBookingsByDay.get(0); // Only 1 entry
            assertEquals(newStartTime, booking.getStartTime());
            assertEquals(newEndTime, booking.getEndTime());
        } catch (BookingNotFoundException e) {
            System.out.println("You should not reach here");
        }
    }

    @Test
    public void testUpdateUncreatedBooking() {
        String falseConfirmationId = "Fake";
        Exception e = assertThrows(BookingNotFoundException.class, () -> facility.updateBooking(2, falseConfirmationId, "00:00", "00:01"));
        assertEquals("No booking under confirmationId: Fake", e.getMessage());
    }

    @Test
    public void testUpdateBookingChangesSortedBookingsOrder() {
        int day = 3;
        String clientIdToChange = "Dummy client 1";
        String clientIdUnchanged = "Dummy client 2";
        String newStartTime = "10:00";
        String startTimeUnchanged = "12:00";
        String startTimeToChange = "15:00";
        String newEndTime = "11:59";
        String endTimeUnchanged = "14:00";
        String endTimeToChange = "17:00";
        try {
            String confirmationIdToChange = facility.addBooking(day, clientIdToChange, startTimeToChange, endTimeToChange);
            facility.addBooking(day, clientIdUnchanged, startTimeUnchanged, endTimeUnchanged);
            // Test initial order
            testSortedStartTimings(day, startTimeUnchanged, startTimeToChange);
            // Update booking
            boolean success = facility.updateBooking(day, confirmationIdToChange, newStartTime, newEndTime);
            assertTrue(success);
            // Test final order
            testSortedStartTimings(day, newStartTime, startTimeUnchanged);
        } catch (Exception e) {
            System.out.println("You should not reach here");
        }
    }

    @Test
    public void testAddBookingUpdatesObservingClients() {
        try {
            // Create server reply worker and receive server reply
            int observingClientPort = serverPort + 12;
            DatagramSocket clientSocket = new DatagramSocket(observingClientPort);
            DatagramReceiveWorker receiveWorker = new DatagramReceiveWorker(clientSocket);
            Thread receiveThread = new Thread(receiveWorker);
            receiveThread.start();

            // Create valid observation session
            InetAddress clientAddress = InetAddress.getLocalHost();
            facility.addObservationSession(clientAddress, observingClientPort, System.currentTimeMillis() + 100000L);

            // Check initial observation count including expired observations
            assertEquals(7, facility.getObservationSessions().size());

            // Add booking to facility
            facility.addBooking(2, "test client", "1000", "1159", serverSocket);

            // Ensure thread has ended and assert server reply is as expected
            receiveThread.join();
            String workerReply = receiveWorker.getServerReplyAndResetBuffer();
            assertEquals(facility.getServerReplyString(), workerReply);
            System.out.println(workerReply); // Check availability message is sent correctly

            // Ensure expired observation sessions are removed
            assertEquals(1, facility.getObservationSessions().size());
        } catch (Exception e) {
            System.out.println("Program should not reach here!");
            e.printStackTrace();
        }
    }

    @Test
    public void testUpdateBookingUpdatesObservingClients() {
        try {
            // Create client socket and receive worker
            int observingClientPort = serverPort + 15;
            DatagramSocket clientSocket = new DatagramSocket(observingClientPort);
            DatagramReceiveWorker receiveWorker = new DatagramReceiveWorker(clientSocket);
            // Set up worker thread for add booking update message
            Thread receiveThread = new Thread(receiveWorker);
            receiveThread.start();
            // Create valid observation session
            InetAddress clientAddress = InetAddress.getLocalHost();
            facility.addObservationSession(clientAddress, observingClientPort, System.currentTimeMillis() + 100000L);
            // Add booking to facility
            String confirmationId = facility.addBooking(2, "test client", "1000", "1159", serverSocket);
            // Ensure thread has ended and assert server reply is as expected
            receiveThread.join();
            assertEquals(facility.getServerReplyString(), receiveWorker.getServerReplyAndResetBuffer());

            // Set up worker thread for update booking update message
            receiveThread = new Thread(receiveWorker);
            receiveThread.start();
            // Update booking
            facility.updateBooking(2, confirmationId, "0800", "0959", serverSocket);
            // Ensure thread has ended and assert server reply is as expected
            receiveThread.join();
            assertEquals(facility.getServerReplyString(), receiveWorker.getServerReplyAndResetBuffer());
        } catch (Exception e) {
            System.out.println("Program should not reach here!");
            e.printStackTrace();
        }
    }

    private void testSortedStartTimings(int day, String earlier, String later) {
        List<IBooking> sortedBookings = facility.getBookingsSorted(day);
        List<String> timingOrder = (
                sortedBookings
                .stream()
                .map(IBooking::getStartTime)
                .collect(Collectors.toList())
        );
        List<String> expectedTimingOrder = Arrays.asList(earlier, later);
        assertIterableEquals(expectedTimingOrder, timingOrder);
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
