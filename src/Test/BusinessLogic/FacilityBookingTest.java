package Test.BusinessLogic;

import Server.BusinessLogic.FacilitiesBookingSystem;
import Server.DataAccess.IServerDB;
import Server.DataAccess.ServerDB;
import Server.Entities.Concrete.TimeSlot;
import Server.Exceptions.*;
import org.junit.Test;

import java.text.ParseException;
import java.util.ArrayList;
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
        } catch (ParseException e) {
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

        // Client B creates bookings for LT1 --> booking for 1 hour on monday 02:00 - 03:00
        String clientIdB = "Client B";
        String bookingStartDateTimeB = "1/02/01";
        String bookingEndDateTimeB = "1/03/00";

        try {
            fbs.createBooking(facilityName, bookingStartDateTime, bookingEndDateTime, clientId);
            fbs.createBooking(facilityName, bookingStartDateTimeB, bookingEndDateTimeB, clientIdB);
        } catch (TimingUnavailableException e) {
            assert false;
            e.printStackTrace();
        } catch (FacilityNotFoundException e) {
            assert false;
            e.printStackTrace();
        } catch (InvalidDatetimeException e) {
            assert false;
            e.printStackTrace();
        } catch (ParseException e) {
            assert false;
            e.printStackTrace();
        }


        try {
            HashMap<Integer, List<TimeSlot>> availableTimings = fbs.getAvailability(facilityName, daysQuery);
            List<TimeSlot> ts1 = availableTimings.get(1);
            assertEquals("0000",ts1.get(0).getStartTime());
            assertEquals("0059",ts1.get(0).getEndTime());

            // We dont include 0200 - 0200 in the available timeslot as it is redundant
            assertEquals("0301",ts1.get(1).getStartTime());
            assertEquals("2359",ts1.get(1).getEndTime());

            List<TimeSlot> ts2 = availableTimings.get(2);
            assertEquals("0000",ts2.get(0).getStartTime());
            assertEquals("2359",ts2.get(0).getEndTime());

        } catch (BookingNotFoundException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void updateBookingTest() {
        IServerDB serverDB = new ServerDB();
        FacilitiesBookingSystem fbs = new FacilitiesBookingSystem(serverDB);
        // Client queriess availablity for monday and tuesday, for facility LT1
        List<Integer> daysQuery = new ArrayList<Integer>();
        daysQuery.add(1);
        String facilityName = "LT1";

        // Client A creates bookings for LT1 --> booking for 1 hour on monday 01:00 - 02:00
        String clientId = "Client A";
        String bookingStartDateTime = "1/01/00";
        String bookingEndDateTime = "1/02/00";

        // Client B creates bookings for LT1 --> booking for 1 hour on monday 02:00 - 03:00
        String clientIdB = "Client B";
        String bookingStartDateTimeB = "1/02/01";
        String bookingEndDateTimeB = "1/03/00";

        String confirmationIdB;


        try {
            fbs.createBooking(facilityName, bookingStartDateTime, bookingEndDateTime, clientId);
            confirmationIdB = fbs.createBooking(facilityName, bookingStartDateTimeB, bookingEndDateTimeB, clientIdB);

            // Client B wants to offset his booking for LT1 by 90 mins
            fbs.updateBooking(confirmationIdB, clientIdB, 90);

            HashMap<Integer, List<TimeSlot>> availableTimings = fbs.getAvailability(facilityName, daysQuery);
            List<TimeSlot> ts1 = availableTimings.get(1);

            assertEquals("0000",ts1.get(0).getStartTime());
            assertEquals("0059",ts1.get(0).getEndTime());

            assertEquals("0201",ts1.get(1).getStartTime());
            assertEquals("0330",ts1.get(1).getEndTime());

            assertEquals("0431",ts1.get(2).getStartTime());
            assertEquals("2359",ts1.get(2).getEndTime());

        } catch (TimingUnavailableException e) {
            assert false;
            e.printStackTrace();
        } catch (FacilityNotFoundException e) {
            assert false;
            e.printStackTrace();
        } catch (InvalidDatetimeException e) {
            assert false;
            e.printStackTrace();
        } catch (BookingNotFoundException e) {
            assert false;
            e.printStackTrace();
        } catch (WrongClientIdException e) {
            assert false;
            e.printStackTrace();
        } catch (ParseException e) {
            assert false;
            e.printStackTrace();
        }

    }

    @Test
    public void updateBookingTestBadRequest() {
        IServerDB serverDB = new ServerDB();
        FacilitiesBookingSystem fbs = new FacilitiesBookingSystem(serverDB);
        // Client queriess availablity for monday and tuesday, for facility LT1
        List<Integer> daysQuery = new ArrayList<Integer>();
        daysQuery.add(1);
        String facilityName = "LT1";

        // Client A creates bookings for LT1 --> booking for 1 hour on monday 01:00 - 02:00
        String clientId = "Client A";
        String bookingStartDateTime = "1/03/30";
        String bookingEndDateTime = "1/04/30";

        // Client B creates bookings for LT1 --> booking for 1 hour on monday 02:00 - 03:00
        String clientIdB = "Client B";
        String bookingStartDateTimeB = "1/02/00";
        String bookingEndDateTimeB = "1/03/00";

        String confirmationIdB;

        try {
            // Testing for regular case
            try{
                fbs.createBooking(facilityName, bookingStartDateTime, bookingEndDateTime, clientId);
                confirmationIdB = fbs.createBooking(facilityName, bookingStartDateTimeB, bookingEndDateTimeB, clientIdB);
                // Client B wants to offset his booking for LT1 by 60 mins, he shouldnt be able to do so
                fbs.updateBooking(confirmationIdB, clientIdB, 60);
                assert false;
            }
            catch (TimingUnavailableException e){
                assert true;
            }

        } catch (FacilityNotFoundException e) {
            e.printStackTrace();
        } catch (InvalidDatetimeException e) {
            e.printStackTrace();
        } catch (BookingNotFoundException e) {
            e.printStackTrace();
        } catch (WrongClientIdException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void updateBookingTestBadRequestEdgeCase() {
        IServerDB serverDB = new ServerDB();
        FacilitiesBookingSystem fbs = new FacilitiesBookingSystem(serverDB);
        // Client queriess availablity for monday and tuesday, for facility LT1
        List<Integer> daysQuery = new ArrayList<Integer>();
        daysQuery.add(1);
        String facilityName = "LT1";

        // Client A creates bookings for LT1 --> booking for 1 hour on monday 01:00 - 02:00
        String clientId = "Client A";
        String bookingStartDateTime = "1/03/30";
        String bookingEndDateTime = "1/04/30";

        // Client B creates bookings for LT1 --> booking for 1 hour on monday 02:00 - 03:00
        String clientIdB = "Client B";
        String bookingStartDateTimeB = "1/02/00";
        String bookingEndDateTimeB = "1/03/00";

        String confirmationIdB;

        try {
            // Testing for edge case where the new start time directly coincides with another booking's start time
            try{
                fbs.createBooking(facilityName, bookingStartDateTime, bookingEndDateTime, clientId);
                confirmationIdB = fbs.createBooking(facilityName, bookingStartDateTimeB, bookingEndDateTimeB, clientIdB);
                // Client B wants to offset his booking for LT1 by 90 mins, he shouldnt be able to do so
                fbs.updateBooking(confirmationIdB, clientIdB, 90);
                assert false;
            }
            catch (TimingUnavailableException e){
                assert true;
            }

        } catch (FacilityNotFoundException e) {
            e.printStackTrace();
        } catch (InvalidDatetimeException e) {
            e.printStackTrace();
        } catch (BookingNotFoundException e) {
            e.printStackTrace();
        } catch (WrongClientIdException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void updateBookingTestBadRequestEdgeCase2() {
        IServerDB serverDB = new ServerDB();
        FacilitiesBookingSystem fbs = new FacilitiesBookingSystem(serverDB);
        // Client queriess availablity for monday and tuesday, for facility LT1
        List<Integer> daysQuery = new ArrayList<Integer>();
        daysQuery.add(1);
        String facilityName = "LT1";

        // Client A creates bookings for LT1 --> booking for 1 hour on monday 01:00 - 02:00
        String clientId = "Client A";
        String bookingStartDateTime = "1/03/30";
        String bookingEndDateTime = "1/04/30";

        // Client B creates bookings for LT1 --> booking for 1 hour on monday 02:00 - 03:00
        String clientIdB = "Client B";
        String bookingStartDateTimeB = "1/02/00";
        String bookingEndDateTimeB = "1/03/00";

        String confirmationIdB;

        try {
            // Testing for edge case where the new end time directly coincides with another booking's start time
            try{
                fbs.createBooking(facilityName, bookingStartDateTime, bookingEndDateTime, clientId);
                confirmationIdB = fbs.createBooking(facilityName, bookingStartDateTimeB, bookingEndDateTimeB, clientIdB);
                // Client B wants to offset his booking for LT1 by 30 mins, he shouldnt be able to do so
                fbs.updateBooking(confirmationIdB, clientIdB, 30);
                assert false;
            }
            catch (TimingUnavailableException e){
                assert true;
            }
        } catch (FacilityNotFoundException e) {
            e.printStackTrace();
        } catch (InvalidDatetimeException e) {
            e.printStackTrace();
        } catch (BookingNotFoundException e) {
            e.printStackTrace();
        } catch (WrongClientIdException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}
