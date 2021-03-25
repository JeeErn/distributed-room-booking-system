package Marshaller;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.*;

public class Unmarshaller {

    /**
     * Starting point to unmarshalling
     * @param bytesArr: seqBytes to unserialize
     * @param c: class of object
     * @param <T>
     * @return
     * @throws ClassNotFoundException
     */
    public static <T extends Marshallable> T unmarshall(byte[] bytesArr, Class<T> c) throws ClassNotFoundException {
        List<Byte> seqBytes = new ArrayList<>();
        for (int i=0; i < bytesArr.length; i++){
            seqBytes.add(bytesArr[i]);
        }
        String className = unmarshallString(seqBytes);
        int id = unmarshallInteger(seqBytes);
        Object obj = unmarshallObject(seqBytes, Class.forName(className));
        Marshallable m = (Marshallable) obj;
        m.setId(id);
        return c.cast(m);
    }

    private static <T> T unmarshallObject(List<Byte> seqBytes, Class<T> c){
        T obj;

        // create instance of object
        try {
            obj = c.getDeclaredConstructor().newInstance();
//          obj = c.newInstance(); (deprecated version)
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }

        Field[] fields = c.getDeclaredFields();
        Map<String, Field> fieldNameToFieldTypeMap = new HashMap<>();
        for (Field field : fields) {
            fieldNameToFieldTypeMap.put(field.getName(), field);
            field.setAccessible(true);
        }

        while (fieldNameToFieldTypeMap.size() > 0) {
            String fieldName = unmarshallString(seqBytes);

            Field field = fieldNameToFieldTypeMap.get(fieldName);

            try {
                Type fullTypeName = field.getGenericType();
                field.set(obj, unmarshallListIter(seqBytes, fullTypeName));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            fieldNameToFieldTypeMap.remove(fieldName);
        }

        return obj;
    }

    private static Object unmarshallListIter(List<Byte> seqBytes, Type type){
        //check if obj is null
        boolean isNull = !unmarshallBoolean(seqBytes);
        if (isNull) return null;

        String[] fullTypeName = type.getTypeName().split("[<>]");
        String typeName = fullTypeName[0];

        switch(typeName){
            case "java.util.List":
                return unmarshallList(seqBytes, ((ParameterizedType) type).getActualTypeArguments()[0]);
            case "java.lang.Integer":
            case "int":
                return unmarshallInteger(seqBytes);
            case "java.lang.String":
                return unmarshallString(seqBytes);
            case "java.lang.Boolean":
            case "boolean":
                return unmarshallBoolean(seqBytes);
            case "java.lang.Short":
            case "short":
                return unmarshallShort(seqBytes);
            case "java.lang.Float":
            case "float":
                return unmarshallFloat(seqBytes);
            case "java.lang.Double":
            case "double":
                return unmarshallDouble(seqBytes);
            default:
                unmarshallObject(seqBytes, (Class<?>) type);
                break;
        }

        return null;
    }

    private static <T> List<T> unmarshallList(List<Byte> seqBytes, Type type){
        List<T> list = new ArrayList<>();
        int sizeList = byteListToInt(seqBytes);
        for(int i = 0; i < sizeList; i++){
            T obj = (T) unmarshallListIter(seqBytes, type);
            list.add(obj);
        }
        return list;
    }

    private static int unmarshallInteger(List<Byte> seqBytes){
        return byteListToInt(seqBytes);
    }

    private static int byteListToInt(List<Byte> seqBytes){
        byte[] arr = new byte[Integer.BYTES];
        for (int i = 0; i < arr.length; i++){
            byte b = seqBytes.remove(0);
            arr[i] = b;
        }
        ByteBuffer bb = ByteBuffer.wrap(arr);
        int integer = bb.getInt();
        return integer;
    }

    private static String unmarshallString(List<Byte> seqBytes){
        int strSize = byteListToInt(seqBytes);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strSize; i++){
            char c = (char) seqBytes.remove(0).byteValue();
            sb.append(c);
        }
        return sb.toString();
    }

    private static boolean unmarshallBoolean(List<Byte> seqBytes){
        return seqBytes.remove(0) != (byte) 0;
    }

    private static short unmarshallShort(List<Byte> seqBytes){
        byte[] arr = new byte[Short.BYTES];
        for (int i = 0; i < arr.length; i++){
            arr[i] = seqBytes.remove(0);
        }
        ByteBuffer bb = ByteBuffer.wrap(arr);
        short shrt = bb.getShort();
        return shrt;
    }

    private static float unmarshallFloat(List<Byte> seqBytes){
        byte[] arr = new byte[Float.BYTES];
        for (int i = 0; i < arr.length; i++){
            arr[i] = seqBytes.remove(0);
        }
        ByteBuffer bb = ByteBuffer.wrap(arr);
        float flt = bb.getFloat();
        return flt;
    }

    private static double unmarshallDouble(List<Byte> seqBytes){
        byte[] arr = new byte[Double.BYTES];
        for (int i = 0; i < arr.length; i++){
            arr[i] = seqBytes.remove(0);
        }
        ByteBuffer bb = ByteBuffer.wrap(arr);
        double dbl = bb.getDouble();
        return dbl;
    }
}
