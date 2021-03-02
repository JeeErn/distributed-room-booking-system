package Server.Entities.Concrete;

/**
 * Interface for Booking so that IBookable depends on this instead of actual Booking class
 */
public interface IBooking {
    String getClientId();
    String getConfirmationId();
    String getStartTime();
    String getEndTime();
    int getDay();
    void updateStartEndTime(String newStartTime, String newEndTime);
}
