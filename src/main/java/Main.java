import connection.Client;
import org.json.JSONObject;
import search.BasicEA;
import search.Generator;
import search.Individual;
import search.objective.RandomFitness;
import search.objective.ResponseFitnessClustering;
import search.objective.ResponseFitnessClustering2;
import search.objective.ResponseFitnessPredefinedTypes;
import search.objective.ResponseStructureFitness;
import search.objective.StatusCodeFitness;
import search.openRPC.Specification;
import test_generation.TestWriter;
import util.IO;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class Main {

    public static void main (String args[]) {

        File jar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        String directory = jar.getParentFile().getAbsolutePath();

        String filepath = directory + System.getProperty("file.separator") + "ripple-openrpc.json";

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
//            String url_ripple = "https://s.altnet.rippletest.net:51234";
            String url_ripple = "http://127.0.0.1:5005";
            URL url = new URL(url_ripple);
            Client client = new Client(url);

            StatusCodeFitness fitness = new StatusCodeFitness(client);

            BasicEA ea = new BasicEA(fitness, generator);
            List<Individual> population = ea.generatePopulation(50);

            Long startTime = System.currentTimeMillis();
            int generation = 0;
            while (System.currentTimeMillis() - startTime < 30 * 60 * 1000) {
                System.out.println("Generation: " + generation);
                generation += 1;
                population = ea.nextGeneration(population);
            }

//            for (int i = 0; i < 20; i++) {
//                System.out.println("Generation: " + i);
//                population = ea.nextGeneration(population);
//            }

//            fitness.printResults();

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
