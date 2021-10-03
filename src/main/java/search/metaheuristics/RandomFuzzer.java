package search.metaheuristics;

import connection.ResponseObject;
import org.json.JSONObject;
import search.Generator;
import search.Individual;
import test_drivers.TestDriver;

import java.util.ArrayList;
import java.util.List;

import static statistics.Collector.getCollector;
import static util.Configuration.MUTATIONS_PER_INDIVIDUAL;
import static util.Configuration.PROPORTION_MUTATED;
import static util.ObjectStripper.stripValues;
import static util.RandomSingleton.getRandomBool;

public class RandomFuzzer extends Heuristic {

    public RandomFuzzer(Generator generator, TestDriver testDriver) {
        super(generator, testDriver);
    }

    public List<Individual> nextGeneration(List<Individual> population) {

        List<Individual> nextPopulation = new ArrayList<>();

        // Part of the next generation consists of the existing individuals mutated, the other part is newly generated.
        for (int i = 0; i < population.size(); i++) {
            Individual mutant = population.get(i);
            if (getRandomBool(PROPORTION_MUTATED)) {
                for (int j = 0; j < MUTATIONS_PER_INDIVIDUAL; j++) {
                    mutant = mutant.mutate(getGenerator());
                }
            } else {
                mutant = generateRandomIndividual();
            }
            nextPopulation.add(mutant);
        }

        this.gatherResponses(nextPopulation);

        // Quit the process if time is up.
        if (getTestDriver().shouldContinue()) {
            for (Individual individual : nextPopulation) {
                ResponseObject responseObject = individual.getResponseObject();
                JSONObject stripped = stripValues(responseObject.getRequestObject(), responseObject.getResponseObject());
                String strippedString = stripped.toString();
                getCollector().addToArchive(strippedString, individual);
            }
        }

        return nextPopulation;
    }

}
