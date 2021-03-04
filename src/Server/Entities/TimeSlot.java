package Server.Entities;

import Server.Exceptions.TimeSlotComparatorException;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

public class TimeSlot implements Comparable<TimeSlot>{
    String startTime; // Format: HHMM
    String endTime; // Format: HHMM
    public TimeSlot(String startTime, String endTime){
        this.startTime = startTime;
        this.endTime = endTime;
    }


    public String getStartTime(){
        return this.startTime;
    }
    public String getEndTime(){
        return this.endTime;
    }
    public Boolean isValidTimeSlot() {
        return !(Integer.parseInt(this.startTime) >= Integer.parseInt(this.endTime));
    }
    // String offset is in the form of : (+/-)HHMM, e.g. +0130 for add 1 hr 30 mins
    public TimeSlot offSetTimeSlot(int offSetMinutes) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("HHmm");

        SimpleDateFormat sdf = new java.text.SimpleDateFormat ("HHmm");
        Date timeStart = sdf.parse(this.getStartTime());
        Date timeEnd = sdf.parse(this.getEndTime());

        Calendar cal = Calendar.getInstance();
        cal.setTime(timeStart);
        cal.add(Calendar.MINUTE, offSetMinutes);
        timeStart = cal.getTime();

        cal.setTime(timeEnd);
        cal.add(Calendar.MINUTE, offSetMinutes);
        timeEnd = cal.getTime();

        String timeStartString = dateFormat.format(timeStart);
        String timeEndString = dateFormat.format(timeEnd);

        return new TimeSlot(timeStartString, timeEndString);
    }

    // Compares start times for sorting purposes
    @Override
    public int compareTo(TimeSlot o) {
        if(Integer.parseInt(o.getStartTime()) == Integer.parseInt(this.getStartTime())){
            return 0;
        }
        else if(Integer.parseInt(o.getStartTime()) < Integer.parseInt(this.getStartTime())){
            return 1;
        }else {
            return -1;
        }
    }

}
