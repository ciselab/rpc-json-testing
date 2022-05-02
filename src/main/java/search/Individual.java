package search;

import connection.ResponseObject;
import org.json.JSONObject;
import search.genes.Gene;
import search.genes.MethodGene;

import java.util.*;

/**
 * An individual represents a test case (a sequence of one or more HTTP requests).
 */
public class Individual {

    private MethodGene root;
    private ResponseObject responseObject = null;
    private double fitness;

    public Individual(MethodGene root) {
        this.root = root;
    }

    public JSONObject toTotalJSONObject() {
        return this.root.toJSON(new HashMap<>()); // TODO
    }

    /**
     * Mutate individual.
     * @param generator
     * @return mutated individual
     */
    public Individual mutate(Generator generator) {
        // change method
        return new Individual(this.root.mutate(generator));
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public MethodGene getRoot() {
        return root;
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

        return that.getRoot().equals(this.getRoot());
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
        Stack<MethodGene> requests = this.getRequest();
        StringBuilder data = new StringBuilder();
        while (!requests.isEmpty()) {
            MethodGene c = requests.pop();
            data.append(" -> ").append(c.getHttpMethod()).append("::").append(c.getApiMethod()).append("[ ").append(c.toJSON(new HashMap<>()).toString()).append(" ]");
        }

        return data.toString();
    }

    public Stack<MethodGene> getRequest() {
        Stack<MethodGene> requests = new Stack<>();
        Queue<Gene> queue = new LinkedList<>();
        queue.add(this.getRoot());

        while (!queue.isEmpty()) {
            Gene gene = queue.poll();

            if (gene instanceof MethodGene) {
                requests.add((MethodGene) gene);
            }

            if (gene.hasChildren()) {
                List<Gene> children = gene.getChildren();
                for (Gene child : children) {
                    queue.add(child);
                }
            }
        }

        return requests;
    }
}
