package search.objective;

import connection.Client;
import search.Individual;
import search.openRPC.Specification;

import java.util.List;

import static util.RandomSingleton.getRandom;

/**
 * RandomFitness creates random fitness values for individuals (based on Gaussian distribution).
 */
public class RandomFitness extends Fitness {
    public RandomFitness(Client client) {
        super(client);
    }

    @Override
    public void evaluate(Specification specification, Individual individual) {
        runTest(specification, individual);
        individual.setFitness(getRandom().nextGaussian());
    }

    @Override
    public void evaluate(Specification specification, List<Individual> population) {
        for (int i = 0; i < population.size(); i++) {
            evaluate(specification, population.get(i));
        }
    }
}
