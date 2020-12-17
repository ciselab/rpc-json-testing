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
    
    public Individual mutate(Generator generator) {
        if (getRandom().nextDouble() < 0.01) {
//            System.out.println("HTTP method mutated");
            // mutate http method
            return new Individual(generator.generateHTTPMethod(), method, genes.copy());
        } else if (getRandom().nextDouble() < 0.1) {
//            System.out.println("API method mutated");
            // mutate method
            String methodName = generator.getRandomMethod();
            ArrayGene method = generator.generateMethod(methodName);
            return new Individual(generator.generateHTTPMethod(), methodName, method);
        } else {
            // mutate parameters
            //TODO fix bug
//            System.out.println("Parameters are mutated, before and after:");
//            System.out.println(genes.toJSON().toString());
            ArrayGene newGenes = genes.mutate(generator);
//            System.out.println(newGenes.toJSON().toString());
//            System.out.println();
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
