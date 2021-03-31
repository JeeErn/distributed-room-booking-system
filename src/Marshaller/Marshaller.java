package Marshaller;

import java.lang.reflect.Field;
import java.util.*;
import java.nio.ByteBuffer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class Marshaller {

    /**
     * Starting point to marshall
     * seqBytes: Class Name, obj id, field names and corresponding values
     * @param obj: obj class must extend Marshallable class
     * @return
     * @throws IllegalAccessException
     */
    public static byte[] marshall(Marshallable obj) throws IllegalAccessException {
        List<Byte> seqBytes = new ArrayList<>();

        String className = obj.getClass().getName();

        marshallString(className, seqBytes);
        marshallInteger(obj.getId(), seqBytes);

        marshallObject(obj, seqBytes);

        //convert from list of bytes to arr of bytes since DatagramPacket buffer is in byte[]
        int size = seqBytes.size();
        byte[] bytesArr = new byte[size];

        for (int i = 0; i < size; i++){
            bytesArr[i] = seqBytes.remove(0);
        }
        return bytesArr;
    }

    /**
     * Adds object's fields and corresponding values to seqBytes
     * @param obj
     * @param seqBytes
     * @throws IllegalAccessException
     */
     private static void marshallObject(Object obj, List<Byte> seqBytes) throws IllegalAccessException {
        Class objClass = obj.getClass();
        Field[] fields = objClass.getDeclaredFields();
        for (Field field : fields){
            //field name
            marshallString(field.getName(), seqBytes);
            String fullTypeName = field.getGenericType().getTypeName();
            String[] typeNameSplit = fullTypeName.split("[<>]");
            field.setAccessible(true);
            //field value
            Object value = field.get(obj);
            marshallListIter(typeNameSplit, value, seqBytes);
        }
    }

    /**
     * Marshalls object based on given type
     * If object type is list, it will recurse until last dimension
     * @param fullTypeName
     * @param obj
     * @param seqBytes
     * @throws IllegalAccessException
     */
    private static void marshallListIter(String[] fullTypeName, Object obj, List<Byte> seqBytes) throws IllegalAccessException {
        if(obj == null){
            marshallBoolean(false, seqBytes);
            return;
        }
        marshallBoolean(true, seqBytes);
        String typeName = fullTypeName[0];
        switch (typeName) {
            case "java.util.List":
                int dimension = fullTypeName.length;
                marshallList(obj, Arrays.copyOfRange(fullTypeName,1, dimension), seqBytes);
                break;
            case "java.lang.Integer":
            case "int":
                marshallInteger((Integer) obj, seqBytes);
                break;
            case "java.lang.String":
                marshallString((String) obj, seqBytes);
                break;
            case "java.lang.Boolean":
            case "boolean":
                marshallBoolean((Boolean) obj, seqBytes);
                break;
            case "java.lang.Short":
            case "short":
                marshallShort((Short) obj, seqBytes);
                break;
            case "java.lang.Float":
            case "float":
                marshallFloat((Float) obj, seqBytes);
                break;
            case "java.lang.Double":
            case "double":
                marshallDouble((Double) obj, seqBytes);
                break;
            default:
                marshallObject(obj, seqBytes);
                break;
        }
    }

    // =====================================
    // Unmarshalling primitive/common object types
    // =====================================

    /**
     * Follows example in lecture
     * [Length of List, object within list,...]
     * @param obj
     * @param fullTypeName
     * @param seqBytes
     * @throws IllegalAccessException
     */
    private static void marshallList(Object obj, String[] fullTypeName, List<Byte> seqBytes) throws IllegalAccessException {
        List<?> list = (List<?>) obj;
        seqBytes.addAll(intToByteList(list.size()));
        for (Object o: list){
            marshallListIter(fullTypeName, o,seqBytes);
        }
    }

    private static void marshallInteger(Integer integer, List<Byte> seqBytes){
        seqBytes.addAll(intToByteList(integer));
    }

    private static List<Byte> intToByteList(int integer) {
        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES);
        byte[] arr = bb.putInt(integer).array();
        List<Byte> seqBytes = IntStream.range(0, arr.length).mapToObj(i -> arr[i]).collect(Collectors.toList());
        return seqBytes;
    }

    private static void marshallString(String string, List<Byte> seqBytes){
        // add String length
        seqBytes.addAll(intToByteList(string.length()));
        // add String value
        byte[] arr = string.getBytes();
        List<Byte> result = new ArrayList<>();
        for (int i=0; i < arr.length; i++){
            result.add(arr[i]);
        }
        seqBytes.addAll(result);
    }

    private static void marshallBoolean(boolean bool, List<Byte> seqBytes){
        byte b = (byte) (bool ? 1:0);
        seqBytes.add(b);
    }

    private static void marshallShort(short shrt, List<Byte> seqBytes){
        ByteBuffer bb = ByteBuffer.allocate(Short.BYTES);
        byte[] arr = bb.putShort(shrt).array();
        seqBytes.addAll(IntStream.range(0, arr.length).mapToObj(i -> arr[i]).collect(Collectors.toList()));
    }

    private static void marshallFloat(float flt, List<Byte> seqBytes){
        ByteBuffer bb = ByteBuffer.allocate(Float.BYTES);
        byte[] arr = bb.putFloat(flt).array();
        seqBytes.addAll(IntStream.range(0, arr.length).mapToObj(i -> arr[i]).collect(Collectors.toList()));
    }

    private static void marshallDouble(double dbl, List<Byte> seqBytes){
        ByteBuffer bb = ByteBuffer.allocate(Double.BYTES);
        byte[] arr = bb.putDouble(dbl).array();
        seqBytes.addAll(IntStream.range(0, arr.length).mapToObj(i -> arr[i]).collect(Collectors.toList()));
    }

}
