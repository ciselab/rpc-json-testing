package search;

import connection.ResponseObject;
import org.json.JSONObject;
import util.Configuration;
import util.CrossoverType;

import java.util.ArrayList;
import java.util.List;

import static util.RandomSingleton.getRandom;
import static util.RandomSingleton.getRandomBool;

public class Individual {
    private List<Chromosome> dna;
    private ResponseObject responseObject = null;
    private double fitness;

    public Individual(List<Chromosome> dna) {
        this.dna = dna;
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
            if (util.RandomSingleton.getRandomBool(1/dna.size())) {
                System.out.println("Chromosome " + i + " will be mutated.");
                newDna.add(dna.get(i).mutate(generator));
                System.out.println("Chromosome " + i + " was successfully mutated.");
            } else {
                newDna.add(dna.get(i));
            }
        }

        return new Individual(newDna);
    }

    public Individual crossover(Individual other) {
        // TODO always starts with the short individual (bad, should be random)
        List<Chromosome> mixedDna = new ArrayList<>();

        List<Chromosome> shortParent = this.getDna();
        List<Chromosome> longParent = other.getDna();

        if (other.getDna().size() < this.getDna().size()) {
            shortParent = other.getDna();
            longParent = this.getDna();
        }

        boolean startShort = getRandomBool(0.5);

        int newLength = shortParent.size();

        if (shortParent.size() != longParent.size()) {
            newLength = getRandom().nextInt(longParent.size() - shortParent.size()) + shortParent.size();
        }

        if (Configuration.CROSSOVER_TYPE == CrossoverType.RANDOM) {
            for (int i = 0; i < newLength; i++) {
                if (i < shortParent.size() && getRandomBool(0.5)) {
                    mixedDna.add(shortParent.get(i));
                } else {
                    mixedDna.add(longParent.get(i));
                }
            }
        } else if (Configuration.CROSSOVER_TYPE == CrossoverType.ONE_POINT) {
            int cutPoint = getRandom().nextInt(newLength);

            for (int i = 0; i < newLength; i++) {
                if (i < shortParent.size() && i < cutPoint) {
                    mixedDna.add(shortParent.get(i));
                } else {
                    mixedDna.add(longParent.get(i));
                }
            }

        } else if (Configuration.CROSSOVER_TYPE == CrossoverType.TWO_POINT) {
            int cutPoint1 = getRandom().nextInt(newLength);
            int cutPoint2 = getRandom().nextInt(newLength);

            for (int i = 0; i < newLength; i++) {
                if (i < shortParent.size() && (i < cutPoint1 || i >= cutPoint2)) {
                    mixedDna.add(shortParent.get(i));
                } else {
                    mixedDna.add(longParent.get(i));
                }
            }
        } else {
            throw new IllegalArgumentException("Unsupported crossover type.");
        }

        return new Individual(mixedDna);
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

    public ResponseObject getResponseObject() {
        if (!hasResponseObject()) {
            throw new IllegalStateException("Response object must be set first!");
        }
        return responseObject;
    }

    public boolean hasResponseObject() {
        return responseObject != null;
    }

    public void setResponseObject(ResponseObject responseObject) {
        this.responseObject = responseObject;
    }

    @Override
    public String toString() {
        StringBuilder data = new StringBuilder();
        for (Chromosome c : dna) {
            data.append(" -> ").append(c.getHTTPMethod()).append("::").append(c.getApiMethod());
        }

        return data.toString();
    }
}
