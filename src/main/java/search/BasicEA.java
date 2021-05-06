package search;

import search.genes.ArrayGene;
import search.objective.Fitness;
import search.openRPC.Specification;

import java.util.ArrayList;
import java.util.List;

import static util.RandomSingleton.getRandom;

public class BasicEA {

    private Fitness fitness;
    private Generator generator;

    private List<Individual> population;

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
        String methodName = generator.getRandomMethod();
        ArrayGene method = generator.generateMethod(methodName);

        return new Individual(generator.generateHTTPMethod(), methodName, method);
    }

    public List<Individual> nextGeneration(List<Individual> population) {
        // TODO generate offspring (mutation and crossover)
        List<Individual> offspring = new ArrayList<>();

        int mutations = 3;

        for (int i = 0; i < population.size(); i++) {
//            System.out.println("Ind: " + population.get(i).toRequest());
            String parent = population.get(i).toRequest().toString();
            Individual mutant = population.get(i);
            for (int j = 0; j < mutations; j++) {
                mutant = mutant.mutate(generator);
            }

            String mutantString = mutant.toRequest().toString();

            if (parent.equals(mutantString)) {
                mutant = generateRandomIndividual();
            }

            offspring.add(mutant);

        }

        offspring.addAll(population);

        // evaluate entire population
        fitness.evaluate(generator, offspring);

//        return elitistSelection(offspring);
        return tournamentSelection(offspring, 4);
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
