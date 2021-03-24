package Server.Application;

import Marshaller.Marshallable;

public class ServerResponse extends Marshallable {
    String response;

    public ServerResponse(){

    }

    public ServerResponse(String response){
        this.response = response;
    }

    public String getData() {
        return null;
    }
}
