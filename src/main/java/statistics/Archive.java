package statistics;

import search.Individual;

import java.util.HashMap;

public class Archive extends HashMap<String, Individual> {

    public void putWithSecondaryObjectives(String key, Individual individual) {
        if (this.containsKey(key)) {
            if (individual.getDna().size() < this.get(key).getDna().size()) {
                this.put(key, individual);
            }
        } else {
            this.put(key, individual);
        }
    }
}
