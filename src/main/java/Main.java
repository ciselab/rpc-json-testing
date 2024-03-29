import connection.Client;

import objective.*;
import search.Generator;
import search.Individual;
import search.metaheuristics.BasicEA;
import search.metaheuristics.Heuristic;
import search.metaheuristics.RandomFuzzer;

import openRPC.Specification;

import statistics.Archive;
import statistics.CoverageRecorder;
import statistics.MethodCoverage;

import test_drivers.GanacheTestDriver;
import test_drivers.RippledTestDriver;
import test_drivers.RippledTestDriverTestNet;
import test_drivers.TestDriver;
import test_generation.TestWriter;

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
import static util.config.Configuration.*;

public class Main {

    private static String url_server;
    private static String testDriverString;
    private static ArrayList<Double> bestFitness = new ArrayList<>();

    public static void main(String args[]) {

        readArguments(args);
        Generator generator = setGenerator(); // Specify the generator based on the grammar of the OPENRPC specification
        TestDriver testDriver = setTestDriver(); // Specify the testDriver to run tests suitable for the selected server
        Heuristic heuristic = setHeuristic(generator, testDriver); // Create the heuristic to be used

        System.out.println("BUDGET: " + BUDGET_TYPE + " amount = " + BUDGET
                + " | POPULATION_SIZE: " + POPULATION_SIZE
                + " | PROPORTION_MUTATED: " + PROPORTION_MUTATED
                + " | CHANGE_TYPE_PROB: " + CHANGE_TYPE_PROB
                + " | NEW_CLUSTERS_AFTER_GEN: " + NEW_CLUSTERS_AFTER_GEN);

        try {
            List<Individual> population = heuristic.generatePopulation(POPULATION_SIZE); // the first generation
            heuristic.gatherResponses(population); // process requests and responses of first generation

            while (testDriver.shouldContinue()) {
                population = nextGeneration(testDriver, heuristic, population); // move onto the next generation
            }

            collectStatistics();
            createTestFiles();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Read the input arguments (heuristic, running time, server, mutation proportion).
     * @param args
     */
    public static void readArguments(String args[]) {
        try {
            HEURISTIC = Integer.parseInt(args[0]); // 1-8
            BUDGET = Integer.parseInt(args[1]); // Time in minutes, number of evals or generations
            String budgetType = args[2]; // Time, evals or generations
            if (budgetType.equals("mins") || budgetType.equals("m") || budgetType.equals("time") || budgetType.equals("t")) {
                BUDGET_TYPE = BUDGET_TYPE.TIME;
            } else if (budgetType.equals("evals") || budgetType.equals("e")) {
                BUDGET_TYPE = BUDGET_TYPE.EVALUATIONS;
            } else {
                BUDGET_TYPE = BUDGET_TYPE.GENERATION;
            }
            SERVER = args[3]; // r or g
            POPULATION_SIZE = Integer.parseInt(args[4]);
            PROPORTION_MUTATED = Double.parseDouble(args[5]);
            CHANGE_TYPE_PROB = Double.parseDouble(args[6]);
            NEW_CLUSTERS_AFTER_GEN = Integer.parseInt(args[7]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Argument(s) not specified. Default value(s) used.");
        } catch (NumberFormatException e) {
            System.out.println("Time limit argument is not an integer. Default time limit used: 24 hours.");
        }
    }

    /**
     * Create the Generator object based on the specification for the server to be used.
     */
    public static Generator setGenerator() {
        File jar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        String directory = jar.getParentFile().getAbsolutePath();
        String filepath;

        if (SERVER.equals("g")) {
            filepath = directory + System.getProperty("file.separator") + "ethereum-openrpc.json";
            url_server = "http://127.0.0.1:8545";
        } else if (SERVER.equals("r")) {
            filepath = directory + System.getProperty("file.separator") + "ripple-openrpc.json";
            url_server = "http://127.0.0.1:5005";
        } else if (SERVER.equals("r2")) {
            filepath = directory + System.getProperty("file.separator") + "ripple-openrpc-small.json";
            url_server = "http://127.0.0.1:5005";
        } else {
            filepath = directory + System.getProperty("file.separator") + "ripple-openrpc-small.json";
            url_server = "https://s.altnet.rippletest.net:51234"; // The url for the Ripple JSON-RPC API ledger (testnet)
        }

        // Read the OpenRPC specification that will be used
        Specification specification = null;
        try {
            String data = readFile(filepath);
            specification = new Specification(new JSONObject(data));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Generator generator = new Generator(specification);

        return generator;
    }

    /**
     * Create the TestDriver object based on the selected server.
     * @return testDriver
     */
    public static TestDriver setTestDriver() {
        TestDriver testDriver = null;

        try {
            URL url = new URL(url_server);
            Client client = new Client(url);
            CoverageRecorder coverageRecorder = new CoverageRecorder();

            if (SERVER.equals("g")) {
                System.out.println("SERVER: Ganache server");
                testDriver = new GanacheTestDriver(client, coverageRecorder);
                testDriverString = "GanacheTestDriver";
            } else if (SERVER.equals("r") || SERVER.equals("r2")) {
                System.out.println("SERVER: Rippled server");
                testDriver = new RippledTestDriver(client, coverageRecorder);
                testDriverString = "RippledTestDriver";
            } else {
                System.out.println("No or invalid argument specified for server. Using default server: Rippled TestNet");
                testDriver = new RippledTestDriverTestNet(client, coverageRecorder);
                testDriverString = "RippledTestDriverTestNet";
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return testDriver;
    }

    /**
     * Create the heuristic object based on input arguments.
     * @param generator
     * @param testDriver
     */
    public static Heuristic setHeuristic(Generator generator, TestDriver testDriver) {
        Heuristic heuristic;
        Fitness fitness;

        switch (HEURISTIC) {
            case 1:
                System.out.println("Using 1: RandomFuzzer");
                heuristic = new RandomFuzzer(generator, testDriver);
                break;
            case 2:
                System.out.println("Using 2: StatusCodeFitness");
                fitness = new StatusCodeFitness();
                heuristic = new BasicEA(generator, testDriver, fitness);
                break;
            case 3:
                System.out.println("Using 3: ResponseSkeletonFitness");
                fitness = new ResponseSkeletonFitness();
                heuristic = new BasicEA(generator, testDriver, fitness);
                break;
            case 4:
                System.out.println("Using 4: ResponseSkeletonAndRequestComplexityFitness");
                fitness = new ResponseSkeletonAndRequestComplexityFitness();
                heuristic = new BasicEA(generator, testDriver, fitness);
                break;
            case 5:
                System.out.println("Using 5: ResponseFitnessPredefinedTypes");
                fitness = new ResponseFitnessPredefinedTypes();
                heuristic = new BasicEA(generator, testDriver, fitness);
                break;
            case 6:
                System.out.println("Using 6: ResponseFitnessClustering");
                fitness = new ResponseFitnessClustering();
                heuristic = new BasicEA(generator, testDriver, fitness);
                break;
            case 7:
                System.out.println("Using 7: ResponseFitnessClustering2");
                fitness = new ResponseFitnessClustering2();
                heuristic = new BasicEA(generator, testDriver, fitness);
                break;
            case 8:
                System.out.println("Using 8: DiversityBasedFitness");
                fitness = new DiversityBasedFitness();
                heuristic = new BasicEA(generator, testDriver, fitness);
                break;
            default:
                System.out.println("No or invalid argument specified for fitness. Using default heuristic: RandomFuzzer");
                heuristic = new RandomFuzzer(generator, testDriver);
        }

        return heuristic;
    }

    /**
     * Process the next generation of individuals.
     * @param testDriver
     * @param heuristic
     * @param population
     * @return the newly created generation.
     */
    public static List<Individual> nextGeneration(TestDriver testDriver, Heuristic heuristic, List<Individual> population) {
        getCollector().nextGeneration();
        population = heuristic.nextGeneration(population);

        System.out.println("Generation: " + (getCollector().getGeneration()-1) + " was processed!");

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

        testDriver.nextGeneration();
        return population;
    }

    /**
     * Write statistics to text files.
     * @throws IOException
     */
    public static void collectStatistics() throws IOException {
        Map<String, MethodCoverage> coverage = getCollector().getInternalCoverage();
        for (String method : coverage.keySet()) {
            System.out.println(method);
            System.out.println(coverage.get(method).statuses);
            System.out.println(coverage.get(method).structures.keySet());
        }

        (new File(testDirectory)).mkdir();

        // Information on API methods that occurred
        writeFile(getCollector().getMethodCountTotal().toString(), "methods_total.txt", false);
        writeFile(getCollector().getMethodCountArchive().toString(), "methods_archive.txt", false);
        writeFile(getCollector().getMethodCountPerGen().toString(), "methods_per_gen.txt", false);

        // Information on status codes that occurred
        writeFile(getCollector().getStatusCodesTotal().toString(), "status_codes_total.txt", false);
        writeFile(getCollector().getStatusCodesArchive().toString(), "status_codes_archive.txt", false);
        writeFile(getCollector().getStatusCodesPerGen().toString(), "status_codes_per_gen.txt", false);

        // Information on the amount of tests in the archive
        Archive archive = getCollector().getArchive();
        String testInArchive = "Amount of tests in the archive: " + archive.size()
                + ", stopped at generation: " + getCollector().getGeneration()
                + " | SERVER: " + SERVER
                + " | BUDGET: " + BUDGET + " " + BUDGET_TYPE
                + " | POPULATION: " + POPULATION_SIZE
                + " | PROPORTION_MUTATED: " + PROPORTION_MUTATED
                + " | NEW_CLUSTERS_AFTER_GEN: " + NEW_CLUSTERS_AFTER_GEN
                + " | CHANGE_TYPE_PROB: " + CHANGE_TYPE_PROB;
        writeFile(testInArchive, "archive_size.txt", false);
    }

    /**
     * Delete old test files and write archive to tests.
     */
    public static void createTestFiles() {
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
            try {
                testWriter.writeTest(archive.get(key), "ind" + i + "Test");
            } catch (IOException e) {
                e.printStackTrace();
            }
            i++;
        }
    }
}
