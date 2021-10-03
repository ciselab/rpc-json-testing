package objective;

import search.Generator;
import search.Individual;

import java.util.List;

public abstract class Fitness {

    /**
     * Evaluate the population on fitness.
     * @param generator
     * @param population
     */
    public abstract void evaluate(Generator generator, List<Individual> population);

    /**
     * Store information regarding the process of the run.
     */
    public abstract List<String> storeInformation();

}
