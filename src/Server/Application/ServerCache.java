package Server.Application;

import java.util.HashMap;

public class ServerCache implements IRequestCache {
    private HashMap<String, String> requestCache;

    public ServerCache() {
        requestCache = new HashMap<>();
    }

    @Override
    public void addRequest(String requestId, String serverResponse) {
        requestCache.put(requestId, serverResponse);
    }

    @Override
    public boolean hasRequest(String requestId) {
        return requestCache.containsKey(requestId);
    }

    @Override
    public String getResponse(String requestId) {
        return requestCache.get(requestId);
    }
}
