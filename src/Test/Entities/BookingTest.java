package Test.Entities;

import Server.Entities.Concrete.Booking;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class BookingTest {
    PriorityQueue<Booking> bookings;

    @Before
    public void createBookingsQueue() {
        bookings = new PriorityQueue<>();
    }

    @Test
    public void testBookingOrder() {
        ArrayList<String> originalInsertOrder = new ArrayList<>(Arrays.asList("12:30", "12:29", "09:15", "18:19"));
        populateHeap(originalInsertOrder);
        ArrayList<String> heapPopOrder = emptyHeap();
        ArrayList<String> sortedOrder = new ArrayList<>(Arrays.asList("09:15", "12:29", "12:30", "18:19"));
        assertIterableEquals(sortedOrder, heapPopOrder);
    }

    private void populateHeap(List<String> startTimes) {
        for (String startTime : startTimes) {
            bookings.add(new Booking("test facility", "Dummy client", 3, startTime, "23:59"));
        }
    }

    private ArrayList<String> emptyHeap() {
        ArrayList<String> poppedValues = new ArrayList<>();
        while (bookings.size() > 0) {
            poppedValues.add(bookings.poll().getStartTime());
        }
        return poppedValues;
    }
}
