package search.operators;

import search.Individual;

import java.util.List;

public interface Mutation {

    public List<Individual> mutate(List<Individual> population);

}
