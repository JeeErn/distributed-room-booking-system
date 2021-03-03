package Server.BusinessLogic;


import Server.DataAccess.IServerDB;
import Server.Entities.IBooking;
import Server.Exceptions.BookingNotFoundException;
import Server.Exceptions.FacilityNotFoundException;
import Server.Exceptions.InvalidDatetimeException;
import Server.Exceptions.TimingUnavailableException;

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
    public String createBooking(String facilityName, String startDateTime, String endDateTime)
            throws TimingUnavailableException, FacilityNotFoundException, InvalidDatetimeException
    {
        if (!isBookingDatetimeValid(startDateTime, endDateTime)) throw new InvalidDatetimeException("Invalid start or end datetime");
        String[] startDatetimeSplit = startDateTime.split("/");
        String[] endDatetimeSplit = endDateTime.split("/");
        int day = Integer.parseInt(startDatetimeSplit[0]);
        // TODO: Change all startTime and endTime depending on format HH:mm to become HHmm to improve efficiency of parsing time
        String startTime = startDatetimeSplit[1] + ":" + startDatetimeSplit[2];
        String endTime = endDatetimeSplit[1] + ":" + endDatetimeSplit[2];
        try {
            List<IBooking> sortedBookings = serverDB.getSortedBookingsByDay(facilityName, day);
            if (!isTimingAvailable(sortedBookings, startTime, endTime)) {
                throw new TimingUnavailableException("Other bookings exist at this timeslot");
            }
            // TODO: Find how to create meaningful client id
            String clientId = "Client Id";
            return serverDB.createBooking(day, clientId, facilityName, startTime, endTime);
        } catch (FacilityNotFoundException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void updateBooking(String confirmationId, int offset)
            throws TimingUnavailableException, BookingNotFoundException, InvalidDatetimeException
    {
        String[] bookingInfo = confirmationId.split(IBooking.confirmationIdSeparator);
        String facilityName = bookingInfo[2];
        int day = retrieveDayInt(bookingInfo[1]);
        try {
            List<IBooking> sortedBookings = serverDB.getSortedBookingsByDay(facilityName, day);
            IBooking bookingToUpdate = serverDB.getBookingByConfirmationId(confirmationId, facilityName);
            /*
            TODO: Check if clientId of booking is same as requesting clientId
            TODO: Calculate new startTime and endTime based on offset and retrieved booking
             */
            String newStartTime = "HH:mm";
            String newEndTime = "HH:mm";
            if (!isBookingDatetimeValid(newStartTime, newEndTime)) throw new InvalidDatetimeException("Invalid offset given");
            if (!isTimingAvailable(sortedBookings, bookingToUpdate, newStartTime, newEndTime)) {
                throw new TimingUnavailableException("Other bookings exist at new timeslot");
            }
            serverDB.updateBooking(confirmationId, facilityName, newStartTime, newEndTime);
        } catch (BookingNotFoundException e) {
            e.printStackTrace();
            throw e;
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
     * @param sortedBookings: a sorted list of bookings that already exist
     * @param startTime: the start time in the form HH:mm
     * @param endTime: the end time in the form HH:mm
     * @return a boolean to indicate if the timing provided is available
     */
    private boolean isTimingAvailable(List<IBooking> sortedBookings, String startTime, String endTime) {
        return true;
    }

    /**
     * Overload method for checking if updated time slot is available
     * @param sortedBookings: a sorted list of bookings that already exist
     * @param originalBooking: the original booking, to be used to ignore the timing in sortedBookings
     * @param newStartTime: the new start time in the form HH:mm
     * @param newEndTime: the new end time in the form HH:mm
     * @return a boolean to indicate if the new timing is available
     */
    private boolean isTimingAvailable(List<IBooking> sortedBookings, IBooking originalBooking, String newStartTime, String newEndTime) {
        return true;
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
