package search;

import org.json.JSONObject;
import search.genes.ArrayGene;
import search.openRPC.Specification;

import java.util.Objects;

import static util.RandomSingleton.getRandom;

public class Individual {
    private String httpMethod;
    private String method;
    private ArrayGene genes;
    private Integer age;
    private double fitness;

    public Individual(String httpMethod, String method, ArrayGene genes) {
        this.httpMethod = httpMethod;
        this.method = method;
        this.genes = genes;
        this.age = 0;
    }

    public JSONObject toRequest() {
        JSONObject request = new JSONObject();
        request.put("method", method);
        request.put("params", genes.toJSON());
        return request;
    }
    
    public Individual mutate(Generator generator) {
        if (getRandom().nextDouble() < 0.01) {
            // mutate http method
            return new Individual(generator.generateHTTPMethod(), method, genes.copy());
        } else if (getRandom().nextDouble() < 0.1) {
            // mutate method
            String methodName = generator.getRandomMethod();
            ArrayGene method = generator.generateMethod(methodName);
            return new Individual(generator.generateHTTPMethod(), methodName, method);
        } else {
            // mutate parameters
            ArrayGene newGenes = genes.mutate(generator);
            return new Individual(httpMethod, method, newGenes);
        }
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

    public String getHTTPMethod() {
        return httpMethod;
    }

    public String getMethod() {
        return method;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
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
        return
//            Objects.equals(httpMethod, that.httpMethod) &&
            Objects.equals(method, that.method) &&
            Objects.equals(genes, that.genes);
    }

}
