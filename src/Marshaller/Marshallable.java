package Marshaller;


public class Marshallable {
    int id;

    public byte[] marshall() throws IllegalAccessException {
        return Marshaller.marshall(this);
    }

    public static <T extends Marshallable> T unmarshall(byte[] byteArr, Class<T> c) {
        try {
            return Unmarshaller.unmarshall(byteArr, c);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    // =====================================
    // Getters
    // =====================================
    public int getId() { return id; }

    // =====================================
    // Setters
    // =====================================
    public void setId(int id){
        this.id = id;
    }

}
