package search;

import search.genes.Gene;

import java.util.ArrayList;
import java.util.List;

public class BasicEA {
    List<Individual> population;

    public BasicEA(int popSize, Fitness fitness) {
        this.population = generatePopulation(popSize);

    }

    public List<Individual> generatePopulation(int size) {
        List<Individual> population = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            population.add(new Individual());
        }
        return population;
    }

    public List<Individual> nextGeneration(List<Individual> population) {
        // TODO: generate offspring (mutation and crossover)
        // TODO: evaluate entire population
        // TODO: select next generation
        return null;
    }
}
