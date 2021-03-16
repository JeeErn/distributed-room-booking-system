package Server.BusinessLogic;


import Server.DataAccess.IServerDB;
import Server.Entities.Concrete.TimeSlot;
import Server.Entities.IBooking;
import Server.Exceptions.*;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * server db is injected into the business logic layer
 */
public class FacilitiesBookingSystem implements IBookingSystem {
    IServerDB serverDB;

    public FacilitiesBookingSystem(IServerDB db) {
        serverDB = db;
    }

    @Override
    public String createBooking(String facilityName, String startDateTime, String endDateTime, String clientId)
            throws TimingUnavailableException, FacilityNotFoundException, InvalidDatetimeException, ParseException
    {
        if (!isBookingDatetimeValid(startDateTime, endDateTime)) throw new InvalidDatetimeException("Invalid start or end datetime");
        String[] startDatetimeSplit = startDateTime.split("/");
        String[] endDatetimeSplit = endDateTime.split("/");
        int day = Integer.parseInt(startDatetimeSplit[0]);

        String startTime = startDatetimeSplit[1] + startDatetimeSplit[2];
        String endTime = endDatetimeSplit[1] +  endDatetimeSplit[2];
        try {
            List<IBooking> sortedBookings = serverDB.getSortedBookingsByDay(facilityName, day);
            TimeSlot timeSlot = new TimeSlot(startTime, endTime);
            if (!isTimingAvailable(sortedBookings, timeSlot)) {
                throw new TimingUnavailableException("Other bookings exist at this timeslot");
            }
            return serverDB.createBooking(day, clientId, facilityName, startTime, endTime);
        } catch (FacilityNotFoundException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public String createBooking(String facilityName, String startDateTime, String endDateTime, String clientId, DatagramSocket serverSocket)
            throws TimingUnavailableException, FacilityNotFoundException, InvalidDatetimeException, ParseException
    {
        if (!isBookingDatetimeValid(startDateTime, endDateTime)) throw new InvalidDatetimeException("Invalid start or end datetime");
        String[] startDatetimeSplit = startDateTime.split("/");
        String[] endDatetimeSplit = endDateTime.split("/");
        int day = Integer.parseInt(startDatetimeSplit[0]);

        String startTime = startDatetimeSplit[1] + startDatetimeSplit[2];
        String endTime = endDatetimeSplit[1] +  endDatetimeSplit[2];
        try {
            List<IBooking> sortedBookings = serverDB.getSortedBookingsByDay(facilityName, day);
            TimeSlot timeSlot = new TimeSlot(startTime, endTime);
            if (!isTimingAvailable(sortedBookings, timeSlot)) {
                throw new TimingUnavailableException("Other bookings exist at this timeslot");
            }
            return serverDB.createBooking(day, clientId, facilityName, startTime, endTime, serverSocket);
        } catch (FacilityNotFoundException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void updateBooking(String confirmationId, String clientId, int offset)
            throws TimingUnavailableException, BookingNotFoundException, InvalidDatetimeException, WrongClientIdException, ParseException
    {
        String[] bookingInfo = confirmationId.split(IBooking.confirmationIdSeparator);
        String facilityName = bookingInfo[2];
        int day = retrieveDayInt(bookingInfo[1]);
        try {
            List<IBooking> sortedBookings = serverDB.getSortedBookingsByDay(facilityName, day);
            IBooking bookingToUpdate = serverDB.getBookingByConfirmationId(confirmationId, facilityName);

            if(bookingToUpdate.getClientId() != clientId){
                throw new WrongClientIdException("Client ID is wrong");
            }

            TimeSlot oldTimeSlot = bookingToUpdate.getTimeSlot();
            TimeSlot newTimeSlot = oldTimeSlot.offSetTimeSlot(offset);

            if (!isTimingAvailable(sortedBookings, bookingToUpdate, newTimeSlot)) {
                throw new TimingUnavailableException("Other bookings exist at new timeslot");
            }
            serverDB.updateBooking(confirmationId, facilityName, newTimeSlot.getStartTime(), newTimeSlot.getEndTime());
        } catch (FacilityNotFoundException e) {
            e.printStackTrace();
            throw new BookingNotFoundException(e.getMessage());
        }
    }

    @Override
    public void updateBooking(String confirmationId, String clientId, int offset, DatagramSocket serverSocket)
            throws TimingUnavailableException, BookingNotFoundException, InvalidDatetimeException, WrongClientIdException, ParseException
    {
        String[] bookingInfo = confirmationId.split(IBooking.confirmationIdSeparator);
        String facilityName = bookingInfo[2];
        int day = retrieveDayInt(bookingInfo[1]);
        try {
            List<IBooking> sortedBookings = serverDB.getSortedBookingsByDay(facilityName, day);
            IBooking bookingToUpdate = serverDB.getBookingByConfirmationId(confirmationId, facilityName);

            if(bookingToUpdate.getClientId() != clientId){
                throw new WrongClientIdException("Client ID is wrong");
            }

            TimeSlot oldTimeSlot = bookingToUpdate.getTimeSlot();
            TimeSlot newTimeSlot = oldTimeSlot.offSetTimeSlot(offset);

            if (!isTimingAvailable(sortedBookings, bookingToUpdate, newTimeSlot)) {
                throw new TimingUnavailableException("Other bookings exist at new timeslot");
            }
            serverDB.updateBooking(confirmationId, facilityName, newTimeSlot.getStartTime(), newTimeSlot.getEndTime(), serverSocket);
        } catch (FacilityNotFoundException e) {
            e.printStackTrace();
            throw new BookingNotFoundException(e.getMessage());
        }
    }

    public String getAvailability (String facilityName, List<Integer> days) throws FacilityNotFoundException, ParseException {
        try {
            return serverDB.getAvailability(facilityName, days);
        } catch (FacilityNotFoundException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void addObservingClient(String facilityName, InetAddress clientAddress, int clientPort, int duration) throws FacilityNotFoundException {
        long expirationTime = calculateExpiryTimestamp(duration);
        serverDB.addObservingClient(facilityName, clientAddress, clientPort, expirationTime);
    }

    // =====================================
    // Private methods
    // =====================================
    /**
     * @param dayString: string in the form "day#", where # is the int code to retrive
     */
    private int retrieveDayInt(String dayString) {
        return Integer.parseInt(dayString.substring(3));
    }

    /**
     * Returns if a booking between the start and end time can be created
     */
    private boolean isTimingAvailable(List<IBooking> sortedBookings, TimeSlot newTimeSlot) throws ParseException {
        List<IBooking> sortedBookingsCopy = new ArrayList<>(sortedBookings); // Make a copy so that we do not manipulate the original list
        List<TimeSlot> availabilityList = SharedUtil.getAvailabilityList(sortedBookingsCopy);
        return (SharedUtil.checkIfTimeSlotInsertable(availabilityList, newTimeSlot));
    }
    /**
     * Returns if a booking between the start and end time can be updated
     */
    private boolean isTimingAvailable(List<IBooking> sortedBookings, IBooking bookingToUpdate, TimeSlot newTimeSlot) throws ParseException {
        List<IBooking> sortedBookingsCopy = new ArrayList<>(sortedBookings); // Make a copy so that we do not manipulate the original list
        sortedBookingsCopy.remove(bookingToUpdate);
        List<TimeSlot> availabilityList = SharedUtil.getAvailabilityList(sortedBookingsCopy);
        return (SharedUtil.checkIfTimeSlotInsertable(availabilityList, newTimeSlot));
    }


    private boolean isBookingDatetimeValid(String startDatetime, String endDatetime) {
        String[] startDatetimeSplit = startDatetime.split("/");
        String[] endDatetimeSplit = endDatetime.split("/");
        if (!isDatetimeValid(startDatetimeSplit) || !isDatetimeValid(endDatetimeSplit)) return false;
        return isStartAndEndTimeValid(startDatetimeSplit, endDatetimeSplit);
    }

    private boolean isStartAndEndTimeValid(String[] startDatetime, String[] endDatetime) {
        boolean isSameDay = Integer.parseInt(startDatetime[0]) == Integer.parseInt(endDatetime[0]);
        int startTime = Integer.parseInt(startDatetime[1] + startDatetime[2]);
        int endTime = Integer.parseInt(endDatetime[1] + endDatetime[2]);
        boolean isStartEarlier = startTime < endTime;
        return (isSameDay && isStartEarlier);
    }

    private boolean isDatetimeValid(String[] datetime) {
        if (datetime.length != 3) return false;
        return (
                isDayValid(datetime[0])
                && isHourValid(datetime[1])
                && isMinuteValid(datetime[2])
        );
    }

    private boolean isDayValid(String dayString) {
        int day = Integer.parseInt(dayString);
        return day >= 0 && day <= 7;
    }

    private boolean isHourValid(String hourString) {
        int hour = Integer.parseInt(hourString);
        return hour >= 0 && hour < 24;
    }

    private boolean isMinuteValid(String minString) {
        int minute = Integer.parseInt(minString);
        return minute >= 0 && minute < 60;
    }

    private long calculateExpiryTimestamp(int duration) {
        // duration in minutes * 60 = duration in seconds * 1000 = duration in milliseconds
        long durationInMillis = duration * 60 * 1000L;
        long systemTime = System.currentTimeMillis();
        return systemTime + durationInMillis;
    }
}
