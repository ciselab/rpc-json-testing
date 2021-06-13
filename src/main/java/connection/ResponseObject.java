package connection;

import org.json.JSONObject;

public class ResponseObject {
    private int responseCode;
    private JSONObject responseObject;

    /**
     * ResponseObject constructor. Contains the HTTP status code and the JSONObject returned by the server.
     * @param responseCode
     * @param responseObject
     */
    public ResponseObject(int responseCode, JSONObject responseObject) {
        this.responseCode = responseCode;
        this.responseObject = responseObject;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public JSONObject getResponseObject() {
        return responseObject;
    }

}
