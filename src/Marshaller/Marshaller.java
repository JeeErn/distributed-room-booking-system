package Marshaller;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

//import com.google.common.primitives.Bytes;

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
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i< byteListToInt(seqBytes); i++){
            sb.append((char) seqBytes.get(i).byteValue());
            System.out.println((char) seqBytes.get(i).byteValue());
        }
        return sb.toString();
    }

    public static void main(String[] args){
//        int testInt = 1001232323;
//        System.out.println(testInt == byteListToInt(intToByteList(testInt)));

        String testString = "Hello";
        System.out.println(testString.getBytes());

        List<Byte> seqBytes = new ArrayList<>();
        marshallString(testString, seqBytes);
        System.out.println(unmarshallString(seqBytes));
        System.out.println(byteListToInt(seqBytes));

    }


}
