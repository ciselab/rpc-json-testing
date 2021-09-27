package objective;

import util.Configuration;
import util.Pair;

import search.Generator;
import search.Individual;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static statistics.Collector.getCollector;

public class ResponseFitnessPredefinedTypes extends Fitness {

    private static String separator = "/";

    // MAP<METHOD, MAP<PATH-TO-PARAM, MAP<CATEGORY, COUNT>>>
    private Map<String, Map<String, Map<Type, Integer>>> valuePerKeyCount;

    public ResponseFitnessPredefinedTypes() {
        super();
        this.valuePerKeyCount = new HashMap<>();
    }

    @Override
    public void evaluate(Generator generator, List<Individual> population) {
        for (Individual individual : population) {
            Double cost = recordValueTypesAndGetFitness(individual.getDna().get(individual.getDna().size() - 1).getApiMethod(),
                individual.getResponseObject().getResponseObject()); // population and responses are in the same order

            // Fitness is between 0 and 1.
            Double fitness = 1.0 / cost;

            individual.setFitness(fitness);

            // decide whether to add individual to the archive
            if (individual.getResponseObject().getResponseCode() > 499) {
                getCollector().addToArchive(individual.getResponseObject().getResponseObject().toString(), individual);
            } else if (fitness >= Configuration.ARCHIVE_THRESHOLD) {
                getCollector().addToArchive(individual.getResponseObject().getResponseObject().toString(), individual);
            }
        }

    }

    @Override
    public ArrayList<String> storeInformation() {
        ArrayList<String> info = new ArrayList<>();

        info.add("Map: " + valuePerKeyCount);
        for (String key : valuePerKeyCount.keySet()) {
            info.add("Method: " + key);
            for (String key2 : valuePerKeyCount.get(key).keySet()) {
                info.add("\t\tParameter: " + key2);
                info.add("\t\t\tCategory count: " + valuePerKeyCount.get(key).get(key2).toString());
            }
        }
        return info;
    }

    /**
     * Copy the response JSONObject and remove the values.
     *
     * @param response
     * @return ratio between how often a type has occurred and how many parameters the response has.
     */
    public Double recordValueTypesAndGetFitness(String method, JSONObject response) {
        JSONObject structure = new JSONObject(response.toString());

        Double score = 0d;
        int numberOfKeys = 0;

        Queue<Pair<String, JSONObject>> queue = new LinkedList<>();
        queue.add(new Pair<>("", structure));

        while (!queue.isEmpty()) {
            Pair<String, JSONObject> pair = queue.poll();
            String path = pair.getKey();
            JSONObject object = pair.getValue();

            Iterator<String> it = object.keys();
            while (it.hasNext()) {
                String key = it.next();

                if (object.isNull(key)) {
                    score += recordType(method, path + separator + key, Type.NULL);
                    numberOfKeys += 1;
                    continue;
                }

                Object smallerObject = object.get(key);
                if (smallerObject instanceof JSONObject) {
                    queue.add(new Pair<>(path + separator + key, (JSONObject) object.get(key)));
                } else if (smallerObject instanceof JSONArray) {
                    JSONArray array = ((JSONArray) smallerObject);

                    if (array.length() == 0) {
                        score += recordType(method, path + separator + key, Type.EMPTY_ARRAY);
                        numberOfKeys += 1;
                        continue;
                    }

                    if (array.isNull(0)) {
                        score += recordType(method, path + separator + key, Type.NULL_ARRAY);
                        numberOfKeys += 1;
                    }

                    Object arrayObject = array.get(0);

                    // just take first object of array
                    if (arrayObject instanceof JSONObject) {
                        queue.add(new Pair<>(path + separator + key, (JSONObject) arrayObject));
                    } else {
                        score += recordType(method, path + separator + key, Type.matchTypeArray(arrayObject));
                        numberOfKeys += 1;
                    }
                } else {
                    score += recordType(method, path + separator + key, Type.matchType(smallerObject));
                    numberOfKeys += 1;
                }
            }
        }

        if (numberOfKeys == 0) {
            return score;
        }
        return score / numberOfKeys;
    }

    /**
     * Store the response (method and all its parameters and corresponding types) in a map to keep track of occurrences.
     * @param method
     * @param path
     * @param type
     * @return the number of times a type has occurred (within a certain parameter within a certain method)
     */
    private Integer recordType(String method, String path, Type type) {
        if (!valuePerKeyCount.containsKey(method)) {
            valuePerKeyCount.put(method, new HashMap<>());
        }
        if (!valuePerKeyCount.get(method).containsKey(path)) {
            valuePerKeyCount.get(method).put(path, new HashMap<>());
        }
        if (!valuePerKeyCount.get(method).get(path).containsKey(type)) {
            valuePerKeyCount.get(method).get(path).put(type, 0);
        }

        Integer currentCount = valuePerKeyCount.get(method).get(path).get(type);
        valuePerKeyCount.get(method).get(path).put(type, valuePerKeyCount.get(method).get(path).get(type) + 1);

        return currentCount;
    }

}
