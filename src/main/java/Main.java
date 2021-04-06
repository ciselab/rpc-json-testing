import connection.Client;
import org.json.JSONObject;
import search.BasicEA;
import search.Generator;
import search.Individual;
import search.objective.ResponseFitnessClustering;
import search.objective.ResponseFitnessClustering2;
import search.openRPC.Specification;
import test_generation.TestWriter;
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
            String url_ripple = "http://localhost:8080";
            URL url = new URL(url_ripple);
            Client client = new Client(url);

            ResponseFitnessClustering fitness = new ResponseFitnessClustering(client);

            BasicEA ea = new BasicEA(fitness, generator);
            List<Individual> population = ea.generatePopulation(50);

            for (int i = 0; i < 20; i++) {
                System.out.println("Generation: " + i);
                population = ea.nextGeneration(population);
            }

            fitness.printResults();

            String testDirectory = System.getProperty("user.dir") + "/src/test/java/generated";
            TestWriter testWriter = new TestWriter(url_ripple, testDirectory);

            for (int i = 0; i < population.size(); i++) {
                testWriter.writeTest(population.get(i), "ind" + i + "Test");
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
