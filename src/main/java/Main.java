import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

public class Main {

    public static void main (String args[]) {
        String url = "wss://s.altnet.rippletest.net/";

        try {
            WebSocketClient client = new WebSocketClient(new URI(url), new Draft_17()) {
                public void onOpen(ServerHandshake serverHandshake) {
                    System.out.println("Connected");
                    int id = 0;

                    JSONObject request = new JSONObject();
                    request.put("id", id);
                    request.put("command", "server_info");
                    request.put("api_version", 1);

                    this.send(request.toString());
                }

                public void onMessage(String s) {
                    System.out.println(s);
                    this.close();
                }

                public void onClose(int i, String s, boolean b) {

                }

                public void onError(Exception e) {
                    e.printStackTrace();
                }
            };
            client.connect();

//            final Client client = new Client(new URI(url));
//            client.setEventHandler(new EventListener() {
//                public void onMessage(JSONObject msg) {
//                    System.out.println(msg.toString());
//                }
//
//                public void onConnecting(int attempt) {
//                    System.out.println(attempt);
//                }
//
//                public void onDisconnected(boolean willReconnect) {
//                    System.out.println(willReconnect);
//                }
//
//                public void onError(Exception error) {
//                    error.printStackTrace();
//                }
//
//                public void onConnected() {
//                    System.out.println("Connected");
//                    int id = 0;
//                    Request request = new Request(client);
//                    request.setJson("id", id);
//                    request.setJson("command", "server_info");
//                    request.setJson("api_version", 1);
//
////                    request.json()
//                    request.request();
//                }
//            });
//            client.connect();



        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

}
