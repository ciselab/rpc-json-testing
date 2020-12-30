package search.objective;

import connection.Client;
import connection.ResponseObject;
import org.json.JSONObject;
import search.Generator;
import search.Individual;
import search.openRPC.Specification;
import search.genes.ArrayGene;

import java.io.IOException;
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
}
