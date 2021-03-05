package Marshaller;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/***
 * Done: Integer, String, Boolean, Short, Float, Double
 * In Progress:
 * Haven't Start: Class, List, Object
***/


public class Marshaller {

//    public static byte[] marshall(Object obj) {
//        List<Byte> seqBytes = new ArrayList<>();
//
//        String className = obj.getClass().getName();
//        // marshall className
//
//        marshallString(className, seqBytes);
//        marshallInteger(obj.getId(), seqBytes);
//
//        marshallObject(obj, seqBytes);
//
//        return Bytes.toArray(seqBytes);
//    }

    private static void marshallType(Object obj, List<Byte> seqBytes){
        if(obj == null){
            marshallBoolean(false, seqBytes);
            return;
        }
        marshallBoolean(true, seqBytes);
        String type = obj.getClass().getName();
        System.out.println(type);
        if(obj instanceof String){
            marshallString((String)obj, seqBytes);
        } else if(obj instanceof Integer){
            marshallInteger((Integer) obj, seqBytes);
        } else if(obj instanceof Boolean){
            marshallBoolean((Boolean) obj, seqBytes);
        } else if(obj instanceof Short){
            marshallShort((Short) obj, seqBytes);
        } else if(obj instanceof Float){
            marshallFloat((Float) obj, seqBytes);
        } else if(obj instanceof Double){
            marshallDouble((Double) obj, seqBytes);
        } else if(obj instanceof List){
            marshallList((List) obj, seqBytes);
        }
        return;
    }

    private static Object unmarshallType(List<Byte> seqBytes){
        boolean isNull = !(unmarshallBoolean(seqBytes));
        if (isNull) return null;

        // TODO: how to identify what type we are trying to unmarshall?

        return null;
    }

    private static void marshallList(List<?> list, List<Byte> seqBytes){
        seqBytes.addAll(intToByteList(list.size()));
        for (Object obj: list){
            marshallType(obj,seqBytes);
        }
    }

    private static <T> List<T> unmarshallList(List<Byte> seqBytes){
        List<T> list = new ArrayList<>();
        for(int i = 0; i < byteListToInt(seqBytes); i++){
            T obj = (T) unmarshallType(seqBytes);
            list.add(obj);
        }

        return list;
    }

    private static void marshallInteger(Integer integer, List<Byte> seqBytes){
        seqBytes.addAll(intToByteList(integer));
    }

    private static int unmarshallInteger(List<Byte> seqBytes){
        return byteListToInt(seqBytes);
    }

    private static List<Byte> intToByteList(int integer) {
        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES);
        byte[] arr = bb.putInt(integer).array();
        List<Byte> byteList = IntStream.range(0, arr.length).mapToObj(i -> arr[i]).collect(Collectors.toList());
        return byteList;
    }

    private static int byteListToInt(List<Byte> seqBytes){
        byte[] arr = new byte[Integer.BYTES];
        for (int i = 0; i < arr.length; i++){
            arr[i] = seqBytes.remove(0);
        }
        ByteBuffer bb = ByteBuffer.wrap(arr);
        int integer = bb.getInt();
        return integer;
    }

    private static void marshallString(String string, List<Byte> seqBytes){
        // add String length (??? Should use int or short)
        seqBytes.addAll(intToByteList(string.length()));
        // add String value
        byte[] arr = string.getBytes();
        List<Byte> byteList = new ArrayList<>();
        for (int i=0; i < arr.length; i++){
            byteList.add(arr[i]);
        }
        seqBytes.addAll(byteList);
    }

    private static String unmarshallString(List<Byte> seqBytes){
        int strSize = byteListToInt(seqBytes);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strSize; i++){
            sb.append((char) seqBytes.remove(0).byteValue());
        }
        return sb.toString();
    }

    private static void marshallBoolean(boolean bool, List<Byte> seqBytes){
        byte b = (byte) (bool? 1:0);
        seqBytes.add(b);
    }

    private static boolean unmarshallBoolean(List<Byte> seqBytes){
        return seqBytes.remove(0) != (byte) 0;
    }

    private static void marshallShort(short shrt, List<Byte> seqBytes){
        ByteBuffer bb = ByteBuffer.allocate(Short.BYTES);
        byte[] arr = bb.putShort(shrt).array();
        seqBytes.addAll(IntStream.range(0, arr.length).mapToObj(i -> arr[i]).collect(Collectors.toList()));
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

    private static void marshallFloat(float flt, List<Byte> seqBytes){
        ByteBuffer bb = ByteBuffer.allocate(Float.BYTES);
        byte[] arr = bb.putFloat(flt).array();
        seqBytes.addAll(IntStream.range(0, arr.length).mapToObj(i -> arr[i]).collect(Collectors.toList()));
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

    private static void marshallDouble(double dbl, List<Byte> seqBytes){
        ByteBuffer bb = ByteBuffer.allocate(Double.BYTES);
        byte[] arr = bb.putDouble(dbl).array();
        seqBytes.addAll(IntStream.range(0, arr.length).mapToObj(i -> arr[i]).collect(Collectors.toList()));
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

    public static void main(String[] args){
        int testInt = 1001232323;
        System.out.println("Test byteList To and From Int: " +  (testInt == byteListToInt(intToByteList(testInt))));
        List<Byte> seqBytesInteger = new ArrayList<>();
        marshallInteger(testInt, seqBytesInteger);
        int actualInt = unmarshallInteger(seqBytesInteger);
        System.out.println("Test Integer: " + (testInt == actualInt));

        String testString = "Distributed Systems Project";
        List<Byte> seqBytes = new ArrayList<>();
        marshallString(testString, seqBytes);
        String actualString = unmarshallString(seqBytes);
        System.out.println("Test String: " + testString.equals(actualString) );

        boolean testTrue = true;
        List<Byte> seqBytesTestTrue = new ArrayList<>();
        marshallBoolean(testTrue, seqBytesTestTrue);
        List<Byte> seqBytesTestBooleanType = new ArrayList<>();
        marshallType(testTrue, seqBytesTestBooleanType);
        boolean actualTrue = unmarshallBoolean(seqBytesTestTrue);
        boolean actualBooleanType = unmarshallBoolean(seqBytesTestBooleanType);
        System.out.println("Test Boolean True: " + (testTrue == actualTrue));
        System.out.println("Test Boolean Type: " + (testTrue == actualBooleanType));

        boolean testFalse = false;
        List<Byte> seqBytesTestFalse = new ArrayList<>();
        marshallBoolean(testFalse, seqBytesTestFalse);
        boolean actualFalse = unmarshallBoolean(seqBytesTestFalse);
        System.out.println("Test Boolean False: " + (testFalse == actualFalse));

        short testShort = 9385;
        List<Byte> seqBytesTestShort = new ArrayList<>();
        marshallShort(testShort,seqBytesTestShort);
        short actualShort = unmarshallShort(seqBytesTestShort);
        System.out.println("Test Short: " + (testShort == actualShort));

        float testFloat = 69.420f;
        List<Byte> seqBytesTestFloat = new ArrayList<>();
        marshallFloat(testFloat, seqBytesTestFloat);
        float actualFloat = unmarshallFloat(seqBytesTestFloat);
        System.out.println("Test Float: " + (testFloat == actualFloat));

        double testDouble = 93478547;
        List<Byte> seqBytesTestDouble = new ArrayList<>();
        marshallDouble(testDouble, seqBytesTestDouble);
        double actualDouble = unmarshallDouble(seqBytesTestDouble);
        System.out.println("Test Double: " + (testDouble == actualDouble));

    }


}
