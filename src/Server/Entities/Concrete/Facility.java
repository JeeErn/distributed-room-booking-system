package Server.Entities.Concrete;

import Server.BusinessLogic.SharedUtil;
import Server.Entities.AbstractFacility;
import Server.Entities.IBookable;
import Server.Entities.IBooking;
import Server.Exceptions.BookingNotFoundException;

import java.io.IOException;
import java.net.DatagramSocket;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public String getAvailability(List<Integer> days) throws ParseException {
        HashMap<Integer, List<TimeSlot>> availableTimings = new HashMap<>();
        for(Integer day : days){
            List<IBooking> sortedBookings = getBookingsSorted(day);
            List<TimeSlot> availableTimes = SharedUtil.getAvailabilityList(sortedBookings);
            availableTimings.put(day, availableTimes);
        }
        return generateAvailabilityMessage(availableTimings);
    }

    @Override
    public void sendUpdateToObservingClients(DatagramSocket socket) throws IOException {
        /* Set up variables
        ioExceptCaught: indicates if IOException was thrown
        updateInfoByteBuffer: update message string in byte format
         */
        boolean ioExceptCaught = false;
        String updateMessage = this.getServerReplyString();
        byte[] updateInfoByteBuffer = updateMessage.getBytes();

        /*
        - Remove expired observations based on current timestamp
        - Iterate through priority queue and send message to clients
        - If IOException caught, rethrow at end of function
         */
        super.removeExpiredObservationSessions();
        for (ObservationSession clientSession : getObservationSessions()) {
            try {
                super.sendMessageTo(socket, clientSession.getClient(), updateInfoByteBuffer);
            } catch (IOException e) {
                ioExceptCaught = true;
                System.out.println("Failed to send to: " + clientSession.getClient());
            }
        }
        if (ioExceptCaught) throw new IOException();
    }

    @Override
    public String getServerReplyString() {
        List<Integer> days = Arrays.asList(0, 1, 2, 3, 4, 5, 6);
        try {
            return getAvailability(days);
        } catch (ParseException e) {
            e.printStackTrace();
            return "Failed to retrieve availability";
        }
    }

    @Override
    public List<IBooking> getBookingsSorted(int day) {
        PriorityQueue<IBooking> bookingsForDay = new PriorityQueue<>(sortedBookings[day]); // Create a local copy to poll
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
        IBooking newBooking = new Booking(this.getFacilityName(), clientId, day, startTime, endTime, facilityBookings.size());
        String confirmationId = newBooking.getConfirmationId();
        facilityBookings.put(confirmationId, newBooking);
        sortedBookings[day].add(newBooking);
        return confirmationId;
    }

    @Override
    public String addBooking(int day, String clientId, String startTime, String endTime, DatagramSocket serverSocket) {
        String confirmationId = addBooking(day, clientId, startTime, endTime);
        try {
            sendUpdateToObservingClients(serverSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    @Override
    public boolean updateBooking(int day, String confirmationId, String newStartTime, String newEndTime, DatagramSocket serverSocket) throws BookingNotFoundException {
        updateBooking(day, confirmationId, newStartTime, newEndTime);
        try {
            sendUpdateToObservingClients(serverSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    // =====================================
    // Private methods
    // =====================================
    private PriorityQueue<IBooking>[] createSortedBookingsPQ() {
        PriorityQueue<IBooking> sortedBookings[] = new PriorityQueue[7];
        for (int i = 0; i < sortedBookings.length; i++) {
            sortedBookings[i] = new PriorityQueue<>();
        }
        return sortedBookings;
    }

    private String generateAvailabilityMessage(HashMap<Integer, List<TimeSlot>> availabilities) {
        StringBuilder message = new StringBuilder();
        for (Integer day : availabilities.keySet()) {
            List<TimeSlot> availableTimeslots = availabilities.get(day);
            for (TimeSlot timeSlot : availableTimeslots) {
                StringBuilder timeSlotString = new StringBuilder();
                String startTime = timeSlot.getStartTime(); // HHmm
                String endTime = timeSlot.getEndTime(); // HHmm
                // Build a string in the form "D/HH/mm to D/HH/mm"
                timeSlotString.append(day);
                timeSlotString.append("/");
                timeSlotString.append(startTime.substring(0, 2));
                timeSlotString.append("/");
                timeSlotString.append(startTime.substring(2));
                timeSlotString.append(" to ");
                timeSlotString.append(day);
                timeSlotString.append("/");
                timeSlotString.append(endTime.substring(0, 2));
                timeSlotString.append("/");
                timeSlotString.append(endTime.substring(2));
                // Append to message
                message.append(timeSlotString);
                message.append(", ");
            }
        }
        return message.toString();
    }
}
