package search.metaheuristics;

import connection.ResponseObject;
import openRPC.ResultSpecification;
import org.json.JSONObject;
import search.Generator;
import search.Individual;
import search.genes.ArrayGene;
import search.genes.MethodGene;
import test_drivers.TestDriver;

import java.util.*;

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
        String methodName = generator.getRandomMethod();
        ArrayGene method = generator.generateMethod(methodName);
        ResultSpecification resultSpecification = generator.getSpecification().getMethodResults().get(methodName);

        MethodGene root = new MethodGene(generator.generateHTTPMethod(), methodName, method, resultSpecification);

        return new Individual(root);
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

            if (!testDriver.shouldContinue()) {
                System.out.println("Requests of part of the individuals of this generation are successfully processed.");
                return;
            }

            Stack<MethodGene> requests = individual.getRequest();

            try {
                System.out.println("Preparing tests");
                testDriver.prepareTest();
                System.out.println("Tests prepared");

                ResponseObject responseObject = null;

                Map<MethodGene, JSONObject> responses = new HashMap<>();

                while (!requests.isEmpty()) {
                    MethodGene request = requests.pop();

                    responseObject = testDriver.runTest(request.getHttpMethod(), request.toJSON(responses));

                    responses.put(request, responseObject.getResponseObject());
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
