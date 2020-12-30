package connection;

import org.json.JSONObject;

public class ResponseObject {
    private int responseCode;
    private JSONObject responseObject;

    /**
     * ResponseObject constructor.
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
