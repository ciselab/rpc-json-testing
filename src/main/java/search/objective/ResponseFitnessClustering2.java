package search.objective;

import connection.Client;
import connection.ResponseObject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;
import search.Generator;
import search.Individual;
import search.clustering.AgglomerativeClustering2;
import util.Pair;

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

public class ResponseFitnessClustering2 extends Fitness {

    private static String separator = "/";

    // MAP<METHOD, MAP<STRUCTURE, LIST<PREVIOUS VALUES>>>
    private Map<String, Map<String, AgglomerativeClustering2>> clusteringPerResponseStructure;
    private Map<String, Set<Integer>> statuses;

    // Count the number of generations
    private int generationCount;
    final private int NEW_CLUSTERS_AFTER_GEN = 10;

    public ResponseFitnessClustering2(Client client) {
        super(client);
        this.clusteringPerResponseStructure = new HashMap<>();
        this.statuses = new HashMap<>();
        this.generationCount = 0;
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

        Map<String, Map<String, List<List<Object>>>> featureVectorPerStructurePerMethodOfCurrentPop = new HashMap<>();

        for (int i = 0; i < population.size(); i++) {
            String method = population.get(i).getMethod();
            JSONObject response = responses.get(i).getResponseObject();

            System.out.println(response.toString());

            JSONObject stripped = stripValues(response);
            String strippedString = stripped.toString();

            Pair<List<Object>, List<Integer>> featureAndWeightVector = getVector(response);
            if (!featureVectorPerStructurePerMethodOfCurrentPop.containsKey(method)) {
                featureVectorPerStructurePerMethodOfCurrentPop.put(method, new HashMap<>());
            }
            if (!featureVectorPerStructurePerMethodOfCurrentPop.get(method).containsKey(strippedString)) {
                featureVectorPerStructurePerMethodOfCurrentPop.get(method).put(strippedString, new ArrayList<>());
            }
            featureVectorPerStructurePerMethodOfCurrentPop.get(method).get(strippedString).add(featureAndWeightVector.getKey());

            if (!clusteringPerResponseStructure.containsKey(method)) {
                clusteringPerResponseStructure.put(method, new HashMap<>());
                statuses.put(method, new HashSet<>());
            }

            statuses.get(method).add(responses.get(i).getResponseCode());

            if (!clusteringPerResponseStructure.get(method).containsKey(strippedString)) {
                clusteringPerResponseStructure.get(method).put(strippedString, new AgglomerativeClustering2(featureAndWeightVector.getValue()));
            }

            // If empty object just give it a fitness of zero
            if (featureAndWeightVector.getKey().size() == 0) {
                population.get(i).setFitness(0);
                continue;
            }

            AgglomerativeClustering2 clustering = clusteringPerResponseStructure.get(method).get(strippedString);

            // first generation every ind gets same fitness OR different metric
            double cost = clustering.calculateMaxSimilarity(featureAndWeightVector.getKey());
//            System.out.println("Cost: " + cost);

            double fitness = 1.0 / (1 + cost);
            population.get(i).setFitness(fitness);
//            System.out.println("fitness " + fitness);

            // TODO hack for worst output
            if (population.get(i).getMethod().equals("random") ||
                population.get(i).getMethod().equals("server_info") ||
                population.get(i).getMethod().equals("server_state")) {
                population.get(i).setFitness(0);
            }
        }
        if (generationCount % NEW_CLUSTERS_AFTER_GEN == 0) {
            for (String method : featureVectorPerStructurePerMethodOfCurrentPop.keySet()) {
                for (String responseStructure : featureVectorPerStructurePerMethodOfCurrentPop.get(method).keySet()) {
                    clusteringPerResponseStructure.get(method).get(responseStructure).cluster(featureVectorPerStructurePerMethodOfCurrentPop.get(method).get(responseStructure));
                }
            }
        }
        generationCount += 1;
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
