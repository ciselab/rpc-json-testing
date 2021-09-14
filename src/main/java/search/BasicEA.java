package search;

import search.genes.ArrayGene;
import search.objective.Fitness;
import util.Configuration;
import util.RandomSingleton;

import java.util.ArrayList;
import java.util.List;

import static util.RandomSingleton.getRandom;
import static util.RandomSingleton.getRandomBool;
import static util.RandomSingleton.getRandomIndex;

public class BasicEA {

    private Fitness fitness;
    private Generator generator;
    
    public BasicEA(Fitness fitness, Generator generator) {
        this.fitness = fitness;
        this.generator = generator;
    }

    public List<Individual> generatePopulation(int size) {
        List<Individual> population = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            population.add(generateRandomIndividual());
        }
        return population;
    }

    public Individual generateRandomIndividual() {
        int nRequests = RandomSingleton.getRandom().nextInt(Configuration.REQUESTS_GENERATOR_LIMIT) + 1;
        List<Chromosome> dna = new ArrayList<>();

        for (int i = 0; i < nRequests; i++) {
            String methodName = generator.getRandomMethod();
            ArrayGene method = generator.generateMethod(methodName);
            Chromosome chromosome = new Chromosome(generator.generateHTTPMethod(), methodName, method);
            dna.add(chromosome);
        }

        return new Individual(dna);
    }

    public List<Individual> nextGeneration(List<Individual> population) {
        List<Individual> offspring = new ArrayList<>();

        for (int i = 0; i < population.size(); i++) {
            if (getRandomBool(Configuration.ADD_NEW_RANDOM_INDIVIDUAL)) {
                offspring.add(generateRandomIndividual());
                continue;
            }

            Individual parent0 = population.get(i);
            Individual parent1 = population.get(getRandomIndex(population));

            String parent0String = parent0.toTotalJSONObject().toString();
            String parent1String = parent1.toTotalJSONObject().toString();

            Individual mutant;
            if (Configuration.CROSSOVER_ENABLED) {
                 mutant = parent0.crossover(parent1);
            } else {
                mutant = parent0;
            }

            for (int j = 0; j < Configuration.MUTATIONS_PER_INDIVIDUAL; j++) {
                mutant = mutant.mutate(generator);
            }

            String mutantString = mutant.toTotalJSONObject().toString();

            if (parent0String.equals(mutantString) || parent1String.equals(mutantString)) {
                mutant = generateRandomIndividual();
            }

            offspring.add(mutant);
        }

        offspring.addAll(population);

        // evaluate entire population
        fitness.evaluate(generator, offspring);

//        return elitistSelection(offspring);
        return tournamentSelection(offspring, Configuration.TOURNAMENT_SIZE);
    }

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
