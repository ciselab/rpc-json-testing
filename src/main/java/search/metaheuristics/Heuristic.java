package search.metaheuristics;

import connection.ResponseObject;
import org.json.JSONObject;
import search.Chromosome;
import search.Generator;
import search.Individual;
import search.genes.ArrayGene;
import test_drivers.TestDriver;
import util.Configuration;
import util.RandomSingleton;

import java.util.ArrayList;
import java.util.List;

import static statistics.Collector.getCollector;

public abstract class Heuristic {

    private Generator generator;
    private TestDriver testDriver;

    public Heuristic(Generator generator, TestDriver testDriver) {
        this.generator = generator;
        this.testDriver = testDriver;
    }

    public List<Individual> generatePopulation(int size) {
        List<Individual> population = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            population.add(generateRandomIndividual());
        }
        return population;
    }

    public Individual generateRandomIndividual() {
        int nRequests = RandomSingleton.getRandom().nextInt(Configuration.REQUESTS_GENERATOR_LIMIT) + 1;
        List<Chromosome> dna = new ArrayList<>();

        for (int i = 0; i < nRequests; i++) {
            String methodName = generator.getRandomMethod();
            ArrayGene method = generator.generateMethod(methodName);
            Chromosome chromosome = new Chromosome(generator.generateHTTPMethod(), methodName, method);
            dna.add(chromosome);
        }

        return new Individual(dna);
    }


    /**
     * Get all responses from current generation of requests (i.e. individuals).
     *
     * @param population
     * @return list of ResponseObjects
     */
    public void gatherResponses(List<Individual> population) {
        for (Individual individual : population) {
            if (individual.hasResponseObject()) {
                continue;
            }

            if (testDriver.shouldContinue()) {

                try {
                    System.out.println("Preparing tests");
                    testDriver.prepTest();
                    System.out.println("Tests prepared");

                    ResponseObject responseObject = null;

                    for (int j = 0; j < individual.getDna().size(); j++) {
                        System.out.println("Chromosome " + j + " are sent to server.");
                        Chromosome chromosome = individual.getDna().get(j);
                        responseObject = testDriver.runTest(chromosome.getHTTPMethod(), chromosome.toRequest());
                        System.out.println("Chromosome " + j + " are successfully handled.");
                    }
                    System.out.println("Requests of individual are successfully handled.");

                    if (responseObject == null) {
                        ResponseObject ro = new ResponseObject("", new JSONObject(),-999, new JSONObject());
                        individual.setResponseObject(ro);
                        System.out.println("ResponseObject is null. This should never be the case!");
                        throw new Exception("Individual with zero chromosomes!!!");
                    }

                    individual.setResponseObject(responseObject);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                testDriver.checkWhetherToStop();
            }
        }
        System.out.println("Requests of all individuals of this generation are successfully processed.");
    }

    public abstract List<Individual> nextGeneration(List<Individual> population);

    public Generator getGenerator() {
        return generator;
    }

    public TestDriver getTestDriver() {
        return testDriver;
    }
}
