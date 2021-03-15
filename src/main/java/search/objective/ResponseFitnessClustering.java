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
import search.genes.ArrayGene;
import search.genes.JSONObjectGene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

public class ResponseFitnessClustering extends Fitness {

    private static String separator = "/";

    // MAP<METHOD, MAP<STRUCTURE, LIST<PREVIOUS VALUES>>>
    private Map<String, Map<String, AgglomerativeClustering>> clusteringPerResponseStructure;
    private Map<String, Set<Integer>> statusses;

    public ResponseFitnessClustering(Client client) {
        super(client);
        this.clusteringPerResponseStructure = new HashMap<>();
        this.statusses = new HashMap<>();
    }

    public void printResults() {
        System.out.println("Methods covered: " + clusteringPerResponseStructure.keySet().size());
        for (String method: clusteringPerResponseStructure.keySet()) {
            System.out.println("\t" + method + ": ");
            System.out.println("\t\tStatusses covered: " + statusses.get(method).size());
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
            JSONObject response = responses.get(i).getResponseObject();

            System.out.println(response.toString());
            JSONObject stripped = stripValues(response);
            String strippedString = stripped.toString();

            Pair<List<Object>, List<Integer>> featureAndWeightVector = getVector(response);

            // If empty object just give it a fitness of zero
            if (featureAndWeightVector.getKey().size() == 0) {
                population.get(i).setFitness(0);
                continue;
            }
//            System.out.println(featureAndWeightVector);

            if (!clusteringPerResponseStructure.containsKey(method)) {
                clusteringPerResponseStructure.put(method, new HashMap<>());
                statusses.put(method, new HashSet<>());
            }

            statusses.get(method).add(responses.get(i).getResponseCode());


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
    public Pair<List<Object>, List<Integer>> getVector(JSONObject response) {
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

                if (object.isNull(key)) {
                    // TODO maybe add this
//                    featureVector.add(null);
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
                    } else {
                        featureVector.add(arrayObject);
                        weightVector.add(depth+1);
                    }
                } else {
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
    public JSONObject stripValues(JSONObject response) {
        JSONObject structure = new JSONObject(response.toString());

        Queue<JSONObject> queue = new LinkedList<>();
        queue.add(structure);

        // TODO something clever with arrays

        while(!queue.isEmpty()) {
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
                            array.put(i, STANDARD_STRING);
                        } else if (arrayObject instanceof Number) {
                            array.put(i, STANDARD_NUMBER);
                        } else if (arrayObject instanceof Boolean) {
                            array.put(i, STANDARD_BOOLEAN);
                        }
                        // TODO currently it is assuming no arrays in arrays
                    }
                } else if (smallerObject instanceof String) {
                    object.put(key, STANDARD_STRING);
                } else if (smallerObject instanceof Number) {
                    object.put(key, STANDARD_NUMBER);
                } else if (smallerObject instanceof Boolean) {
                    object.put(key, STANDARD_BOOLEAN);
                } else {
//                    System.out.println(smallerObject.toString());
//                    System.out.println(smallerObject.getClass());
                }
            }
        }
        return structure;
    }

}
