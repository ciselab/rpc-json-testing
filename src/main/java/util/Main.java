package util;

import connection.Client;

import objective.*;
import search.Chromosome;
import search.metaheuristics.BasicEA;
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

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static statistics.Collector.getCollector;

import static util.IO.readFile;
import static util.IO.writeFile;

public class Main {

    private static ArrayList<Double> bestFitness = new ArrayList<>();

    public static int GENERATION = 0;

    public static void main(String args[]) {

        // Read the input arguments (heuristic, running time, server).
        String fitnessFunction = "9";
        int runningTime = 5; //default value
        String server = "";

        try {
            fitnessFunction = args[0]; // 1, 2, 3, 4, 5, 6, 7 or 8, default is 1
            runningTime = Integer.parseInt(args[1]); // time in minutes, default is 1 hour
            server = args[2];
        } catch (ArrayIndexOutOfBoundsException e) {
            // // System.out.println("Argument(s) not specified. Default value(s) used.");
        } catch (NumberFormatException e) {
            // // System.out.println("Time limit argument is not an integer. Default time limit used: 24 hours.");
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
                // // System.out.println("Using g: Ganache server");
                testDriver = new GanacheTestDriver(client, runTime);
                testDriverString = "GanacheTestDriver";
            } else if (server.equals("r")) {
                // // System.out.println("Using r: Rippled server");
                testDriver = new RippledTestDriver(client, runTime);
                testDriverString = "RippledTestDriver";
            } else {
                // // System.out.println("No or invalid argument specified for server. Using default server: Rippled TestNet");
                testDriver = new RippledTestDriverTestNet(client, runTime);
                testDriverString = "RippledTestDriverTestNet";
            }

            // Create the selected heuristic and fitness function object.
            Heuristic heuristic;
            Fitness fitness = null;
            switch (fitnessFunction) {
                case "1":
                    System.out.println("Using 1: RandomFuzzer");
                    heuristic = new RandomFuzzer(generator, testDriver);
                    break;
                case "2":
                    System.out.println("Using 2: StatusCodeFitness");
                    fitness = new StatusCodeFitness();
                    heuristic = new BasicEA(generator, testDriver, fitness);
                    break;
                case "3":
                    System.out.println("Using 3: ResponseFitnessPredefinedTypes");
                    fitness = new ResponseFitnessPredefinedTypes();
                    heuristic = new BasicEA(generator, testDriver, fitness);
                    break;
                case "4":
                    System.out.println("Using 4: ResponseFitnessClustering");
                    fitness = new ResponseFitnessClustering();
                    heuristic = new BasicEA(generator, testDriver, fitness);
                    break;
                case "5":
                    System.out.println("Using 5: ResponseFitnessClustering2");
                    fitness = new ResponseFitnessClustering2();
                    heuristic = new BasicEA(generator, testDriver, fitness);
                    break;
                case "6":
                    System.out.println("Using 6: ResponseStructureFitness");
                    fitness = new ResponseStructureFitness();
                    heuristic = new BasicEA(generator, testDriver, fitness);
                    break;
                case "7":
                    System.out.println("Using 7: ResponseStructureFitness2");
                    fitness = new ResponseStructureFitness2();
                    heuristic = new BasicEA(generator, testDriver, fitness);
                    break;
                case "8":
                    System.out.println("Using 8: DiversityBasedFitness");
                    fitness = new DiversityBasedFitness();
                    heuristic = new BasicEA(generator, testDriver, fitness);
                    break;
                case "9":
                    System.out.println("Using 9: DistanceFitness");
                    fitness = new DistanceFitness();
                    heuristic = new BasicEA(generator, testDriver, fitness);
                    break;
                default:
                    // // System.out.println("No or invalid argument specified for fitness. Using default heuristic: RandomFuzzer");
                    heuristic = new RandomFuzzer(generator, testDriver);
            }

            ArrayList<Map<String, Integer>> methodsPerGeneration = new ArrayList<>();

            List<String> methods = new ArrayList<>(specification.getMethods().keySet());

            List<Individual> population = heuristic.generatePopulation(Configuration.POPULATION_SIZE);
            heuristic.gatherResponses(population);

            while (testDriver.shouldContinue()) {
                GENERATION += 1;
                  System.out.println("Starting generation: " + getCollector().getGeneration() + ", "
                        + (testDriver.getTimeLeft() / 1000) + " seconds = " + (testDriver.getTimeLeft() / (60 * 1000)) + " minutes left.");

                Map<String, Integer> methodsThisGeneration = new HashMap<>();
                methodsPerGeneration.add(methodsThisGeneration);

                getCollector().nextGeneration();
                population = heuristic.nextGeneration(population);

                for (Individual individual : population) {
                    Chromosome last = individual.getDna().get(individual.getDna().size() - 1);

                    if (!methodsThisGeneration.containsKey(last.getApiMethod())) {
                        methodsThisGeneration.put(last.getApiMethod(), 0);
                    }
                    methodsThisGeneration.put(last.getApiMethod(), methodsThisGeneration.get(last.getApiMethod()) + 1);
                }
                // Store some statistics for analysis purposes.

                if (testDriver.shouldContinue()) {
                    // // System.out.println("Storing statistics for the previous generation.");
                    double maxFitness = 0;
                    for (Individual ind : population) {

                        // Count methods
                        getCollector().countMethods(ind);
                        getCollector().countStatusCodes(ind);

//                        if (ind.getFitness() > maxFitness) {
//                            maxFitness = ind.getFitness();
//                        }
                    }
                    bestFitness.add(maxFitness);
                }
            }

            StringBuilder data = new StringBuilder();
            for (String method : methods) {
                data.append(method).append(",");
            }
            data.append("\n");
            for (Map<String, Integer> aMethodsPerGeneration : methodsPerGeneration) {
                for (String method : methods) {
                    if (aMethodsPerGeneration.containsKey(method)) {
                        data.append(aMethodsPerGeneration.get(method)).append(",");
                    } else {
                        data.append("0,");
                    }
                }
                data.append("\n");
            }

            writeFile(data.toString(), "methods_per_generation.csv");
            // Stopping criterium = time
//            while (testDriver.shouldContinue()) {
//                // System.out.println("Starting generation: " + getCollector().getGeneration() + ", " + (testDriver.getTimeLeft() / 1000) + " seconds left");
//
//                getCollector().nextGeneration();
//                population = heuristic.nextGeneration(population);
//
//                // Store some statistics for analysis purposes.
//                if (testDriver.shouldContinue()) {
//                    double maxFitness = 0;
//                    for (Individual ind : population) {
//
//                        // Count methods
//                        getCollector().countMethods(ind);
//                        getCollector().countStatusCodes(ind);
//
//                        if (ind.getFitness() > maxFitness) {
//                            maxFitness = ind.getFitness();
//                        }
//                    }
//                    bestFitness.add(maxFitness);
//                }
//            }

            Map<String, MethodCoverage> coverage = getCollector().getInternalCoverage();
            for (String method : coverage.keySet()) {
                // // System.out.println("Method: " + method);
                // // System.out.println("\t Statuses: " + coverage.get(method).statuses);
                // // System.out.println("\tStructures: " + coverage.get(method).structures.keySet().size());
            }

            if (fitness != null) {
                // Information on how the fitness function is progressing
                writeFile(fitness.storeInformation(), "fitness_progress.txt");

                // Write best fitness values of each generation to file
                writeFile(bestFitness.toString(), "best_fitness_values.txt");
            }

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

            // Delete old test files and write archive to tests
            String testDirectory = System.getProperty("user.dir") + "/src/test/java/generated";
            TestWriter testWriter = new TestWriter(url_server, testDirectory, testDriverString);
            for (File file : new java.io.File(testDirectory).listFiles()) {
                if (!file.isDirectory()) {
                    file.delete();
                }
            }
            // // System.out.println("Tests in the archive: " + archive.size());
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
}
