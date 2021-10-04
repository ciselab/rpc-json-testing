package statistics;

import search.Individual;

import java.util.HashMap;

public class Archive extends HashMap<String, Individual> {

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
//        System.out.println("Archive size: " + this.size());
        return wasPutInArchive;
    }

    public void printArchive() {
        for (String key : this.keySet()) {
            System.out.println(key);
        }
    }
}
