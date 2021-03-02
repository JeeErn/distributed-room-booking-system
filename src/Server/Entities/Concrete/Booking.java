package Server.Entities.Concrete;

public class Booking implements Comparable<Booking> {
    private String clientId;
    private String confirmationId;
    private String startTime;
    private String endTime;
    private int day;

    public Booking(String facilityName, String clientId, int day, String startTime, String endTime) {
        this.clientId = clientId;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
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
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    @Override
    public int compareTo(Booking otherBooking) {
        return compare(this.startTime, otherBooking.getStartTime());
    }

    public void updateStartEndTime(String newStartTime, String newEndTime) {
        startTime = newStartTime;
        endTime = newEndTime;
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
        String[] timeOne = time1.split(":");
        String[] timeTwo = time2.split(":");
        return isEarlier(timeOne[0], timeTwo[0], timeOne[1], timeTwo[1]);
    }

    private int isEarlier(String oneHours, String twoHours, String oneMins, String twoMins) {
        int oneHour = Integer.parseInt(oneHours);
        int twoHour = Integer.parseInt(twoHours);
        int oneMin = Integer.parseInt(oneMins);
        int twoMin = Integer.parseInt(twoMins);
        if (Integer.compareUnsigned(oneHour, twoHour) < 0) {
            return Integer.compareUnsigned(oneHour, twoHour);
        }
        return Integer.compareUnsigned(oneMin, twoMin);
    }

    private String generateConfirmationId(String facilityName, String clientId) {
        return clientId + "%=day" + this.day + "%=" + facilityName;
    }
}
