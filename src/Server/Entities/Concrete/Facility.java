package Server.Entities.Concrete;

import Server.Entities.AbstractFacility;
import Server.Entities.IBookable;
import Server.Entities.IBooking;
import Server.Exceptions.BookingNotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO: Add send update to observing clients within relevant functions
public class Facility extends AbstractFacility implements IBookable {
    private HashMap<String, IBooking> facilityBookings;
    private PriorityQueue<IBooking>[] sortedBookings;
    private String facilityType;

    public Facility(String facilityName, String facilityType) {
        super.setFacilityName(facilityName);
        super.setObservationSessions(new PriorityQueue<>());
        this.facilityType = facilityType;
        facilityBookings = new HashMap<>();
        sortedBookings = createSortedBookingsPQ();
    }

    // =====================================
    // Getters
    // =====================================
    public String getFacilityType() {
        return facilityType;
    }

    @Override
    public List<IBooking> getBookingsSorted(int day) {
        PriorityQueue<IBooking> bookingsForDay = sortedBookings[day];
        return Stream.generate(bookingsForDay::poll)
                .limit(bookingsForDay.size())
                .collect(Collectors.toList());
    }

    @Override
    public IBooking getBookingByConfirmationId(String confirmationId)
            throws BookingNotFoundException {
        if (!facilityBookings.containsKey(confirmationId)) {
            throw new BookingNotFoundException("No booking under confirmationId: " + confirmationId);
        }
        return facilityBookings.get(confirmationId);
    }

    @Override
    public String addBooking(int day, String clientId, String startTime, String endTime) {
        Booking newBooking = new Booking(this.getFacilityName(), clientId, day, startTime, endTime);
        String confirmationId = newBooking.getConfirmationId();
        facilityBookings.put(confirmationId, newBooking);
        sortedBookings[day].add(newBooking);
        return confirmationId;
    }

    @Override
    public boolean updateBooking(int day, String confirmationId, String newStartTime, String newEndTime)
            throws BookingNotFoundException {
        if (!facilityBookings.containsKey(confirmationId)) {
            throw new BookingNotFoundException("No booking under confirmationId: " + confirmationId);
        }
        IBooking bookingToUpdate = facilityBookings.get(confirmationId);
        // Remove and add back to heap as order may have changed
        sortedBookings[day].remove(bookingToUpdate);
        bookingToUpdate.updateStartEndTime(newStartTime, newEndTime);
        sortedBookings[day].add(bookingToUpdate);
        return true;
    }

    private PriorityQueue<IBooking>[] createSortedBookingsPQ() {
        PriorityQueue<IBooking> sortedBookings[] = new PriorityQueue[7];
        for (int i = 0; i < sortedBookings.length; i++) {
            sortedBookings[i] = new PriorityQueue<>();
        }
        return sortedBookings;
    }
}
