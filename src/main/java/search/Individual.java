package search;

import org.json.JSONObject;
import search.genes.ArrayGene;
import search.openRPC.Specification;

import static util.RandomSingleton.getRandom;

public class Individual {
    private String httpMethod;
    private String method;
    private ArrayGene genes;
    private double fitness;

    public Individual(String httpMethod, String method, ArrayGene genes) {
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
    
    public Individual mutate(Specification specification) {
        if (getRandom().nextDouble() < 0.01) {
            // mutate http method
            return new Individual(specification.getGenerator().generateHTTPMethod(), method, genes.copy());
        } else if (getRandom().nextDouble() < 0.1) {
            // mutate method
            ArrayGene method = (ArrayGene) specification.getRandomOption();
            return new Individual(specification.getGenerator().generateHTTPMethod(), method.getKey(), method);
        } else {
            // mutate parameters
            //TODO fix bug
            System.out.println("MUTATE PARAMS");

            System.out.println(genes.toJSON().toString());
            ArrayGene newGenes = genes.mutate(specification);
            System.out.println(newGenes.toJSON().toString());
            System.out.println();
            // mutate params
            return new Individual(httpMethod, method, newGenes);
        }
    }

    public String getHTTPMethod() {
        return httpMethod;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }
}
