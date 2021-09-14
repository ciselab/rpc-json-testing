package search;

import org.json.JSONObject;
import search.genes.ArrayGene;
import search.openRPC.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static util.RandomSingleton.getRandom;
import static util.RandomSingleton.getRandomBool;

public class Individual {
    private List<Chromosome> dna;
    private Integer age;
    private double fitness;

    public Individual(List<Chromosome> dna) {
        this.dna = dna;
        this.age = 0;
    }

    public JSONObject toTotalJSONObject() {
        JSONObject totalJson = new JSONObject();
        for (int i = 0; i < dna.size(); i++) {
            totalJson.put("" + i, dna.get(i).toRequest());
        }
        return totalJson;
    }

    /**
     * Mutate individual.
     * @param generator
     * @return mutated individual
     */
    public Individual mutate(Generator generator) {
        List<Chromosome> newDna = new ArrayList<>();

        for (int i = 0; i < dna.size(); i++) {
            // TODO maybe make this conditional on random var
            newDna.add(dna.get(i).mutate(generator));
        }

        return new Individual(newDna);
    }

    /**
     * When an individual lives through the next generation, it becomes a generation older.
     */
    public void birthday() {
        this.age = this.age + 1;
    }

    public int getAge() {
        return age;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public List<Chromosome> getDna() {
        return dna;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Individual that = (Individual) o;

        if (that.getDna().size() != this.getDna().size()) {
            return false;
        }

        for (int i = 0; i < dna.size(); i++) {
            if (!dna.get(i).equals(that.getDna().get(i))) {
                return false;
            }
        }

        return true;
    }

}
