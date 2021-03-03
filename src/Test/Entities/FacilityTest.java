package Test.Entities;

import Server.Entities.Concrete.Facility;
import Server.Entities.IBooking;
import Server.Exceptions.BookingNotFoundException;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FacilityTest {
    Facility facility;

    @Before
    public void createFacility() {
        facility = new Facility("Test Facility", "Tester");
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
}
