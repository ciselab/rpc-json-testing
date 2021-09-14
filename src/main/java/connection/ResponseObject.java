package connection;

import org.json.JSONObject;

public class ResponseObject {
    private String method;
    private JSONObject requestObject;

    private int responseCode;
    private JSONObject responseObject;

    /**
     * ResponseObject constructor. Contains the HTTP status code and the JSONObject returned by the server.
     * @param responseCode
     * @param responseObject
     */
    public ResponseObject(String method, JSONObject requestObject, int responseCode, JSONObject responseObject) {
        this.requestObject = requestObject;
        this.responseCode = responseCode;
        this.responseObject = responseObject;
    }

    public String getMethod() {
        return method;
    }

    public JSONObject getRequestObject() {
        return requestObject;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public JSONObject getResponseObject() {
        return responseObject;
    }

}
