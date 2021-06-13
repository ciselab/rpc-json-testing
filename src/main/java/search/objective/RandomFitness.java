package search.objective;

import connection.ResponseObject;
import search.Generator;
import search.Individual;
import test_drivers.TestDriver;

import java.util.List;

import static util.RandomSingleton.getRandom;

/**
 * RandomFitness creates random fitness values for individuals (based on Gaussian distribution).
 */
public class RandomFitness extends Fitness {
    private double ARCHIVE_THRESHOLD = 2.5;

    public RandomFitness(TestDriver testDriver) {
        super(testDriver);
    }

    @Override
    public void evaluate(Generator generator, List<Individual> population) {
        // Call methods
        List<ResponseObject> responseObjects = getResponses(population);

        for (int i = 0; i < population.size(); i++) {
            Individual individual = population.get(i);

            double fitness = getRandom().nextGaussian();
            individual.setFitness(fitness);

            // decide whether to add individual to the archive
            if (fitness >= ARCHIVE_THRESHOLD && !getArchive().contains(individual)) {
                this.addToArchive(individual);
            }
        }
    }

}
