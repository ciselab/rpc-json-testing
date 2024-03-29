package objective;

import org.json.JSONArray;
import org.json.JSONObject;

import search.Generator;
import search.Individual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static statistics.Collector.getCollector;
import static util.ObjectStripper.stripValues;

/**
 * Use complexity of the request and response object (layers of JSON object) in the fitness calculation.
 */
public class ResponseSkeletonAndRequestComplexityFitness extends Fitness {
    private static String STANDARD_STRING = "";
    private static Boolean STANDARD_BOOLEAN = true;
    private static Integer STANDARD_NUMBER = 0;

    private Map<String, Integer> structureFrequencyTable;

    public ResponseSkeletonAndRequestComplexityFitness() {
        super();
        this.structureFrequencyTable = new HashMap<>();
    }

    @Override
    public void evaluate(Generator generator, List<Individual> population) {
        for (Individual individual : population) {

            String structureString = stripValues(individual.toTotalJSONObject(), individual.getResponseObject().getResponseObject()).toString();

            if (!structureFrequencyTable.containsKey(structureString)) {
                structureFrequencyTable.put(structureString, 0);
            }
            structureFrequencyTable.put(structureString, structureFrequencyTable.get(structureString) + 1);
        }

        for (Individual individual : population) {
            String structureString = stripValues(individual.toTotalJSONObject(), individual.getResponseObject().getResponseObject()).toString();

            int inputComplexity = calculateComplexity(individual.toTotalJSONObject());
            int outputComplexity = calculateComplexity(individual.getResponseObject().getResponseObject());

            double exploitationFitness = 1.0 / (1.0 + (double) structureFrequencyTable.get(structureString));
            // every key should be mutated at least once
            double explorationFitness = (inputComplexity);

            double fitness = exploitationFitness * explorationFitness;
            individual.setFitness(fitness);

            // decide whether to add individual to the archive
            getCollector().addToArchive(individual.getResponseObject().getResponseObject().toString(), individual);

        }
    }

    @Override
    public ArrayList<String> storeInformation() {
        ArrayList<String> info = new ArrayList<>();
        info.add("Map: " + structureFrequencyTable.keySet().size());
        return info;
    }


    private int calculateComplexity(JSONObject response) {
        int complexity = 0;

        JSONObject structure = new JSONObject(response.toString());

        Queue<JSONObject> queue = new LinkedList<>();
        queue.add(structure);

        while(!queue.isEmpty()) {
            JSONObject object = queue.poll();
            Iterator<String> it = object.keys();
            while (it.hasNext()) {
                complexity++;
                String key = it.next();
                Object smallerObject = object.get(key);
                if (smallerObject instanceof JSONObject) {
                    queue.add((JSONObject) object.get(key));
                } else if (smallerObject instanceof JSONArray) {
                    JSONArray array = ((JSONArray) smallerObject);
                    for (int i = 0; i < array.length(); i++) {
                        Object arrayObject = array.get(i);
                        if (arrayObject instanceof JSONObject) {
                            queue.add((JSONObject) arrayObject);
                        }
                        // TODO currently it is assuming no arrays in arrays
                    }
                }
            }
        }
        return complexity;
    }
}
