package search.objective;

import connection.Client;
import search.Individual;
import search.openRPC.Specification;
import search.genes.ArrayGene;

import java.io.IOException;
import java.util.List;

public abstract class Fitness {

    private Client client;

    public Fitness(Client client) {
        this.client = client;
    }

    public void runTest(Specification specification, Individual individual) {

        int responseCode = 0;
        try {
            responseCode = client.createRequest(individual.getHTTPMethod(), individual.toRequest());
            System.out.println(individual.toRequest().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // TODO: use output (HTTP response code and response JSON object for fitness)
    }

    public abstract void evaluate(Specification specification, Individual individual);

    public abstract void evaluate(Specification specification, List<Individual> population);
}
