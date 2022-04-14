package statistics;

import search.Individual;

import java.util.HashMap;

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
            if (individual.getDna().size() < this.get(key).getDna().size()) {
                this.put(key, individual);
            }
        } else {
            this.put(key, individual);
            wasPutInArchive = true;
        }
        return wasPutInArchive;
    }

}
