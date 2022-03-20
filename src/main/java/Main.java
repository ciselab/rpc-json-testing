import connection.Client;

import search.Generator;
import search.Individual;
import search.metaheuristics.Heuristic;
import search.metaheuristics.RandomFuzzer;

import openRPC.Specification;
import statistics.Archive;
import statistics.MethodCoverage;

import test_drivers.GanacheTestDriver;
import test_drivers.RippledTestDriver;
import test_drivers.RippledTestDriverTestNet;
import test_drivers.TestDriver;
import test_generation.TestWriter;

import util.config.Configuration;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static statistics.Collector.getCollector;
import static util.IO.*;

public class Main {

    private static ArrayList<Double> bestFitness = new ArrayList<>();

    public static void main(String args[]) {

        // Read the input arguments (heuristic, running time, server).
        String fitnessFunction = "1";
        int runningTime = 1; //default value
        String server = "";

        try {
            fitnessFunction = args[0]; // 1, 2, 3, 4, 5, 6, 7 or 8, default is 1
            runningTime = Integer.parseInt(args[1]); // time in minutes, default is 1 hour
            server = args[2];
            Configuration.PROPORTION_MUTATED = Double.parseDouble(args[3]);
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
                testDriver = new GanacheTestDriver(client, runTime, true);
                testDriverString = "GanacheTestDriver";
            } else if (server.equals("r")) {
                System.out.println("Using r: Rippled server");
                testDriver = new RippledTestDriver(client, runTime, true);
                testDriverString = "RippledTestDriver";
            } else {
                System.out.println("No or invalid argument specified for server. Using default server: Rippled TestNet");
                testDriver = new RippledTestDriverTestNet(client, runTime, true);
                testDriverString = "RippledTestDriverTestNet";
            }

            // Create the selected heuristic and fitness function object.
            Heuristic heuristic;
            switch (fitnessFunction) {
                case "1":
                    System.out.println("Using 1: RandomFuzzer");
                    heuristic = new RandomFuzzer(generator, testDriver);
                    break;
                default:
                    System.out.println("No or invalid argument specified for fitness. Using default heuristic: RandomFuzzer");
                    heuristic = new RandomFuzzer(generator, testDriver);
            }

            System.out.println("Experiment will run for " + runningTime + " minute(s) = " + ((double) runningTime / 60) + " hour(s)");
            System.out.println("Mutation percentage: " + Configuration.PROPORTION_MUTATED);

            List<Individual> population = heuristic.generatePopulation(Configuration.POPULATION_SIZE);
            heuristic.gatherResponses(population);

            // Stopping criterium = time
            while (testDriver.shouldContinue()) {
                System.out.println("Starting generation: " + getCollector().getGeneration() + ", "
                    + (testDriver.getTimeLeft() / 1000) + " seconds = " + (testDriver.getTimeLeft() / (60*1000)) + " minutes left.");

                getCollector().nextGeneration();
                population = heuristic.nextGeneration(population);

                // Store some statistics for analysis purposes.
                System.out.println("Storing statistics for the previous generation.");
                if (testDriver.shouldContinue()) {
                    double maxFitness = 0;
                    for (Individual ind : population) {

                        // Count methods
                        getCollector().countMethods(ind);
                        getCollector().countStatusCodes(ind);

                        if (ind.getFitness() > maxFitness) {
                            maxFitness = ind.getFitness();
                        }
                    }
                    bestFitness.add(maxFitness);
                }
            }

            Map<String, MethodCoverage> coverage = getCollector().getInternalCoverage();
            for (String method : coverage.keySet()) {
                System.out.println(method);
                System.out.println(coverage.get(method).statuses);
                System.out.println(coverage.get(method).structures.keySet());
            }

            collectStatistics();

            // Delete old test files and write archive to tests
            String testDirectory = System.getProperty("user.dir") + "/src/test/java/generated";
            (new File(testDirectory)).mkdir();
            TestWriter testWriter = new TestWriter(url_server, testDirectory, testDriverString);
            for (File file : new java.io.File(testDirectory).listFiles()) {
                if (!file.isDirectory()) {
                    file.delete();
                }
            }

            Archive archive = getCollector().getArchive();
            System.out.println("Tests in the archive: " + archive.size());
            int i = 0;
            for (String key : archive.keySet()) {
                testWriter.writeTest(archive.get(key), "ind" + i + "Test");
                i++;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void collectStatistics() throws IOException {
        (new File(testDirectory)).mkdir();

        // Information on API methods that occurred
        writeFile(getCollector().getMethodCountTotal().toString(), "methods_total.txt");
        writeFile(getCollector().getMethodCountArchive().toString(), "methods_archive.txt");
        writeFile(getCollector().getMethodCountPerGen().toString(), "methods_per_gen.txt");

        // Information on status codes that occurred
        writeFile(getCollector().getStatusCodesTotal().toString(), "status_codes_total.txt");
        writeFile(getCollector().getStatusCodesArchive().toString(), "status_codes_archive.txt");
        writeFile(getCollector().getStatusCodesPerGen().toString(), "status_codes_per_gen.txt");

        // Information on the amount of tests in the archive
        Archive archive = getCollector().getArchive();
        String testInArchive = "Amount of tests in the archive: " + archive.size() + ", stopped at generation: " + getCollector().getGeneration();
        writeFile(testInArchive, "archive_size.txt");
    }
}
