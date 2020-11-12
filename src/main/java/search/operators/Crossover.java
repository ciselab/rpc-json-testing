package search.operators;

import search.Individual;

import java.util.List;

public interface Crossover {

    public List<Individual> crossover(List<Individual> population);

}
