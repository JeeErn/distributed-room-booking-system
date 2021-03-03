package Test.BusinessLogic;

import Server.BusinessLogic.FacilitiesBookingSystem;
import Server.DataAccess.IServerDB;
import Server.DataAccess.ServerDB;
import Server.Entities.TimeSlot;
import Server.Exceptions.BookingNotFoundException;
import Server.Exceptions.FacilityNotFoundException;
import Server.Exceptions.InvalidDatetimeException;
import Server.Exceptions.TimingUnavailableException;
import org.junit.Test;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FacilityBookingTest {
    @Test
    public void basicGetAvailabilityTest() {
        IServerDB serverDB = new ServerDB();
        FacilitiesBookingSystem fbs = new FacilitiesBookingSystem(serverDB);
        // Client queriess availablity for monday and tuesday, for facility LT1
        List<Integer> daysQuery = new ArrayList<Integer>();
        daysQuery.add(1);
        daysQuery.add(2);
        String facilityName = "LT1";

        try {
            HashMap<Integer, List<TimeSlot>> availableTimings = fbs.getAvailability(facilityName, daysQuery);
            List<TimeSlot> ts1 = availableTimings.get(1);
            assertEquals("0000",ts1.get(0).getStartTime());
            assertEquals("2359",ts1.get(0).getEndTime());

            List<TimeSlot> ts2 = availableTimings.get(2);
            assertEquals("0000",ts2.get(0).getStartTime());
            assertEquals("2359",ts2.get(0).getEndTime());

        } catch (BookingNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getAvailablityWithInputsTest() {
        IServerDB serverDB = new ServerDB();
        FacilitiesBookingSystem fbs = new FacilitiesBookingSystem(serverDB);
        // Client queriess availablity for monday and tuesday, for facility LT1
        List<Integer> daysQuery = new ArrayList<Integer>();
        daysQuery.add(1);
        daysQuery.add(2);
        String facilityName = "LT1";

        // Client A creates bookings for LT1 --> booking for 1 hour on monday 01:00 - 02:00
        String clientId = "Client A";
        String bookingStartDateTime = "1/01/00";
        String bookingEndDateTime = "1/02/00";

//        // Client B creates bookings for LT1 --> booking for 1 hour on monday 02:00 - 03:00
//        String clientIdB = "Client B";
//        String bookingStartDateTimeB = "1/02/00";
//        String bookingEndDateTimeB = "1/03/00";

        try {
            fbs.createBooking(facilityName, bookingStartDateTime, bookingEndDateTime, clientId);
//            fbs.createBooking(facilityName, bookingStartDateTimeB, bookingEndDateTimeB, clientIdB);
        } catch (TimingUnavailableException e) {
            e.printStackTrace();
        } catch (FacilityNotFoundException e) {
            e.printStackTrace();
        } catch (InvalidDatetimeException e) {
            e.printStackTrace();
        }

        try {
            HashMap<Integer, List<TimeSlot>> availableTimings = fbs.getAvailability(facilityName, daysQuery);
            List<TimeSlot> ts1 = availableTimings.get(1);
            assertEquals("0000",ts1.get(0).getStartTime());
            assertEquals("0100",ts1.get(0).getEndTime());
            assertEquals("0200",ts1.get(1).getStartTime());
            assertEquals("2359",ts1.get(1).getEndTime());

            List<TimeSlot> ts2 = availableTimings.get(2);
            assertEquals("0000",ts2.get(0).getStartTime());
            assertEquals("2359",ts2.get(0).getEndTime());

        } catch (BookingNotFoundException e) {
            e.printStackTrace();
        }
    }
}
