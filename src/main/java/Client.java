import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.URI;

public class Client extends WebSocketClient {

    WeakReference<EventListener> h;

    public Client(URI serverURI) {
        super(serverURI, new Draft_17());
    }

    public void muteEventHandler() {
        h.clear();
    }

    public void setEventHandler(EventListener eventHandler) {
        h = new WeakReference<EventListener>(eventHandler);
    }

    public void sendRequest(JSONObject jsonObject) {
        this.send(jsonObject.toString());
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        EventListener handler = h.get();
        if (handler != null) {
            handler.onConnected();
        }
    }

    @Override
    public void onMessage(String message) {
        EventListener handler = h.get();
        if (handler != null) {
            handler.onMessage(new JSONObject(message));
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        EventListener handler = h.get();
        if (handler != null) {
            handler.onDisconnected(false);
        }
    }

    @Override
    public void onError(Exception ex) {
        EventListener handler = h.get();
        if (handler != null) {
            handler.onError(ex);
        }
    }
}