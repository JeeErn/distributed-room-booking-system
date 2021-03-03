package Server.Entities;

public class TimeSlot {
    String startTime;
    String endTime;
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
}
