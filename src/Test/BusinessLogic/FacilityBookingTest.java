package Test.BusinessLogic;

import Server.BusinessLogic.FacilitiesBookingSystem;
import Server.DataAccess.IServerDB;
import Server.DataAccess.ServerDB;
import Server.Exceptions.*;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FacilityBookingTest {
    IServerDB serverDB;
    FacilitiesBookingSystem fbs;

    @Before
    public void createServerDB() {
        serverDB = new ServerDB();
        fbs = new FacilitiesBookingSystem(serverDB);
    }

    @Test
    public void basicGetAvailabilityTest() {
        // Client queriess availablity for monday and tuesday, for facility LT1
        List<Integer> daysQuery = new ArrayList<Integer>();
        daysQuery.add(1);
        daysQuery.add(2);
        String facilityName = "LT1";

        try {
            String availableTimings = fbs.getAvailability(facilityName, daysQuery);
            String expected = "1/00/00 to 1/23/59, 2/00/00 to 2/23/59, ";
            assertEquals(expected, availableTimings);
        } catch (BookingNotFoundException | ParseException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void getAvailablityWithInputsTest() {
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
        } catch (TimingUnavailableException | FacilityNotFoundException | InvalidDatetimeException | ParseException e) {
            assert false;
            e.printStackTrace();
        }


        try {
            String availableTimings = fbs.getAvailability(facilityName, daysQuery);
            String expected = "1/00/00 to 1/00/59, 1/03/01 to 1/23/59, 2/00/00 to 2/23/59, ";
            assertEquals(expected, availableTimings);
        } catch (BookingNotFoundException | ParseException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void updateBookingTest() {
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

            String availableTimings = fbs.getAvailability(facilityName, daysQuery);
            String expected = "1/00/00 to 1/00/59, 1/02/01 to 1/03/30, 1/04/31 to 1/23/59, ";
            assertEquals(expected, availableTimings);

        } catch (TimingUnavailableException | FacilityNotFoundException | InvalidDatetimeException | BookingNotFoundException | WrongClientIdException | ParseException e) {
            assert false;
            e.printStackTrace();
        }

    }

    @Test
    public void updateBookingTestBadRequest() {
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

        } catch (FacilityNotFoundException | InvalidDatetimeException | BookingNotFoundException | WrongClientIdException | ParseException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void updateBookingTestBadRequestEdgeCase() {
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

        } catch (FacilityNotFoundException | InvalidDatetimeException | BookingNotFoundException | WrongClientIdException | ParseException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void updateBookingTestBadRequestEdgeCase2() {
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
        } catch (FacilityNotFoundException | InvalidDatetimeException | BookingNotFoundException | WrongClientIdException | ParseException e) {
            e.printStackTrace();
        }

    }
}
