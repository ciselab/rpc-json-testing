package search.objective;

import connection.ResponseObject;
import test_drivers.TestDriver;
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

public class ResponseFitnessPredefinedTypes extends Fitness {

    private static String separator = "/";

    // MAP<METHOD, MAP<PATH-TO-PARAM, MAP<CATEGORY, COUNT>>>
    private Map<String, Map<String, Map<Type, Integer>>> valuePerKeyCount;

    public ResponseFitnessPredefinedTypes(TestDriver testDriver) {
        super(testDriver);
        this.valuePerKeyCount = new HashMap<>();
    }

    @Override
    public void evaluate(Generator generator, List<Individual> population) {

        List<ResponseObject> responses = getResponses(population);

        if (getTestDriver().shouldContinue()) {

            for (int i = 0; i < population.size(); i++) {
                Double fitness = recordValueTypesAndGetFitness(population.get(i).getDna().get(population.get(i).getDna().size() - 1).getMethod(), responses.get(i).getResponseObject()); // population and responses are in the same order

                fitness = 1.0 / (1 + fitness);

                population.get(i).setFitness(fitness);

                // decide whether to add individual to the archive
                if (responses.get(i).getResponseCode() > 499 && !getArchive().contains(population.get(i))) {
                    this.addToArchive(population.get(i), responses.get(i));
                } else if (fitness >= Configuration.ARCHIVE_THRESHOLD && !getArchive().contains(population.get(i))) {
                    this.addToArchive(population.get(i), responses.get(i));
                }
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
     * @return JSONObject with standard values, but key structure intact.
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
