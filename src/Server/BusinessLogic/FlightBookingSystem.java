package Server.BusinessLogic;

import Server.DataAccess.IServerDB;


/**
 * server db is injected into the business logic layer
 */

public class FlightBookingSystem {
    IServerDB serverDB;
    public FlightBookingSystem(IServerDB serverDB){
        this.serverDB = serverDB;
    }
}
