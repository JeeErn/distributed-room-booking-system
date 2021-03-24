package Client;

import Marshaller.Marshallable;

import java.util.ArrayList;
import java.util.List;

public class ClientRequest extends Marshallable {
    int requestMethod;
    List<String> arguments = new ArrayList<>();


    public ClientRequest(){

    }

    public ClientRequest(int requestMethod, List<String> arguments){
        this.requestMethod = requestMethod;
        this.arguments = arguments;
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
