package Server.Entities.Concrete;

import Server.Entities.IBooking;

public class Booking implements Comparable<Booking>, IBooking {
    private String clientId;
    private String confirmationId;
    private TimeSlot timeSlot;
    private int day;

    public Booking(String facilityName, String clientId, int day, String startTime, String endTime) {
        this.clientId = clientId;
        this.day = day;
        this.timeSlot = new TimeSlot(startTime, endTime);
        this.confirmationId = generateConfirmationId(facilityName, clientId);
    }

    // =====================================
    // Getters
    // =====================================
    public String getClientId() {
        return clientId;
    }

    public String getConfirmationId() {
        return confirmationId;
    }

    public String getStartTime() {
        return this.timeSlot.getStartTime();
    }

    public String getEndTime() {
        return this.timeSlot.getEndTime();
    }

    public TimeSlot getTimeSlot() { return this.timeSlot; }

    public int getDay() {
        return day;
    }

    @Override
    public int compareTo(Booking otherBooking) {
        return compare(this.getStartTime(), otherBooking.getStartTime());
    }

    public void updateStartEndTime(String newStartTime, String newEndTime) {
        TimeSlot timeSlot = new TimeSlot(newStartTime, newEndTime);
        this.timeSlot = timeSlot;
    }

    // =====================================
    // Private methods
    // =====================================

    /**
     * Compares to time strings to determine if time1 is earlier than time2
     * @param time1: time in the format HH:mm
     * @param time2: time in the format HH:mm
     * @return -ve int if time1 < time2, else +ve
     */
    private int compare(String time1, String time2) {
        int timeOne = Integer.parseInt(time1.replace(":", ""));
        int timeTwo = Integer.parseInt(time2.replace(":", ""));
        return isEarlier(timeOne, timeTwo);
    }

    private int isEarlier(int timeOne, int timeTwo) {
        return Integer.compareUnsigned(timeOne, timeTwo);
    }


    private String generateConfirmationId(String facilityName, String clientId) {
        return clientId + IBooking.confirmationIdSeparator + "day" + this.day + IBooking.confirmationIdSeparator + facilityName;
    }
}
