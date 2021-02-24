package Test.Entities;

import Server.Entities.Concrete.ObservationSession;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

import static org.junit.Assert.assertEquals;

public class ObservationSessionTest {
    PriorityQueue<ObservationSession> sessions;

    @Before
    public void beforeEach() {
        sessions = new PriorityQueue<>();
    }

    @Test
    public void testComparableImpl() {
        ArrayList<Long> originalInsertOrder = new ArrayList<>(Arrays.asList(50L, 3L, 5L, 1L, 1000L));
        populateHeap(originalInsertOrder);
        List<Long> heapPopOrder = emptyHeap();
        ArrayList<Long> sortedInsertOrder = (ArrayList<Long>) originalInsertOrder.clone();
        sortedInsertOrder.sort(Long::compareTo);
        assertEquals(sortedInsertOrder, heapPopOrder);
    }

    private void populateHeap(List<Long> expiryTimes) {
        for (Long expiryTime : expiryTimes) {
            sessions.add(new ObservationSession(expiryTime, "Dummy client"));
        }
    }

    private List<Long> emptyHeap() {
        List<Long> poppedValues = new ArrayList<>();
        while (sessions.size() > 0) {
            poppedValues.add(sessions.poll().getExpirationTimeStamp());
        }
        return poppedValues;
    }
}
