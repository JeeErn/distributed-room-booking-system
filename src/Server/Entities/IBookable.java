package Server.Entities;

import Server.Entities.Concrete.IBooking;
import Server.Exceptions.BookingNotFoundException;

import java.util.List;

public interface IBookable {

    /**
     * Creates a booking under the facility
     * @param day: the int code of the day enum
     * @param clientId: the client id string to identify the client
     * @param startTime: the start time in HH:mm
     * @param endTime: the end time in HH:mm
     * @return a String confirmationId
     */
    String addBooking(int day, String clientId, String startTime, String endTime);

    /**
     * Updates a booking under the facility
     * @param day: the int code of the day enum
     * @param confirmationId: confirmation id of an existing booking
     * @param newStartTime: new start time in HH:mm
     * @param newEndTime: new end time in HH:mm
     * @return true if update was done successfully, else false
     * @throws BookingNotFoundException if confirmation id is not found in facility
     */
    boolean updateBooking(int day, String confirmationId, String newStartTime, String newEndTime) throws BookingNotFoundException;

    /**
     * Gets the booking for a given day sorted by start time
     * @param day: the int code of the day enum
     * @return a sorted list of IBooking-implemented objects
     */
    List<IBooking> getBookingsSorted(int day);

    /**
     * Gets a particular booking given the confirmation code
     * @param confirmationId: the String confirmation dd of the booking to retrieve
     * @return an IBooking-implemented object with the corresponding confirmation id
     * @throws BookingNotFoundException if the confirmation id is not found in the facility
     */
    IBooking getBookingByConfirmationId(String confirmationId) throws BookingNotFoundException;
}
