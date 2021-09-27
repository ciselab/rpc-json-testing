package search.metaheuristics;

import search.Generator;
import search.Individual;
import test_drivers.TestDriver;
import util.Configuration;

import java.util.List;

import static statistics.Collector.getCollector;
import static util.RandomSingleton.getRandomBool;

public class RandomFuzzer extends Heuristic {

    private TestDriver testDriver;

    public RandomFuzzer(Generator generator, TestDriver testDriver) {
        super(generator, testDriver);
        this.testDriver = testDriver;
    }

    public List<Individual> nextGeneration(List<Individual> population) {

        List<Individual> nextPopulation = generatePopulation(population.size());

        this.gatherResponses(nextPopulation);

        for (Individual individual : nextPopulation) {
            if (getRandomBool(Configuration.ARCHIVE_THRESHOLD_RANDOM)) {
                getCollector().addToArchive(individual.getResponseObject().getResponseObject().toString(), individual);
            }
        }

        return nextPopulation;
    }

}
