package Client;

import Marshaller.Marshallable;

import java.util.ArrayList;
import java.util.List;

public class ClientRequest extends Marshallable {
    int requestMethod;
    List<String> arguments = new ArrayList<>();

    // Note: Unmarshalling needs to have an empty constructor for some reason
    public ClientRequest(){
    }

    public ClientRequest(int requestMethod, List<String> arguments, int requestId){
        this.requestMethod = requestMethod;
        this.arguments = arguments;
        this.setId(requestId);
    }

    public void setRequestMethod(int requestMethod){
        this.requestMethod = requestMethod;
    }

    public void setArguments(List<String> arguments){
        this.arguments = arguments;
    }

    public int getRequestMethod() {
        return requestMethod;
    }

    public List<String> getArguments(){
        return arguments;
    }


}
