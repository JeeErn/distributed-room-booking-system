package Server.Entities.Concrete;

public class ObservationSession implements Comparable<ObservationSession> {
    long expirationTimeStamp;
    String client;

    public ObservationSession(long expirationTimeStamp, String client) {
        this.expirationTimeStamp = expirationTimeStamp;
        this.client = client;
    }

    public long getExpirationTimeStamp() {
        return this.expirationTimeStamp;
    }

    public String getClient() {
        return this.client;
    }

    /**
     * Compares the expiry timestamp between o1 and o2
     * @return -1 if o1 < o2; 0 if o1 = o2; 1 if o1 > 02
     */
    @Override
    public int compareTo(ObservationSession o) {
        return Long.compare(this.expirationTimeStamp, o.expirationTimeStamp);
    }
}
