package search.objective;

import connection.Client;
import connection.ResponseObject;
import util.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;
import search.Generator;
import search.Individual;
import search.clustering.AgglomerativeClustering;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class ResponseFitnessClustering extends Fitness {

    private static String separator = "/";

    // MAP<METHOD, MAP<STRUCTURE, LIST<PREVIOUS VALUES>>>
    private Map<String, Map<String, AgglomerativeClustering>> clusteringPerResponseStructure;
    private Map<String, Set<Integer>> statuses;

    public ResponseFitnessClustering(Client client) {
        super(client);
        this.clusteringPerResponseStructure = new HashMap<>();
        this.statuses = new HashMap<>();
    }

    public void printResults() {
        System.out.println("Methods covered: " + clusteringPerResponseStructure.keySet().size());
        for (String method: clusteringPerResponseStructure.keySet()) {
            System.out.println("\t" + method + ": ");
            System.out.println("\t\tStatusses covered: " + statuses.get(method).size());
            System.out.println("\t\tStructures covered: " + clusteringPerResponseStructure.get(method).keySet().size());

            for (String structure : clusteringPerResponseStructure.get(method).keySet()) {
                System.out.println("\t\t\tClusters: " + clusteringPerResponseStructure.get(method).get(structure).getClusters().size());
            }
        }
    }

    @Override
    public void evaluate(Generator generator, Individual individual) throws IOException {

    }

    @Override
    public void evaluate(Generator generator, List<Individual> population) {

        List<ResponseObject> responses = getResponses(population);

        for (int i = 0; i < population.size(); i++) {
            String method = population.get(i).getMethod();
            JSONObject request = population.get(i).toRequest();
            JSONObject response = responses.get(i).getResponseObject();

            JSONObject stripped = stripValues(request, response);
            String strippedString = stripped.toString();

            Pair<List<Object>, List<Integer>> featureAndWeightVector = getVector(response, stripped);

            // If empty object just give it a fitness of zero
            if (featureAndWeightVector.getKey().size() == 0) {
                population.get(i).setFitness(0);
                continue;
            }

            if (!clusteringPerResponseStructure.containsKey(method)) {
                clusteringPerResponseStructure.put(method, new HashMap<>());
                statuses.put(method, new HashSet<>());
            }

            statuses.get(method).add(responses.get(i).getResponseCode());


            if (!clusteringPerResponseStructure.get(method).containsKey(strippedString)) {
                clusteringPerResponseStructure.get(method).put(strippedString, new AgglomerativeClustering(featureAndWeightVector.getValue()));
            }

            AgglomerativeClustering clustering = clusteringPerResponseStructure.get(method).get(strippedString);

            double cost = clustering.cluster(featureAndWeightVector.getKey());

            double fitness = 1.0 / (1 + cost);
            population.get(i).setFitness(fitness);

        }
    }

    /**
     * Calculate the feature vector and the weight vector.
     *
     * @param response the response JSONObject
     * @return featureVector and weightVector
     */
    public Pair<List<Object>, List<Integer>> getVector(JSONObject response, JSONObject stripped) {
        JSONObject structure = new JSONObject(response.toString());

        List<Object> featureVector = new ArrayList<>();
        List<Integer> weightVector = new ArrayList<>();

        Queue<Pair<JSONObject, Integer>> queue = new LinkedList<>();
        queue.add(new Pair<>(structure, 0));

        while (!queue.isEmpty()) {
            Pair<JSONObject, Integer> pair = queue.poll();
            JSONObject object = pair.getKey();
            Integer depth = pair.getValue();

            Iterator<String> it = object.keys();
            while (it.hasNext()) {
                String key = it.next();

                // Skip this key if the value is null or if it does not exist in the stripped JSONObject
                if (object.isNull(key)) {
                    // TODO should we do this? It  can occur that an error_message is null for example.
                    if (stripped.has(key)) {
                        featureVector.add("null");
                        weightVector.add(depth+1);
                    }
                    continue;
                }

                Object smallerObject = object.get(key);
                if (smallerObject instanceof JSONObject) {
                    queue.add(new Pair<>((JSONObject) object.get(key), depth+1));
                } else if (smallerObject instanceof JSONArray) {
                    JSONArray array = ((JSONArray) smallerObject);

                    if (array.length() == 0) {
                        // TODO maybe add something here (empty array) (maybe add the length of the array as a value)
                        continue;
                    }

                    if (array.isNull(0)) {
                        // TODO maybe add this
//                    featureVector.add(null);
                    }

                    Object arrayObject = array.get(0);

                    // just take first object of array
                    if (arrayObject instanceof JSONObject) {
                        queue.add(new Pair<>((JSONObject) arrayObject, depth+1));
                    } else if (stripped.has(key)) {
                        featureVector.add(arrayObject);
                        weightVector.add(depth+1);
                    }
                } else if (stripped.has(key)) {
                    featureVector.add(smallerObject);
                    weightVector.add(depth+1);
                }
            }
        }
        return new Pair<>(featureVector, weightVector);
    }

    private static String STANDARD_STRING = "";
    private static Boolean STANDARD_BOOLEAN = true;
    private static Integer STANDARD_NUMBER = 0;

    /**
     * Copy the response JSONObject and remove the values.
     * @param response
     * @return JSONObject with standard values, but key structure intact.
     */
    public JSONObject stripValues(JSONObject request, JSONObject response) {
        JSONObject structure = new JSONObject(response.toString());
        JSONObject copy = new JSONObject();

        Queue<Pair<JSONObject, JSONObject>> queue = new LinkedList<>();
        queue.add(new Pair<>(structure, copy));

        HashMap<String, Object> requestKeyValuePairs = getKeyValuePairs(request);

        // TODO something clever with arrays

        while(!queue.isEmpty()) {
            Pair<JSONObject, JSONObject> pair = queue.poll();
            JSONObject object = pair.getKey();
            JSONObject strippedObject = pair.getValue();

            Iterator<String> it = object.keys();
            while (it.hasNext()) {
                String key = it.next();

                Object smallerObject = object.get(key);
                if (smallerObject instanceof JSONObject) {
                    queue.add(new Pair<>((JSONObject) object.get(key), strippedObject));
                } else if (smallerObject instanceof JSONArray) {
                    JSONArray array = ((JSONArray) smallerObject);
                    for (int i = 0; i < array.length(); i++) {
                        if (i > 0) {
                            array.remove(1);
                            continue;
                        }

                        Object arrayObject = array.get(i);
                        if (arrayObject instanceof JSONObject) {
                            queue.add(new Pair<>((JSONObject) arrayObject, strippedObject));
                        } else if (arrayObject instanceof JSONString) {
                            array.put(i, STANDARD_STRING);
                        } else if (arrayObject instanceof Number) {
                            array.put(i, STANDARD_NUMBER);
                        } else if (arrayObject instanceof Boolean) {
                            array.put(i, STANDARD_BOOLEAN);
                        }
                        // TODO currently it is assuming no arrays in arrays
                    }
                } else if (!(requestKeyValuePairs.containsKey(key) && requestKeyValuePairs.get(key).equals(smallerObject))) {
                    if (smallerObject instanceof String) {
                        object.put(key, STANDARD_STRING);
                        strippedObject.put(key, STANDARD_STRING);
                    } else if (smallerObject instanceof Number) {
                        object.put(key, STANDARD_NUMBER);
                        strippedObject.put(key, STANDARD_NUMBER);
                    } else if (smallerObject instanceof Boolean) {
                        object.put(key, STANDARD_BOOLEAN);
                        strippedObject.put(key, STANDARD_BOOLEAN);
                    } else {
                        // A unknown object
                    }
                }
            }
        }
        System.out.println("Request: " + request.toString());
        System.out.println("Response: " + response.toString());
        System.out.println("Response stripped: " + copy.toString());
        return copy;
    }


    // Check the key and value pairs in the request
    // Match them with the key and value pairs in the response
    // If they are a match, do not include this pair in the feature vector
    public HashMap<String, Object> getKeyValuePairs(JSONObject request) {
        JSONObject structure = new JSONObject(request.toString());

        Queue<JSONObject> queue = new LinkedList<>();
        queue.add(structure);

        HashMap<String, Object> keyValuePairs = new HashMap<>();

        while (!queue.isEmpty()) {
            JSONObject object = queue.poll();
            Iterator<String> it = object.keys();
            while (it.hasNext()) {
                String key = it.next();

                Object smallerObject = object.get(key);
                if (smallerObject instanceof JSONObject) {
                    queue.add((JSONObject) object.get(key));
                } else if (smallerObject instanceof JSONArray) {
                    JSONArray array = ((JSONArray) smallerObject);
                    for (int i = 0; i < array.length(); i++) {
                        if (i > 0) {
                            array.remove(1);
                            continue;
                        }

                        Object arrayObject = array.get(i);
                        if (arrayObject instanceof JSONObject) {
                            queue.add((JSONObject) arrayObject);
                        } else if (arrayObject instanceof JSONString) {
                            array.put(i, arrayObject);
                        } else if (arrayObject instanceof Number) {
                            array.put(i, arrayObject);
                        } else if (arrayObject instanceof Boolean) {
                            array.put(i, arrayObject);
                        }
                        // TODO currently it is assuming no arrays in arrays
                    }
                } else if (smallerObject instanceof String) {
                    keyValuePairs.put(key, smallerObject);
                } else if (smallerObject instanceof Number) {
                    keyValuePairs.put(key, smallerObject);
                } else if (smallerObject instanceof Boolean) {
                    keyValuePairs.put(key, smallerObject);
                } else {
//                    System.out.println(smallerObject.toString());
//                    System.out.println(smallerObject.getClass());
                }
            }
        }
        return keyValuePairs;
    }

}
