package Server.Entities.Concrete;

import Server.Entities.AbstractFacility;

import java.util.PriorityQueue;

public class CallbackTestFacility extends AbstractFacility {
    public CallbackTestFacility(String facilityName) {
        super.setFacilityName(facilityName);
        super.setObservationSessions(new PriorityQueue<>());
    }
}
