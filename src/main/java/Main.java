import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import search.genes.IntegerGene;
import search.genes.JSONObjectGene;
import search.genes.StringGene;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Main {

    public static void main (String args[]) {
        String url = "wss://s.altnet.rippletest.net/";

        try {
            // Normal (easy) way to make contact with the API
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

//            final connection.Client client = new connection.Client(new URI(url));
//            client.connect();
//
//            while (!client.connected) {
//                System.out.println("Waiting");
//                Thread.sleep(5);
//            }
//
//            System.out.println("Connected");
//
//            // Create request within the grammar structure
//            StringGene id = new StringGene("id");
//            IntegerGene idValue = new IntegerGene(0);
//            StringGene command = new StringGene("command");
//            StringGene commandValue = new StringGene("server_info");
//            StringGene apiVersion = new StringGene("api_version");
//            IntegerGene apiVersionValue = new IntegerGene(1);
//
//            JSONObjectGene objectGene = new JSONObjectGene();
//            objectGene.addChild(id, idValue);
//            objectGene.addChild(command, commandValue);
//            objectGene.addChild(apiVersion, apiVersionValue);
//
//            JSONObject request = objectGene.toJSON();
//
//            CompletableFuture<JSONObject> future = client.sendRequest(idValue.getValue(), request);
//
//            JSONObject response = future.get(10, TimeUnit.SECONDS);
//
//            System.out.println(response);
//
//            client.close();

        } catch (URISyntaxException e) {
            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        } catch (TimeoutException e) {
//            e.printStackTrace();
        }

    }

}
