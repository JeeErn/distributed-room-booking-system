package Server.DataAccess;

import Server.Entities.Concrete.Facility;
import Server.Entities.IBooking;
import Server.Entities.IBookable;
import Server.Exceptions.BookingNotFoundException;
import Server.Exceptions.FacilityNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServerDB implements IServerDB {
    // Hashmap of facility names to booking
    private HashMap<String, IBookable> facilities;
    // Hashmap of confirmationId to day of booking. Used to improve retrieval speed of booking day
    private HashMap<String, Integer> bookingsByDay;

    public ServerDB() {
        facilities = createFacilitiesTable();
        bookingsByDay = new HashMap<>();
    }

    // =====================================
    // Getters
    // =====================================
    public List<String[]> getFacilityInfo() {
        List<String[]> facilityInfo = new ArrayList<>();
        // Add facilities in the form [facilityName, facilityType]
        facilityInfo.add(new String[]{"LT1", "Lecture Theater"});
        facilityInfo.add(new String[]{"LT2", "Lecture Theater"});
        facilityInfo.add(new String[]{"TC1", "Tennis Court"});
        facilityInfo.add(new String[]{"BTC1", "Badminton Court"});
        facilityInfo.add(new String[]{"BTC2", "Badminton Court"});
        facilityInfo.add(new String[]{"SWLAB1", "Software Lab"});

        return facilityInfo;
    }

    // For testing
    public int getDayOfBooking(String confirmationId) throws BookingNotFoundException {
        if (!bookingsByDay.containsKey(confirmationId)) throw new BookingNotFoundException("Confirmation id does not exist");
        return bookingsByDay.get(confirmationId);
    }

    @Override
    public List<String> getFacilityNames() {
        return new ArrayList<>(facilities.keySet());
    }

    @Override
    public String createBooking(int day, String clientId, String facilityName, String startTime, String endTime)
            throws FacilityNotFoundException
    {
        if (!facilities.containsKey(facilityName)) {
            throw new FacilityNotFoundException("Facility does not exist");
        }
        IBookable facility = facilities.get(facilityName);
        String confirmationId = facility.addBooking(day, clientId, startTime, endTime);
        bookingsByDay.put(confirmationId, day);
        return confirmationId;
    }

    @Override
    public void updateBooking(String confirmationId, String facilityName, String newStartTime, String newEndTime)
            throws FacilityNotFoundException, BookingNotFoundException
    {
        if (!facilities.containsKey(facilityName)) {
            throw new FacilityNotFoundException("Facility does not exist");
        }
        if (!bookingsByDay.containsKey(confirmationId)) {
            throw new BookingNotFoundException("Confirmation id does not exist");
        }
        IBookable facility = facilities.get(facilityName);
        int day = bookingsByDay.get(confirmationId);
        facility.updateBooking(day, confirmationId, newStartTime, newEndTime);
    }

    @Override
    public IBooking getBookingByConfirmationId(String confirmationId, String facilityName)
            throws FacilityNotFoundException, BookingNotFoundException
    {
        if (!facilities.containsKey(facilityName)) {
            throw new FacilityNotFoundException("Facility does not exist");
        }
        IBookable facility = facilities.get(facilityName);
        return facility.getBookingByConfirmationId(confirmationId);
    }

    @Override
    public List<IBooking> getSortedBookingsByDay(String facilityName, int day) throws FacilityNotFoundException {
        if (!facilities.containsKey(facilityName)) {
            throw new FacilityNotFoundException("Facility does not exist");
        }
        IBookable facility = facilities.get(facilityName);
        return facility.getBookingsSorted(day);
    }

    // =====================================
    // Private methods
    // =====================================
    private HashMap<String, IBookable> createFacilitiesTable() {
        HashMap<String, IBookable> facilities = new HashMap<>();
        List<String[]> allFacilityInfo = getFacilityInfo();
        for (String[] facilityInfo : allFacilityInfo) {
            String facilityName = facilityInfo[0];
            String facilityType = facilityInfo[1];
            IBookable facility = new Facility(facilityName, facilityType);
            facilities.put(facilityName, facility);
        }
        return facilities;
    }

}
