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

    private String target;

    public Heuristic(Generator generator, TestDriver testDriver) {
        this.generator = generator;
        this.testDriver = testDriver;
    }

    public List<Individual> generatePopulation(int size) {
        List<Individual> population = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            population.add(generator.generateRandomIndividual());
        }
        return population;
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
                    testDriver.prepTest();

                    ResponseObject responseObject = null;

                    for (int j = 0; j < individual.getDna().size(); j++) {
                        Chromosome chromosome = individual.getDna().get(j);
                        responseObject = testDriver.runTest(chromosome.getHTTPMethod(), chromosome.toRequest());
                    }

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
    }

    public abstract List<Individual> nextGeneration(List<Individual> population);

    public Generator getGenerator() {
        return generator;
    }

    public TestDriver getTestDriver() {
        return testDriver;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
        this.generator.setTarget(target);
    }
}
