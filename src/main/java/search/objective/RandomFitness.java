package search.objective;

import connection.Client;
import connection.ResponseObject;
import search.Generator;
import search.Individual;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static util.RandomSingleton.getRandom;

/**
 * RandomFitness creates random fitness values for individuals (based on Gaussian distribution).
 */
public class RandomFitness extends Fitness {
    private double ARCHIVE_THRESHOLD = 2.5;

    public RandomFitness(Client client) {
        super(client);
    }

    @Override
    public void evaluate(Generator generator, Individual individual) throws IOException {
    }

    @Override
    public void evaluate(Generator generator, List<Individual> population) {
        List<ResponseObject> responses = new ArrayList<>();

        double averageEvalTime = 0;

        for (int i = 0; i < population.size(); i++) {
            Individual individual = population.get(i);
            long start = System.nanoTime();
            try {
                responses.add(getClient().createRequest(individual.getHTTPMethod(), individual.toRequest()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            double fitness = getRandom().nextGaussian();
            individual.setFitness(fitness);

            // decide whether to add individual to the archive
            if (fitness >= ARCHIVE_THRESHOLD && !archive.contains(individual)) {
                this.addToArchive(individual);
            }

            averageEvalTime += (System.nanoTime() - start);
        }

        averageEvalTime /= (population.size() * 1000000d);
        System.out.println("Average test time: " + averageEvalTime + " ms");
    }

}
