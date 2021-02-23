package Server.Entities.Concrete;

import Server.Entities.AbstractFacility;

import java.net.DatagramSocket;
import java.util.PriorityQueue;

public class CallbackTestFacility extends AbstractFacility {
    public CallbackTestFacility(String facilityName, DatagramSocket socket) {
        this.facilityName = facilityName;
        this.socket = socket;
        this.observationSessions = new PriorityQueue<>();
    }
}
