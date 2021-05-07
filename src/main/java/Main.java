import connection.Client;
import org.json.JSONObject;
import search.BasicEA;
import search.Generator;
import search.Individual;
import search.objective.Fitness;
import search.objective.RandomFitness;
import search.objective.ResponseFitnessClustering;
import search.objective.ResponseFitnessClustering2;
import search.objective.ResponseFitnessPredefinedTypes;
import search.objective.ResponseStructureFitness;
import search.objective.ResponseStructureFitness2;
import search.objective.ResponseStructureFitness3;
import search.objective.StatusCodeFitness;
import search.openRPC.Specification;
import test_generation.TestWriter;
import util.IO;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String args[]) {
        String fitnessFunction = "";
        int runningTime = 24 * 60 * 60 * 1000; //default value
        try {
            fitnessFunction = args[0]; // 1, 2, 3, 4, 5, 6, 7 or 8, default is 1
            runningTime = Integer.parseInt(args[1]); // time in milliseconds, default is 24 hours
        }
        catch (ArrayIndexOutOfBoundsException e){
            System.out.println("Argument(s) not specified. Default value(s) used.");
        }
        catch (NumberFormatException e){
            System.out.println("Time limit argument is not an integer. Default time limit used: 24 hours.");
        }

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
            Fitness fitness;

            switch (fitnessFunction) {
                case "1":
                    System.out.println("Using RandomFitness");
                    fitness = new RandomFitness(client);
                    break;
                case "2":
                    System.out.println("Using StatusCodeFitness");
                    fitness = new StatusCodeFitness(client);
                    break;
                case "3":
                    System.out.println("Using ResponseFitnessPredefinedTypes");
                    fitness = new ResponseFitnessPredefinedTypes(client);
                    break;
                case "4":
                    System.out.println("Using ResponseFitnessClustering");
                    fitness = new ResponseFitnessClustering(client);
                    break;
                case "5":
                    System.out.println("Using ResponseFitnessClustering2");
                    fitness = new ResponseFitnessClustering2(client);
                    break;
                case "6":
                    System.out.println("Using ResponseStructureFitness");
                    fitness = new ResponseStructureFitness(client);
                    break;
                case "7":
                    System.out.println("Using ResponseStructureFitness2");
                    fitness = new ResponseStructureFitness2(client);
                    break;
                case "8":
                    System.out.println("Using ResponseStructureFitness3");
                    fitness = new ResponseStructureFitness3(client);
                    break;
                default:
                    System.out.println("No or invalid argument specified. Using default fitness: RandomFitness");
                    fitness = new RandomFitness(client);
            }
            System.out.println("Experiment will run for " + runningTime + " ms = " + ((double) runningTime/1000/60) + " minute(s) = " + ((double) runningTime/1000/60/60) + " hour(s)");

            BasicEA ea = new BasicEA(fitness, generator);
            List<Individual> population = ea.generatePopulation(50);

            // stopping criterium: time
            Long startTime = System.currentTimeMillis();
            int generation = 0;
            while (System.currentTimeMillis() - startTime < runningTime) {
//                fitness.printResults();
                System.out.println("Generation: " + generation);
                generation += 1;
                long start = System.nanoTime();
                population = ea.nextGeneration(population);
//                System.out.println("Generation time: " + ((System.nanoTime() - start) / 1000000d));

            }

            // stopping criterium: generations
//            for (int i = 0; i < 20; i++) {
//                System.out.println("Generation: " + i);
//                population = ea.nextGeneration(population);
//            }

//            ((ResponseFitnessClustering2) fitness).printResults();

            // Write tests for the best individuals
            String testDirectory = System.getProperty("user.dir") + "/src/test/java/generated";
            TestWriter testWriter = new TestWriter(url_ripple, testDirectory);

            // Delete old test files
            for (File file : new java.io.File(testDirectory).listFiles()) {
                if (!file.isDirectory())
                    file.delete();
            }

            List<Individual> archive = fitness.getArchive();
            System.out.println("Tests in the archive: " + archive.size());
            for (int i = 0; i < archive.size(); i++) {
                testWriter.writeTest(archive.get(i), "ind" + i + "Test");
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
