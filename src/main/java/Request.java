import org.json.JSONObject;


// We can just shift to using delegation
public class Request {

    private Client client;

    private JSONObject      json;
    public long         sendTime;

    public Request(Client client) {
        this.client = client;
        json        = new JSONObject();
    }

    public JSONObject getJson() {
        return json;
    }

    public void setJson(String key, Object value) {
        json.put(key, value);
    }

    public void request() {
        client.sendRequest(this.json);
    }

}
