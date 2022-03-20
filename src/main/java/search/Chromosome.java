package search;

import org.json.JSONObject;
import search.genes.ArrayGene;
import util.config.Configuration;

import java.util.Objects;

import static util.RandomSingleton.getRandom;

public class Chromosome {
    private String httpMethod;
    private String apiMethod;
    private ArrayGene genes;

    public Chromosome(String httpMethod, String apiMethod, ArrayGene genes) {
        this.httpMethod = httpMethod;
        this.apiMethod = apiMethod;
        this.genes = genes;
    }

    public JSONObject toRequest() {
        JSONObject request = new JSONObject();
        request.put("method", apiMethod);
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
            // mutate http apiMethod
            return new Chromosome(generator.generateHTTPMethod(), apiMethod, genes.copy());
        } else if (choice < (Configuration.MUTATE_HTTP_METHOD_PROB + Configuration.MUTATE_API_METHOD_PROB)) {
            // mutate api apiMethod (and corresponding newly generated parameters)
            String methodName = generator.getRandomMethod();
            ArrayGene method = generator.generateMethod(methodName);
            return new Chromosome(generator.generateHTTPMethod(), methodName, method);
        } else {
            // mutate parameters of api apiMethod
            ArrayGene newGenes = genes.mutate(generator);
            return new Chromosome(httpMethod, apiMethod, newGenes);
        }
    }

    public String getHTTPMethod() {
        return httpMethod;
    }

    public String getApiMethod() {
        return apiMethod;
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
            Objects.equals(apiMethod, that.apiMethod) &&
            Objects.equals(genes, that.genes);
    }
}
