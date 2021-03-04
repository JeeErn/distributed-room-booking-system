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
//        marshallInt(obj.getId(), seqBytes);
//
//        marshallObject(obj, seqBytes);
//
//        return Bytes.toArray(seqBytes);
//    }

    private static void marshallInt(Integer integer, List<Byte> seqBytes){
        seqBytes.addAll(intToByteList(integer));
    }

    private static int unmarshallInt(List<Byte> seqBytes){
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
            arr[i] = seqBytes.get(i);
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
        int start = intToByteList(byteListToInt(seqBytes)).size();
        StringBuilder sb = new StringBuilder();
        for (int i = start; i< (seqBytes.size()); i++){
            sb.append((char) seqBytes.get(i).byteValue());
        }
        return sb.toString();
    }

    private static void marshallBoolean(boolean bool, List<Byte> seqBytes){
        byte b = (byte) (bool? 1:0);
        seqBytes.add(b);
    }

    private static boolean unmarshallBoolean(List<Byte> seqBytes){
        return seqBytes.get(0) != (byte) 0;
    }

    private static void marshallShort(short shrt, List<Byte> seqBytes){
        ByteBuffer bb = ByteBuffer.allocate(Short.BYTES);
        byte[] arr = bb.putShort(shrt).array();
        seqBytes.addAll(IntStream.range(0, arr.length).mapToObj(i -> arr[i]).collect(Collectors.toList()));
    }

    private static short unmarshallShort(List<Byte> seqBytes){
        byte[] arr = new byte[Short.BYTES];
        for (int i = 0; i < arr.length; i++){
            arr[i] = seqBytes.get(i);
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
            arr[i] = seqBytes.get(i);
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
            arr[i] = seqBytes.get(i);
        }
        ByteBuffer bb = ByteBuffer.wrap(arr);
        double dbl = bb.getDouble();
        return dbl;
    }

    public static void main(String[] args){
        int testInt = 1001232323;
        System.out.println(testInt == byteListToInt(intToByteList(testInt)));

        String testString = "Distributed Systems Project";
        List<Byte> seqBytes = new ArrayList<>();
        marshallString(testString, seqBytes);
        String actualString = unmarshallString(seqBytes);
        System.out.println(testString.equals(actualString) );

        boolean testTrue = true;
        List<Byte> seqBytesTestTrue = new ArrayList<>();
        marshallBoolean(testTrue, seqBytesTestTrue);
        boolean actualTrue = unmarshallBoolean(seqBytesTestTrue);
        System.out.println("Test Boolean True: " + (testTrue == actualTrue));

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
