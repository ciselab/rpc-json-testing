package search.operators;

import search.Individual;

import java.util.List;

public interface Selection {

    public List<Individual> selectNextGeneration(List<Individual> population);

}
