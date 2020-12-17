import connection.Client;
import org.json.JSONObject;
import search.BasicEA;
import search.Generator;
import search.Individual;
import search.objective.RandomFitness;
import search.openRPC.Specification;
import util.IO;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class Main {

    public static void main (String args[]) {

        ClassLoader classLoader = Main.class.getClassLoader();

        String filepath = classLoader.getResource("ripple-openrpc.json").getFile();
        Specification specification = null;

        try {
            String data = IO.readFile(filepath);
            specification = new Specification(new JSONObject(data));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Generator generator = new Generator(specification);

        try {
            // TODO (later): find other APIs to connect to
            // The url for the Ripple JSON-RPC API ledger (testnet)
            String url_ripple = "https://s.altnet.rippletest.net:51234";
            URL url = new URL(url_ripple);
            Client client = new Client(url);

            RandomFitness randomFitness = new RandomFitness(client);

            BasicEA ea = new BasicEA(randomFitness, generator);
            List<Individual> population = ea.generatePopulation(10);

            for (int i = 0; i < 10; i++) {
                population = ea.nextGeneration(population);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
