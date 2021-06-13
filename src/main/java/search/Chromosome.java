package search;

import org.json.JSONObject;
import search.genes.ArrayGene;

import java.util.Objects;

import static util.RandomSingleton.getRandom;

public class Chromosome {
    private String httpMethod;
    private String method;
    private ArrayGene genes;

    public Chromosome(String httpMethod, String method, ArrayGene genes) {
        this.httpMethod = httpMethod;
        this.method = method;
        this.genes = genes;
    }

    public JSONObject toRequest() {
        JSONObject request = new JSONObject();
        request.put("method", method);
        request.put("params", genes.toJSON());
        return request;
    }

    /**
     * Mutate individual.
     * @param generator
     * @return mutated individual
     */
    public Chromosome mutate(Generator generator) {
        if (getRandom().nextDouble() < 0.01) {
            // mutate http method
            return new Chromosome(generator.generateHTTPMethod(), method, genes.copy());
        } else if (getRandom().nextDouble() < 0.1) {
            // mutate method (this is actually not mutation but just a new individual)
            String methodName = generator.getRandomMethod();
            ArrayGene method = generator.generateMethod(methodName);
            return new Chromosome(generator.generateHTTPMethod(), methodName, method);
        } else {
            // mutate parameters (mutate certain rate of parameters)
            ArrayGene newGenes = genes.mutate(generator);
            return new Chromosome(httpMethod, method, newGenes);
        }
    }


    public String getHTTPMethod() {
        return httpMethod;
    }

    public String getMethod() {
        return method;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Chromosome that = (Chromosome) o;
        return
//            Objects.equals(httpMethod, that.httpMethod) &&
            Objects.equals(method, that.method) &&
            Objects.equals(genes, that.genes);
    }
}
