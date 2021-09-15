package search;

import org.json.JSONObject;
import search.genes.ArrayGene;
import util.Configuration;

import java.util.Objects;

import static util.RandomSingleton.getChoiceWithChance;
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
     * Mutate a chromosome.
     * @param generator
     * @return mutated individual
     */
    public Chromosome mutate(Generator generator) {
        double choice = getRandom().nextDouble();
        if (choice < Configuration.MUTATE_HTTP_METHOD_PROB) {
            // mutate http method
            return new Chromosome(generator.generateHTTPMethod(), method, genes.copy());
        } else if (choice < (Configuration.MUTATE_HTTP_METHOD_PROB + Configuration.MUTATE_API_METHOD_PROB)) {
            // mutate api method (and corresponding newly generated parameters)
            String methodName = generator.getRandomMethod();
            ArrayGene method = generator.generateMethod(methodName);
            return new Chromosome(generator.generateHTTPMethod(), methodName, method);
        } else {
            // mutate parameters of api method
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
