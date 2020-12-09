package search;

import search.genes.ArrayGene;
import search.objective.Fitness;
import search.openRPC.Specification;

import java.util.ArrayList;
import java.util.List;

public class BasicEA {

    private Specification specification;
    private Fitness fitness;

    private List<Individual> population;

    public BasicEA(Fitness fitness, Specification specification) {
        this.specification = specification;
        this.fitness = fitness;
    }

    public List<Individual> generatePopulation(int size) {
        List<Individual> population = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ArrayGene method = (ArrayGene) specification.getRandomOption();

            Individual individual = new Individual(specification.getGenerator().generateHTTPMethod(), method.getKey(), method);
            population.add(individual);
        }
        return population;
    }

    public List<Individual> nextGeneration(List<Individual> population) {
//        System.out.println("NEW GEN");
        // TODO generate offspring (mutation and crossover)
        List<Individual> offspring = new ArrayList<>();

        for (int i = 0; i < population.size(); i++) {
            offspring.add(population.get(i).mutate(specification));
        }

        offspring.addAll(population);

        // evaluate entire population
        fitness.evaluate(specification, offspring);

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
