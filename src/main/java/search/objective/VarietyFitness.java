package search.objective;

import connection.Client;
import search.Generator;
import search.Individual;

import java.util.List;

public class VarietyFitness extends Fitness {
    public VarietyFitness(Client client) {
        super(client);
    }

    @Override
    public void evaluate(Generator generator, Individual individual) {
        runTest(generator, individual);
        individual.setFitness(0);
    }

    @Override
    public void evaluate(Generator generator, List<Individual> population) {
        for (int i = 0; i < population.size(); i++) {
            evaluate(generator, population.get(i));
        }
    }
}
