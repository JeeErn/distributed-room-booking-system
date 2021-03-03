package Test.DataAccess;

import Server.DataAccess.ServerDB;
import Server.Exceptions.BookingNotFoundException;
import Server.Exceptions.FacilityNotFoundException;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServerDBTest {
    ServerDB serverDB;

    @Before
    public void createServerDb() {
        serverDB = new ServerDB();
    }

    @Test
    public void testFacilityCreation() {
        List<String[]> expectedFacilityInfo = serverDB.getFacilityInfo();
        List<String> createdFacilityNames = serverDB.getFacilityNames();
        // Check same size
        assertEquals(expectedFacilityInfo.size(), createdFacilityNames.size());
        HashSet<String> actualNameSet = new HashSet<>(createdFacilityNames);
        // Iterate through facility names and pop from expected for each match
        for (String[] facilityInfo : expectedFacilityInfo) {
            String expectedName = facilityInfo[0];
            assertTrue(actualNameSet.contains(expectedName));
            actualNameSet.remove(expectedName);
        }
        // Info set should be empty at the end
        assertEquals(0, actualNameSet.size());
    }

    @Test
    public void testThrowFacilityNotFound() {
        // From create booking
        Exception e1 = assertThrows(FacilityNotFoundException.class, () ->
            serverDB.createBooking(
                    2,
                    "Dummy client",
                    "Fake facility",
                    "00:00",
                    "00:01"
            )
        );
        assertEquals("Facility does not exist", e1.getMessage());
        // From update booking
        Exception e2 = assertThrows(FacilityNotFoundException.class, () ->
            serverDB.updateBooking(
                    "Fake confirmation id",
                    "Fake facility",
                    "00:00",
                    "00:01"
            )
        );
        assertEquals("Facility does not exist", e2.getMessage());
        // From get Booking by ConfirmationId
        Exception e3 = assertThrows(FacilityNotFoundException.class, () ->
            serverDB.getBookingByConfirmationId(
                    "Fake confirmation id",
                    "Fake facility"
            )
        );
        assertEquals("Facility does not exist", e3.getMessage());
        // From get bookings by day
        Exception e4 = assertThrows(FacilityNotFoundException.class, () ->
                serverDB.getSortedBookingsByDay(
                        "Fake confirmation id",
                        0
                )
        );
        assertEquals("Facility does not exist", e4.getMessage());
    }

    @Test
    public void testThrowBookingNotFound() {
        List<String> facilityNames = serverDB.getFacilityNames();
        // From update booking
        Exception e1 = assertThrows(BookingNotFoundException.class, () ->
                serverDB.updateBooking(
                        "Fake confirmationId",
                        facilityNames.get(0),
                        "00:00",
                        "00:01"
                )
        );
        assertEquals("Confirmation id does not exist", e1.getMessage());
        // From get Booking by confirmation id
        String confirmationId = "Fake confirmationId";
        Exception e2 = assertThrows(BookingNotFoundException.class, () ->
                serverDB.getBookingByConfirmationId(
                        confirmationId,
                        facilityNames.get(0)
                )
        );
        assertEquals("No booking under confirmationId: " + confirmationId, e2.getMessage());
    }

    @Test
    public void testAddToBookingsByDayMap() throws FacilityNotFoundException, BookingNotFoundException {
        List<String> facilityNames = serverDB.getFacilityNames();
        String facilityName = facilityNames.get(0);
        int day = 0;
        String confirmationId = serverDB.createBooking(day, "Dummy client", facilityName, "10:00", "12:00");
        int actualDay = serverDB.getDayOfBooking(confirmationId);
        assertEquals(day, actualDay);
    }
}
