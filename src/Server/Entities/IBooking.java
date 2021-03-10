package Server.Entities;

import Server.Entities.Concrete.TimeSlot;

/**
 * Interface for Booking so that IBookable depends on this instead of actual Booking class
 */
public interface IBooking {
    static final String confirmationIdSeparator = "%=";
    String getClientId();
    String getConfirmationId();
    String getStartTime();
    String getEndTime();
    TimeSlot getTimeSlot();
    int getDay();
    void updateStartEndTime(String newStartTime, String newEndTime);
}
