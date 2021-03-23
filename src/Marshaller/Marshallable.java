package Marshaller;

import java.util.List;

public class Marshallable {
    int id;

//    public Marshallable() {
//        //TODO: need to (this.id = id;) ??
//    }

    public List<Byte> marshall() throws IllegalAccessException {
        return Marshaller.marshall(this);
    }

    public static <T extends Marshallable> T unmarshall(List<Byte> seqBytes, Class<T> c) {
        try {
            return Unmarshaller.unmarshall(seqBytes, c);
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
