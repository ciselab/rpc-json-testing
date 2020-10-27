import org.json.JSONObject;

public interface EventListener {
    void onMessage(JSONObject msg);
    void onConnecting(int attempt);
    void onDisconnected(boolean willReconnect);
    void onError(Exception error);
    void onConnected();
}
