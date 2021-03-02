package Server.DataAccess;

import Server.Exceptions.BookingNotFoundException;
import Server.Exceptions.FacilityNotFoundException;

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
     * @throws BookingNotFoundException when the confirmation id is not found in the facility
     */
    void updateBooking(String confirmationId, String facilityName, String newStartTime, String newEndTime) throws BookingNotFoundException;
}
