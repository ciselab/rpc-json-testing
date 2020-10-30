package connection;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Client extends WebSocketClient {

    public boolean connected = false;

    private Map<Integer, CompletableFuture<JSONObject>> futures;

    public Client(URI serverURI) {
        super(serverURI, new Draft_17());

        this.futures = new HashMap<>();
    }

    public CompletableFuture<JSONObject> sendRequest(int id, JSONObject jsonObject) {
        CompletableFuture<JSONObject> future = new CompletableFuture<>();

        futures.put(id, future);

        this.send(jsonObject.toString());

        return future;
    }

    @Override
    public void onMessage(String message) {
        JSONObject response = new JSONObject(message);

        // TODO check if response has id
        // als id niet er is, wat te doen met response? connecten met random iets??
        // altijd timeout setten en ook opvangen (wat als server eruit ligt? iets mee doen)

        Integer id = (Integer) response.get("id");

        if (futures.containsKey(id)) {
            futures.get(id).complete(response);
            futures.remove(id);
        }
        // handle otherwise
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        this.connected = true;
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
//        System.out.println(code);
//        System.out.println(reason);
//        System.out.println(remote);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }
}