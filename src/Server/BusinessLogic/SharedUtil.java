package Server.BusinessLogic;

import Server.Entities.Concrete.TimeSlot;
import Server.Entities.IBooking;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SharedUtil {
    public static boolean checkIfTimeSlotInsertable(List<TimeSlot> availabilityList, TimeSlot timeSlot) {
        int startTime = Integer.parseInt(timeSlot.getStartTime());
        int endTime = Integer.parseInt(timeSlot.getEndTime());
        for (TimeSlot ts : availabilityList){
            int tsStart = Integer.parseInt(ts.getStartTime());
            int tsEnd = Integer.parseInt(ts.getEndTime());
            if(tsStart <= startTime){
                if(tsEnd >= endTime){
                    return true;
                }
            }
        }
        return false; // Returns false if there is no timeslot to fit the new timeslot into
    }

    /**
     * Adds or subtracts from the input times
     * @param timeString
     * @param operation
     * @param minutes
     * @return
     * @throws ParseException
     */
    public static String parseTime(String timeString, int operation, int minutes) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("HHmm");
        SimpleDateFormat sdf = new java.text.SimpleDateFormat ("HHmm");
        Date time = sdf.parse(timeString);
        long timeVal = time.getTime();
        if(operation == 1){
            timeVal += 60 * 1000 * minutes; // Adds x minute to the time
        }else if(operation == 2){
            timeVal -= 60 * 1000 * minutes; // minuses x minutes from the time
        }
        Date parsedTime = new Date(timeVal);
        return dateFormat.format(parsedTime);
    }

    /**
     * Generates a list of timeslots from the sorted bookings list
     * Avaialble bookings are inclusive e.g. [0000 - 0159]
     * @param sortedBookings
     */
    public static List<TimeSlot> getAvailabilityList(List<IBooking> sortedBookings) throws ParseException {
        List<TimeSlot> availableTimes = new ArrayList<>();
        // 1) All bookings are 0000 - 2359
        String startTime = "0000";
        for (IBooking booking : sortedBookings){
            String availEndTime = SharedUtil.parseTime(booking.getStartTime(), 2, 1);
            String nextAvailStartTime = SharedUtil.parseTime(booking.getEndTime(), 1, 1);
            if(booking.getStartTime().equals("0000")) {
                availEndTime = "0000"; // Handle Edge case. 0000 - 1 = 2359
            }
            if(booking.getEndTime().equals("2359")) {
                nextAvailStartTime = "2359"; // Handle Edge case. 2359 + 1 = 0000
            }

            TimeSlot availableTimeSlot = new TimeSlot(startTime, availEndTime);

            if(availableTimeSlot.isValidTimeSlot()){
                availableTimes.add(availableTimeSlot);
            }
            startTime = nextAvailStartTime;
        }
        TimeSlot endTimeSlot = new TimeSlot(startTime, "2359");
        if(endTimeSlot.isValidTimeSlot())
        availableTimes.add(endTimeSlot);
        return availableTimes;
    }
}
