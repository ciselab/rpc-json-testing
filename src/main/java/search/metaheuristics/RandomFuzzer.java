package search.metaheuristics;

import search.Generator;
import search.Individual;
import test_drivers.TestDriver;

import java.util.List;

public class RandomFuzzer extends Heuristic {

    private TestDriver testDriver;

    public RandomFuzzer(Generator generator, TestDriver testDriver) {
        super(generator, testDriver);
        this.testDriver = testDriver;
    }

    public List<Individual> nextGeneration(List<Individual> population) {

        List<Individual> nextPopulation = generatePopulation(population.size());

        this.gatherResponses(nextPopulation);

        return nextPopulation;
    }

}
