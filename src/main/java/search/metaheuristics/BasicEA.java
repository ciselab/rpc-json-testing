package search.metaheuristics;

import search.Generator;
import search.Individual;
import objective.Fitness;
import statistics.Collector;
import test_drivers.TestDriver;
import util.config.Configuration;
import util.config.SelectionType;

import java.util.ArrayList;
import java.util.List;

import static util.RandomSingleton.getRandom;
import static util.RandomSingleton.getRandomBool;
import static util.RandomSingleton.getRandomIndex;
import static util.config.Configuration.PROPORTION_MUTATED;

public class BasicEA extends Heuristic {

    private Fitness fitness;

    public BasicEA(Generator generator, TestDriver testDriver, Fitness fitness) {
        super(generator, testDriver);
        this.fitness = fitness;
    }

    public List<Individual> nextGeneration(List<Individual> population) {
        List<Individual> offspring = new ArrayList<>();

        for (int i = 0; i < population.size(); i++) {

            // Quit the process if time is up.
            if (!this.getTestDriver().shouldContinue()) {
                return population;
            }

            Individual mutant;

            if (getRandomBool(PROPORTION_MUTATED)) {

                if (getRandomBool(Configuration.ADD_NEW_RANDOM_INDIVIDUAL)) {
                    if (getRandomBool(Configuration.SAMPLE_FROM_ARCHIVE)) {
                        String key = (new ArrayList<>(Collector.getCollector().getArchive().keySet())).get(getRandomIndex(Collector.getCollector().getArchive().keySet()));
                        offspring.add(Collector.getCollector().getArchive().get(key));
                    } else {
                        offspring.add(generateRandomIndividual());
                    }
                    continue;
                }

                Individual parent0 = population.get(i);
                Individual parent1 = population.get(getRandomIndex(population));

                String parent0String = parent0.toTotalJSONObject().toString();
                String parent1String = parent1.toTotalJSONObject().toString();

                if (Configuration.CROSSOVER_ENABLED) {
                    mutant = parent0.crossover(parent1);
                } else {
                    mutant = parent0;
                }

                for (int j = 0; j < Configuration.MUTATIONS_PER_INDIVIDUAL; j++) {
                    mutant = mutant.mutate(getGenerator());
                }

                String mutantString = mutant.toTotalJSONObject().toString();

                if (parent0String.equals(mutantString) || parent1String.equals(mutantString)) {
                    mutant = generateRandomIndividual();
                }
            } else {
                mutant = generateRandomIndividual();
            }

            offspring.add(mutant);
        }

        offspring.addAll(population);

        this.gatherResponses(offspring);

        // Quit the process if time is up.
        if (!this.getTestDriver().shouldContinue()) {
            return population;
        }

        // evaluate entire population
        fitness.evaluate(getGenerator(), offspring);


        if (Configuration.SELECTION_TYPE == SelectionType.TOURNAMENT) {
            return tournamentSelection(offspring, Configuration.TOURNAMENT_SIZE);
        } else if (Configuration.SELECTION_TYPE == SelectionType.ELITIST) {
            return elitistSelection(offspring);
        }
        throw new IllegalArgumentException("Unsupported selection type");
    }

    /**
     * Selection based on Tournament Selection.
     *
     * @param population
     * @param tournamentSize
     * @return the next population of individuals
     */
    private List<Individual> tournamentSelection(List<Individual> population, int tournamentSize) {
        // select next generation
        List<Individual> newPopulation = new ArrayList<>();

        int champions = tournamentSize / 2;

        while (!population.isEmpty()) {
            List<Individual> tournament = new ArrayList<>();

            for (int i = 0; i < tournamentSize; i++) {
                if (population.isEmpty()) {
                    break;
                }

                tournament.add(population.remove(getRandom().nextInt(population.size())));

                System.out.println("Population size = " + population.size());
            }

            // Sort by descending order
            tournament.sort((o1, o2) -> Double.compare(o2.getFitness(), o1.getFitness()));

            for (int i = 0; i < champions; i++) {
                if (tournament.size() - 1 <= i) {
                    break;
                }
                newPopulation.add(tournament.get(i));
            }
        }

        return newPopulation;
    }

    /**
     * Selection based on Elitist Selection.
     *
     * @param population
     * @return the next population of individuals
     */
    private List<Individual> elitistSelection(List<Individual> population) {
        // Sort
        population.sort((o1, o2) -> Double.compare(o2.getFitness(), o1.getFitness()));

        // select next generation
        List<Individual> newPopulation = new ArrayList<>();

        for (int i = 0; i < population.size(); i++) {
            newPopulation.add(population.get(i));
        }

        return newPopulation;
    }

}
