import connection.Client;
import org.json.JSONObject;
import search.Individual;
import search.genes.ArrayGene;
import search.genes.JSONObjectGene;
import search.genes.StringGene;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Main {

    public static void main (String args[]) {

        // TODO: find other APIs to connect to
        // The url for the Ripple JSON-RPC API ledger (testnet)
        String url_ripple = "https://s.devnet.rippletest.net:51234/";

        try {
            URL url = new URL(url_ripple);
            Client client = new Client(url);

            // TODO: start the tool to do multiple passes of EA

            // For now, this is an example of an individual (a request)
            Individual ind = new Individual();

            int responseCode = client.createRequest("GET", ind.toReguest());

            // TODO: use output (HTTP response code and response JSON object for fitness)

            // Print the HTTP response code
            System.out.println(responseCode);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
