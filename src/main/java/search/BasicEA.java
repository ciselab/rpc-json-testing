package search;

import search.genes.ArrayGene;
import search.objective.Fitness;
import search.openRPC.Specification;

import java.util.ArrayList;
import java.util.List;

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
            String methodName = generator.getRandomMethod();
            ArrayGene method = generator.generateMethod(methodName);

            Individual individual = new Individual(generator.generateHTTPMethod(), methodName, method);
            population.add(individual);
        }
        return population;
    }

    public List<Individual> nextGeneration(List<Individual> population) {
        // TODO generate offspring (mutation and crossover)
        List<Individual> offspring = new ArrayList<>();

        for (int i = 0; i < population.size(); i++) {
            offspring.add(population.get(i).mutate(generator));
        }

        offspring.addAll(population);

        // evaluate entire population
        fitness.evaluate(generator, offspring);

        // Sort
        offspring.sort((o1, o2) -> Double.compare(o2.getFitness(), o1.getFitness()));

        // select next generation
        List<Individual> newPopulation = new ArrayList<>();

        for (int i = 0; i < population.size(); i++) {
            newPopulation.add(offspring.get(i));
        }

        return newPopulation;
    }
}
