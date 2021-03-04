package Server.BusinessLogic;


import Server.DataAccess.IServerDB;
import Server.Entities.IBooking;
import Server.Entities.TimeSlot;
import Server.Exceptions.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
            throws TimingUnavailableException, FacilityNotFoundException, InvalidDatetimeException, ParseException {
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

    public HashMap<Integer, List<TimeSlot>> getAvailability (String facilityName, List<Integer> days) throws BookingNotFoundException, ParseException {
        try {
            HashMap<Integer, List<TimeSlot>> availableTimings = new HashMap<>();
        for(Integer day : days){
            List<IBooking> sortedBookings = serverDB.getSortedBookingsByDay(facilityName, day);
            List<TimeSlot> availableTimes = getAvailablilityList(sortedBookings);
            availableTimings.put(day, availableTimes);
        }
        return availableTimings;
        } catch (FacilityNotFoundException e) {
            e.printStackTrace();
            throw new BookingNotFoundException(e.getMessage());
        }
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
        List<TimeSlot> availabilityList = getAvailablilityList(sortedBookingsCopy);
        return (checkIfTimeSlotInsertable(availabilityList, newTimeSlot));
    }
    /**
     * Returns if a booking between the start and end time can be updated
     */
    private boolean isTimingAvailable(List<IBooking> sortedBookings, IBooking bookingToUpdate, TimeSlot newTimeSlot) throws ParseException {
        List<IBooking> sortedBookingsCopy = new ArrayList<>(sortedBookings); // Make a copy so that we do not manipulate the original list
        sortedBookingsCopy.remove(bookingToUpdate);
        List<TimeSlot> availabilityList = getAvailablilityList(sortedBookingsCopy);
        return (checkIfTimeSlotInsertable(availabilityList, newTimeSlot));
    }

    private static boolean checkIfTimeSlotInsertable(List<TimeSlot> availabilityList, TimeSlot timeSlot) {
        int startTime = Integer.parseInt(timeSlot.getStartTime());
        int endTime = Integer.parseInt(timeSlot.getEndTime());
        for (TimeSlot ts : availabilityList){
            int tsStart = Integer.parseInt(ts.getStartTime());
            int tsEnd = Integer.parseInt(ts.getEndTime());
            if(tsStart <= startTime){
                if(tsEnd >= endTime){
                    return true;
                }
            }
        }
        return false; // Returns false if there is no timeslot to fit the new timeslot into
    }

    /**
     * Generates a list of timeslots from the sorted bookings list
     * Avaialble bookings are inclusive e.g. [0000 - 0159]
     * @param sortedBookings
     */
    private static List<TimeSlot> getAvailablilityList(List<IBooking> sortedBookings) throws ParseException {
        List<TimeSlot> availableTimes = new ArrayList<>();
        // 1) All bookings are 0000 - 2359
        String startTime = "0000";
        for (IBooking booking : sortedBookings){
            String availEndTime = FacilitiesBookingSystem.parseTime(booking.getStartTime(), 2, 1);
            String nextAvailStartTime = FacilitiesBookingSystem.parseTime(booking.getEndTime(), 1, 1);

            TimeSlot availableTimeSlot = new TimeSlot(startTime, availEndTime);

            if(availableTimeSlot.isValidTimeSlot()){
                availableTimes.add(availableTimeSlot);
            }
            startTime = nextAvailStartTime;
        }
        availableTimes.add(new TimeSlot(startTime, "2359"));
        return availableTimes;
    }


    /**
     * Adds or subtracts from the input times
     * @param timeString
     * @param operation
     * @param minutes
     * @return
     * @throws ParseException
     */
    private static String parseTime(String timeString, int operation, int minutes) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("HHmm");
        SimpleDateFormat sdf = new java.text.SimpleDateFormat ("HHmm");
        Date time = sdf.parse(timeString);
        long timeVal = time.getTime();
        if(operation == 1){
            timeVal += 60 * 1000 * minutes;
        }else if(operation == 2){
            timeVal -= 60 * 1000 * minutes;
        }
        Date parsedTime = new Date(timeVal);
        return dateFormat.format(parsedTime);
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
}
