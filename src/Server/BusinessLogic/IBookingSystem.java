package Server.BusinessLogic;

import Server.Entities.Concrete.TimeSlot;
import Server.Exceptions.*;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;

public interface IBookingSystem {
    /**
     * Creates a booking for a facility from start time to end time
     * @param facilityName: the name of the facility to book
     * @param startDateTime: the start datetime in the form D/HH/mm
     * @param endDateTime: the end datetime in the form D/HH/mm
     * @param clientId: the clientId of the client who requested the booking
     * @return the confirmation id of the booking
     * @throws TimingUnavailableException if the time slot is already booked or if the start time and end time are on different days
     * @throws FacilityNotFoundException if the facility name provided does not exist in the database
     * @throws InvalidDatetimeException if the start datetime or the end datetime is not in the format D/HH/mm,
     * with 0 <= D < 8, 0 <= HH < 24, 0 <= mm < 60, or if the end time is earlier than the start time
     */
    String createBooking(String facilityName, String startDateTime, String endDateTime, String clientId)
            throws TimingUnavailableException, FacilityNotFoundException, InvalidDatetimeException, ParseException;

    /**
     * Updates a booking by shifting it forward or backward by a given offset
     * @param confirmationId: the confirmation id of an existing, confirmed booking
     * @param offset: the offset in minutes to shift the booking time. Negative values shift the booking earlier, positive values shift it later
     * @throws TimingUnavailableException if the time slot is already booked, or if the offset causes the booking to be shifted to a different day
     * @throws BookingNotFoundException if the confirmation id provided does not exist in the database
     * @throws InvalidDatetimeException if the offset causes the new start datetime or new end datetime to move to a new day
     */
    void updateBooking(String confirmationId, String clientId,  int offset)
            throws TimingUnavailableException, BookingNotFoundException, InvalidDatetimeException, WrongClientIdException, ParseException;

    /**
     * Gets the availability of the queried day
     * @param facilityName: the facility that the client is trying to book
     * @param days: the days that the client is trying to query for
     * @return avaialble timings in hashmap of day : TimeSlot
     * */
    HashMap<Integer, List<TimeSlot>> getAvailability(String facilityName, List<Integer> days) throws BookingNotFoundException, ParseException;

}
