package search.objective;

import connection.Client;
import connection.ResponseObject;
import search.Generator;
import search.Individual;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class Fitness {

    public Client getClient() {
        return client;
    }

    private Client client;

    public Fitness(Client client) {
        this.client = client;
    }

    public abstract void evaluate(Generator generator, Individual individual) throws IOException;

    public abstract void evaluate(Generator generator, List<Individual> population);

    /**
     * Get all responses from current generation of requests (i.e. individuals).
     * @param population
     * @return list of ResponseObjects
     */
    public List<ResponseObject> getResponses(List<Individual> population) {
        List<ResponseObject> responses = new ArrayList<>();

        for (int i = 0; i < population.size(); i++) {
            Individual individual = population.get(i);
            try {
                responses.add(getClient().createRequest(individual.getHTTPMethod(), individual.toRequest()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responses;
    }

}
