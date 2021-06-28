package search.objective;

import connection.ResponseObject;
import search.clustering.AgglomerativeClustering3;
import test_drivers.TestDriver;
import search.Generator;
import search.Individual;
import util.Pair;
import util.Triple;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Cluster once every X generations.
 */
public class DiversityBasedFitness extends Fitness {

    private static String separator = "/";

    // MAP<METHOD, MAP<STRUCTURE, LIST<PREVIOUS VALUES>>>
    private Map<String, Map<String, AgglomerativeClustering3>> clusteringPerResponseStructure;
    private Map<String, Set<Integer>> statuses;

    // Count the number of generations
    private int generationCount;
    final private int NEW_CLUSTERS_AFTER_GEN = 10;

    private double ARCHIVE_THRESHOLD = 0.8;

    private int serverErrors = 0;

    public DiversityBasedFitness(TestDriver testDriver) {
        super(testDriver);
        this.clusteringPerResponseStructure = new HashMap<>();
        this.statuses = new HashMap<>();
        this.generationCount = 0;
    }

    @Override
    public void evaluate(Generator generator, List<Individual> population) {

        List<ResponseObject> responses = getResponses(population);

        // Map with API methods as key and a map as value. This second map has skeletons as key and a list of feature vectors as value.
        Map<String, Map<String, List<List<Object>>>> allFeatureVectors = new HashMap<>();

        for (int i = 0; i < population.size(); i++) {
            String method = population.get(i).getDna().get(population.get(i).getDna().size() - 1).getMethod();
            JSONObject request = population.get(i).toTotalJSONObject();
            JSONObject response = responses.get(i).getResponseObject();

            JSONObject stripped = stripValues(request, response);
            String strippedString = stripped.toString();

            Pair<List<Object>, List<Integer>> featureAndWeightVector = getVector(response, stripped);

            // If the response is an empty object just give it a fitness of zero
            if (featureAndWeightVector.getKey().size() == 0) {
                population.get(i).setFitness(0);
                continue;
            }

            // Add key and value for the used method if it does not exist yet.
            if (!allFeatureVectors.containsKey(method)) {
                allFeatureVectors.put(method, new HashMap<>());
            }
            // Add key and value for the found response skeleton if it does not exist yet.
            if (!allFeatureVectors.get(method).containsKey(strippedString)) {
                allFeatureVectors.get(method).put(strippedString, new ArrayList<>());
            }
            // Add the feature vector to the right place in the map.
            allFeatureVectors.get(method).get(strippedString).add(featureAndWeightVector.getKey());

            if (!clusteringPerResponseStructure.containsKey(method)) {
                clusteringPerResponseStructure.put(method, new HashMap<>());
                statuses.put(method, new HashSet<>());
            }

            statuses.get(method).add(responses.get(i).getResponseCode());

            if (!clusteringPerResponseStructure.get(method).containsKey(strippedString)) {
                clusteringPerResponseStructure.get(method).put(strippedString, new AgglomerativeClustering3(featureAndWeightVector.getValue()));
            }

            AgglomerativeClustering3 clustering = clusteringPerResponseStructure.get(method).get(strippedString);

            // calculate the minimum distance of the individual to the clusters
            double cost = clustering.addOne(featureAndWeightVector.getKey());

            double fitness = 1.0 / (1 + cost);

            // TODO not use this hack for worst output
            if (population.get(i).getDna().get(population.get(i).getDna().size() - 1).getMethod().equals("random") ||
                population.get(i).getDna().get(population.get(i).getDna().size() - 1).getMethod().equals("server_info") ||
                population.get(i).getDna().get(population.get(i).getDna().size() - 1).getMethod().equals("server_state")) {
                fitness = 0;
            }

            population.get(i).setFitness(fitness);

            // decide whether to add individual to the archive
            if (responses.get(i).getResponseCode() > 499) {
                System.out.println("Individual with response code: " + responses.get(i).getResponseCode());
                serverErrors += 1;
                this.addToArchive(population.get(i));
            }
            if (fitness >= ARCHIVE_THRESHOLD && !getArchive().contains(population.get(i))) {
                this.addToArchive(population.get(i));
            }
        }
        if (generationCount % NEW_CLUSTERS_AFTER_GEN == 0) {
            for (String method : allFeatureVectors.keySet()) {
                //TODO make sure all strings are in there
                for (String responseStructure : allFeatureVectors.get(method).keySet()) {
                    clusteringPerResponseStructure.get(method).get(responseStructure).cluster();
                }
            }
        }
        generationCount += 1;

        System.out.println("Number of 5xx errors: " + serverErrors);
    }

    /**
     * Calculate the feature vector and the weight vector. Make use of depth of keys.
     *
     * @param stripped the stripped response JSONObject
     * @param response the response JSONObject
     * @return featureVector and weightVector
     */
    public Pair<List<Object>, List<Integer>> getVector(JSONObject response, JSONObject stripped) {
        JSONObject structure = new JSONObject(response.toString());

        List<Object> featureVector = new ArrayList<>();
        List<Integer> weightVector = new ArrayList<>();

        Queue<Triple<JSONObject, Integer, JSONObject>> queue = new LinkedList<>();
        queue.add(new Triple<>(structure, 0, stripped));

        while (!queue.isEmpty()) {
            Triple<JSONObject, Integer, JSONObject> triple = queue.poll();
            JSONObject object = triple.getKey();
            Integer depth = triple.getValue();
            JSONObject strippedObject = triple.getValue2();

            Iterator<String> it = object.keys();
            while (it.hasNext()) {
                String key = it.next();

                // If the key has value null
                if (object.isNull(key)) {
                    // If there is a null value, null is added as a string to the vector.
                    if (stripped.has(key)) {
                        featureVector.add("null");
                        weightVector.add(depth+1);
                    }
                    continue;
                }

                /// Skip key if it does not exist in the stripped JSONObject
                if (!strippedObject.has(key)) {
                    continue;
                }

                Object smallerObject = object.get(key);
                Object strippedSmallerObject = strippedObject.get(key);
                if (smallerObject instanceof JSONObject) {
                    queue.add(new Triple<>((JSONObject) smallerObject, depth+1, (JSONObject) strippedSmallerObject));
                } else if (smallerObject instanceof JSONArray) {
                    JSONArray array = ((JSONArray) smallerObject);
                    JSONArray strippedArray = ((JSONArray) strippedSmallerObject);

                    if (array.length() == 0) {
                        // TODO maybe add something here (empty array) (maybe add the length of the array as a value?)
                        continue;
                    }

                    if (array.isNull(0)) {
                        featureVector.add("null");
                    }

                    Object arrayObject = array.get(0);
                    Object strippedArrayObject = strippedArray.get(0);

                    // TODO currently we assume there are no arrays in arrays
                    // use first object of array
                    if (arrayObject instanceof JSONObject) {
                        queue.add(new Triple<>((JSONObject) arrayObject, depth+1, (JSONObject) strippedArrayObject));
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

    public void printResults() {
        System.out.println("Methods covered: " + clusteringPerResponseStructure.keySet().size());
        for (String method: clusteringPerResponseStructure.keySet()) {
            System.out.println("\t" + method + ": ");
            System.out.println("\t\tStatusses covered: " + statuses.get(method).size());
            System.out.println("\t\tStructures covered: " + clusteringPerResponseStructure.get(method).keySet().size());

            for (String structure : clusteringPerResponseStructure.get(method).keySet()) {
                System.out.println("\t\t\tClusters: " + clusteringPerResponseStructure.get(method).get(structure).getClusters().size());

                List<Integer> clusterSize = new ArrayList<>();

                StringBuilder individuals = new StringBuilder();

//                for (Cluster cluster : clusteringPerResponseStructure.get(method).get(structure).getClusters()) {
//                    clusterSize.add(cluster.size());
//                    // printing
//                    for (List<Object> vector: cluster) {
//                        individuals.append("\t\t\t\t\t").append(vector.toString()).append("\n");
//                    }
//                    individuals.append("\n");
//                }

                System.out.println("\t\t\t\t" + clusterSize.toString());
                System.out.println(individuals);

            }
        }
    }

}