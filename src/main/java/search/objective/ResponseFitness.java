package search.objective;

import connection.Client;
import connection.ResponseObject;
import javafx.util.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;
import search.Generator;
import search.Individual;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class ResponseFitness extends Fitness {

    private static String separator = "/";

    // MAP<METHOD, MAP<TYPE, MAP<CATEGORY, COUNT>>>
    private Map<String, Map<String, Map<Type, Integer>>> valuePerKeyCount;

    public ResponseFitness(Client client) {
        super(client);
        this.valuePerKeyCount = new HashMap<>();
    }

    @Override
    public void evaluate(Generator generator, Individual individual) throws IOException {

    }

    @Override
    public void evaluate(Generator generator, List<Individual> population) {

        List<ResponseObject> responses = getResponses(population);

        for (int i = 0; i < population.size(); i++) {
            Double fitness = recordValueTypesAndGetFitness(population.get(i).getMethod(), responses.get(i).getResponseObject());

            fitness = 1.0 / (1 + fitness);

//            System.out.println(responses.get(i).getResponseObject().toString(2));
            System.out.println(fitness);


            population.get(i).setFitness(fitness);
        }
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
                    continue;
                }

                Object smallerObject = object.get(key);
                if (smallerObject instanceof JSONObject) {
                    queue.add(new Pair<>(path + separator + key, (JSONObject) object.get(key)));
                } else if (smallerObject instanceof JSONArray) {
                    JSONArray array = ((JSONArray) smallerObject);

                    if (array.length() == 0) {
                        score += recordType(method, path + separator + key, Type.EMPTY_ARRAY);
                        continue;
                    }

                    if (array.isNull(0)) {
                        score += recordType(method, path + separator + key, Type.NULL_ARRAY);
                    }

                    Object arrayObject = array.get(0);

                    // just take first object of array
                    if (arrayObject instanceof JSONObject) {
                        queue.add(new Pair<>(path + separator + key, (JSONObject) arrayObject));
                    } else {
                        score += recordType(method, path + separator + key, Type.matchTypeArray(arrayObject));
                    }
                } else {
                    score += recordType(method, path + separator + key, Type.matchType(smallerObject));
                }
            }
        }
        return score;
    }

    private Integer recordType(String method, String path, Type type) {
//        System.out.println("Methods: " + valuePerKeyCount.keySet().size());
        if (!valuePerKeyCount.containsKey(method)) {
            valuePerKeyCount.put(method, new HashMap<>());
        }

//        System.out.println(method + ": " + valuePerKeyCount.get(method).keySet().size());
        if (!valuePerKeyCount.get(method).containsKey(path)) {
            valuePerKeyCount.get(method).put(path, new HashMap<>());
        }

//        System.out.println(path + ": " + valuePerKeyCount.get(method).get(path).keySet().size());
        if (!valuePerKeyCount.get(method).get(path).containsKey(type)) {
            valuePerKeyCount.get(method).get(path).put(type, 0);
        }

//        System.out.println(type + ": " + valuePerKeyCount.get(method).get(path).get(type));
        Integer currentCount = valuePerKeyCount.get(method).get(path).get(type);
        valuePerKeyCount.get(method).get(path).put(type, valuePerKeyCount.get(method).get(path).get(type) + 1);

        return currentCount;
    }

}
