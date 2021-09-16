import connection.Client;
import org.json.JSONObject;
import search.metaheuristics.BasicEA;
import search.Generator;
import search.Individual;
import search.metaheuristics.Heuristic;
import search.metaheuristics.RandomFuzzer;
import search.objective.DiversityBasedFitness;
import search.objective.Fitness;
import search.objective.RandomFitness;
import search.objective.ResponseFitnessClustering;
import search.objective.ResponseFitnessClustering2;
import search.objective.ResponseFitnessPredefinedTypes;
import search.objective.ResponseStructureFitness;
import search.objective.ResponseStructureFitness2;
import search.objective.StatusCodeFitness;
import search.openRPC.Specification;
import test_drivers.GanacheTestDriver;
import test_drivers.RippledTestDriver;
import test_drivers.RippledTestDriverTestNet;
import test_drivers.TestDriver;
import test_generation.TestWriter;
import util.Configuration;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static util.IO.readFile;
import static util.IO.writeFile;

public class Main {

    private static ArrayList<Double> bestFitness = new ArrayList<>();

    public static void main(String args[]) {

        // Read the input arguments (heuristic, running time, server).
        String fitnessFunction = "8";
        int runningTime = 5; //default value
        String server = "";

        try {
            fitnessFunction = args[0]; // 1, 2, 3, 4, 5, 6, 7 or 8, default is 1
            runningTime = Integer.parseInt(args[1]); // time in minutes, default is 1 hour
            server = args[2];
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Argument(s) not specified. Default value(s) used.");
        } catch (NumberFormatException e) {
            System.out.println("Time limit argument is not an integer. Default time limit used: 24 hours.");
        }

        // Set the specification and the url for the server to be used.
        File jar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        String directory = jar.getParentFile().getAbsolutePath();
        String filepath;
        String url_server;
        if (server.equals("g")) {
            filepath = directory + System.getProperty("file.separator") + "ethereum-openrpc.json";
            url_server = "http://127.0.0.1:8545";
        } else if (server.equals("r")) {
            filepath = directory + System.getProperty("file.separator") + "ripple-openrpc.json";
            url_server = "http://127.0.0.1:5005";
        } else {
            filepath = directory + System.getProperty("file.separator") + "ripple-openrpc.json";
            url_server = "https://s.altnet.rippletest.net:51234"; // The url for the Ripple JSON-RPC API ledger (testnet)
        }

        // Read the OpenRPC specification that will be used.
        Specification specification = null;
        try {
            String data = readFile(filepath);
            specification = new Specification(new JSONObject(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Generator generator = new Generator(specification);

        Long runTime = new Long(runningTime * 60 * 1000);

        try {
            URL url = new URL(url_server);
            Client client = new Client(url);

            TestDriver testDriver;
            String testDriverString;
            if (server.equals("g")) {
                System.out.println("Using g: Ganache server");
                testDriver = new GanacheTestDriver(client, runTime);
                testDriverString = "GanacheTestDriver";
            } else if (server.equals("r")) {
                System.out.println("Using r: Rippled server");
                testDriver = new RippledTestDriver(client, runTime);
                testDriverString = "RippledTestDriver";
            } else {
                System.out.println("No or invalid argument specified for server. Using default server: Rippled TestNet");
                testDriver = new RippledTestDriverTestNet(client, runTime);
                testDriverString = "RippledTestDriverTestNet";
            }

            // Create the selected fitness function object.
            Fitness fitness;
            switch (fitnessFunction) {
                case "1":
                    System.out.println("Using 1: RandomFitness");
                    fitness = new RandomFitness();
                    break;
                case "2":
                    System.out.println("Using 2: StatusCodeFitness");
                    fitness = new StatusCodeFitness();
                    break;
                case "3":
                    System.out.println("Using 3: ResponseFitnessPredefinedTypes");
                    fitness = new ResponseFitnessPredefinedTypes();
                    break;
                case "4":
                    System.out.println("Using 4: ResponseFitnessClustering");
                    fitness = new ResponseFitnessClustering();
                    break;
                case "5":
                    System.out.println("Using 5: ResponseFitnessClustering2");
                    fitness = new ResponseFitnessClustering2();
                    break;
                case "6":
                    System.out.println("Using 6: ResponseStructureFitness2");
                    fitness = new ResponseStructureFitness2();
                    break;
                case "7":
                    System.out.println("Using 7: ResponseStructureFitness3");
                    fitness = new ResponseStructureFitness3();
                    break;
                case "8":
                    System.out.println("Using 8: DiversityBasedFitness");
                    fitness = new DiversityBasedFitness();
                    break;
                default:
                    System.out.println("No or invalid argument specified for fitness. Using default fitness: RandomFitness");
                    fitness = new RandomFitness();
            }

            System.out.println("Experiment will run for " + runningTime + " minute(s) = " + ((double) runningTime / 60) + " hour(s)");

            BasicEA ea = new BasicEA(generator, testDriver, fitness);
            List<Individual> population = ea.generatePopulation(Configuration.POPULATION_SIZE);

            // Stopping criterium = time
            int generation = 0;
            while (testDriver.shouldContinue()) {
                System.out.println("Starting generation: " + generation + ", " + (testDriver.getTimeLeft() / 1000) + " seconds left");
                generation += 1;
                population = ea.nextGeneration(population);

                // Keeping records of the highest fitness in each generation.
                double maxFitness = 0;
                for (Individual ind : population) {
                    if (ind.getFitness() > maxFitness) {
                        maxFitness = ind.getFitness();
                    }
                }
                bestFitness.add(maxFitness);
            }

            // Information on how the fitness function is progressing
            writeFile(fitness.storeInformation(), "fitness_progress.txt");
            // Information on status codes that occurred
            writeFile(fitness.getStatusCodesTotal().toString(), "status_codes_total.txt");
            writeFile(fitness.getStatusCodesArchive().toString(), "status_codes_archive.txt");
            // Information on the amount of tests in the archive
            List<Individual> archive = fitness.getArchive();
            String testInArchive = "Amount of tests in the archive: " + archive.size() + ", stopped at generation: " + generation;
            writeFile(testInArchive, "archive_size.txt");
            // Write best fitness values of each generation to file
            writeFile(bestFitness.toString(), "best_fitness_values.txt");

            // Delete old test files and write archive to tests
            String testDirectory = System.getProperty("user.dir") + "/src/test/java/generated";
            TestWriter testWriter = new TestWriter(url_server, testDirectory, testDriverString);
            for (File file : new java.io.File(testDirectory).listFiles()) {
                if (!file.isDirectory()) {
                    file.delete();
                }
            }
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
