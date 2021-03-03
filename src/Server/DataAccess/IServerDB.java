package Server.DataAccess;

import Server.Entities.IBooking;
import Server.Exceptions.BookingNotFoundException;
import Server.Exceptions.FacilityNotFoundException;

import java.util.List;

public interface IServerDB {
    /**
     * Creates a booking for a given day for a given facility between a specified start to end time
     * @param day: the int value of the enumerated days
     * @param clientId: the client id string
     * @param facilityName: the name of the facility to book
     * @param startTime: the start time in HH:mm
     * @param endTime: the end time in HH:mm
     * @return the confirmation id of the booking created
     * @throws FacilityNotFoundException when the facility name provided is not found
     */
    String createBooking(int day, String clientId, String facilityName, String startTime, String endTime) throws FacilityNotFoundException;

    /**
     * Updates a booking given an existing confirmation id and the new start and end times
     * @param confirmationId: confirmation id of an existing, confirmed booking
     * @param facilityName: the name of the facility
     * @param newStartTime: the new start time in HH:mm
     * @param newEndTime: the new end time in HH:mm
     * @throws FacilityNotFoundException if the facility name is not found in the database
     * @throws BookingNotFoundException when the confirmation id is not found in the facility
     */
    void updateBooking(String confirmationId, String facilityName, String newStartTime, String newEndTime) throws FacilityNotFoundException, BookingNotFoundException;

    /**
     * Retrieves a booking from a given facility using the confirmation id
     * @param confirmationId: confirmation id of an existing, confirmed booking
     * @param facilityName: the name of the facility
     * @return the IBooking-implemented object that corresponds to the confirmation id
     * @throws FacilityNotFoundException if the facility name given is not found in the database
     * @throws BookingNotFoundException if the confirmation id is not found in the facility
     */
    IBooking getBookingByConfirmationId(String confirmationId, String facilityName) throws FacilityNotFoundException, BookingNotFoundException;

    /**
     * Retrieves all bookings for the given day under the given facility
     * @param facilityName: the name of the facility
     * @param day: the int code of the days enum
     * @return a sorted list of IBooking-implemented objects
     * @throws FacilityNotFoundException if the facility name does not exist
     */
    List<IBooking> getSortedBookingsByDay(String facilityName, int day) throws FacilityNotFoundException;
}
