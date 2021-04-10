package search.objective;

import connection.Client;
import search.Generator;
import search.Individual;

import java.io.IOException;
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
    public void evaluate(Generator generator, Individual individual) throws IOException {
        getClient().createRequest(individual.getHTTPMethod(), individual.toRequest());
        individual.setFitness(getRandom().nextGaussian());
        System.out.println(individual.getHTTPMethod()+ " " + individual.getMethod() + " " + individual.toRequest().toString());
    }

    @Override
    public void evaluate(Generator generator, List<Individual> population) {
        for (int i = 0; i < population.size(); i++) {
            try {
                evaluate(generator, population.get(i));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
