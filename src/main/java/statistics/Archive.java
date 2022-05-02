package statistics;

import search.Individual;
import search.genes.MethodGene;

import java.util.HashMap;
import java.util.Stack;

public class Archive extends HashMap<String, Individual> {

    /**
     * Add individual to the archive if it is new OR replace if it contains less requests than an existing individual.
     * @param key
     * @param individual
     * @return boolean value of whether the individual was added to the archive or not
     */
    public boolean putWithSecondaryObjectives(String key, Individual individual) {
        boolean wasPutInArchive = false;
        if (this.containsKey(key)) {
            Stack<MethodGene> requests = individual.getRequest();
            if (requests.size() < this.get(key).getRequest().size()) {
                this.put(key, individual);
            }
        } else {
            this.put(key, individual);
            wasPutInArchive = true;
        }
        return wasPutInArchive;
    }

}
