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
        String[] timeOneArr = time1.split(":");
        String[] timeTwoArr = time2.split(":");
        int timeOne = Integer.parseInt(timeOneArr[0] + timeOneArr[1]);
        int timeTwo = Integer.parseInt(timeTwoArr[0] + timeTwoArr[1]);
        return isEarlier(timeOne, timeTwo);
    }

    private int isEarlier(int timeOne, int timeTwo) {
        return Integer.compareUnsigned(timeOne, timeTwo);
    }


    private String generateConfirmationId(String facilityName, String clientId) {
        return clientId + "%=day" + this.day + "%=" + facilityName;
    }
}
