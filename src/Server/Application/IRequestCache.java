package Server.Application;

public interface IRequestCache {
    /**
     * Adds a request id and the server response to the cache
     * @param requestId: the requestId string from the client
     * @param serverResponse: the generated server response for the given request
     */
    void addRequest(String requestId, String serverResponse);

    /**
     * Checks if the requestId has been responded to by the server before
     * @param requestId: the requestId string from the client
     * @return a boolean to indicate if the server has cached the response for the given request
     */
    boolean hasRequest(String requestId);

    /**
     * Gets the cached response associated with the requestId
     * @param requestId: the requestId string from the client
     * @return the server response that has been cached for the given request
     */
    String getResponse(String requestId);
}
