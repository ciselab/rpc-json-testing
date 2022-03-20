package search.metaheuristics;

import connection.ResponseObject;
import org.json.JSONObject;
import search.Generator;
import search.Individual;
import test_drivers.TestDriver;
import util.config.Configuration;

import java.util.ArrayList;
import java.util.List;

import static statistics.Collector.getCollector;
import static util.config.Configuration.MUTATIONS_PER_INDIVIDUAL;
import static util.config.Configuration.PROPORTION_MUTATED;
import static util.ObjectStripper.stripValues;
import static util.RandomSingleton.getRandomBool;

public class RandomFuzzer extends Heuristic {

    public RandomFuzzer(Generator generator, TestDriver testDriver) {
        super(generator, testDriver);
    }

    public List<Individual> nextGeneration(List<Individual> population) {

        List<Individual> nextPopulation = new ArrayList<>();

        // Part of the next generation consists of the existing individuals mutated, the other part is newly generated.
        for (Individual original : population) {
            System.out.println("old");
            System.out.println(original);
            Individual mutant;
            if (getRandomBool(PROPORTION_MUTATED)) {
                System.out.println("Mutated");
                mutant = original.mutate(getGenerator());
//                int count = 2;
//                while (mutant.toString().equals(original.toString())) {
//                    System.out.println("Mutation: " + count);
//
//                    mutant = original.mutate(getGenerator());
//                    count ++;
//                }
                if (mutant.toString().equals(original.toString())) {
                    mutant = generateRandomIndividual();
                }
            } else {
                mutant = generateRandomIndividual();
            }


            System.out.println("new");
            System.out.println(mutant);
            System.out.println();

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
