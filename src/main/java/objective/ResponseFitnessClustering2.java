package objective;

import search.Generator;
import search.Individual;
import search.clustering.AgglomerativeClustering2;

import org.json.JSONArray;
import org.json.JSONObject;
import util.config.Configuration;
import util.datastructures.Pair;
import util.datastructures.Triple;

import java.util.*;

import static statistics.Collector.getCollector;
import static util.ObjectStripper.stripValues;

/**
 * Cluster once every X generations. Fitness is the minimum distance to any other individual in the cluster.
 */
public class ResponseFitnessClustering2 extends Fitness {

    private static String separator = "/";

    // MAP<METHOD, MAP<STRUCTURE, LIST<PREVIOUS VALUES>>>
    private Map<String, Map<String, AgglomerativeClustering2>> clusteringPerResponseStructure;
    private Map<String, Set<Integer>> statuses;

    // Count the number of generations
    private int generationCount;

    public ResponseFitnessClustering2() {
        super();
        this.clusteringPerResponseStructure = new HashMap<>();
        this.statuses = new HashMap<>();
        this.generationCount = 0;
    }

    @Override
    public void evaluate(Generator generator, List<Individual> population) {
        // Map with API methods as key and a map as value. This second map has skeletons as key and a list of feature vectors as value.
        Map<String, Map<String, List<List<Object>>>> allFeatureVectors = new HashMap<>();

        for (Individual individual : population) {
            String method = individual.getDna().get(individual.getDna().size() - 1).getApiMethod();
            JSONObject request = individual.toTotalJSONObject();
            JSONObject response = individual.getResponseObject().getResponseObject();

            JSONObject stripped = stripValues(request, response);
            String strippedString = stripped.toString();

            Pair<List<Object>, List<Integer>> featureAndWeightVector = getVector(response, stripped);

            // If the response is an empty object just give it a fitness of zero
            if (featureAndWeightVector.getKey().size() == 0) {
                individual.setFitness(0);
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

            statuses.get(method).add(individual.getResponseObject().getResponseCode());

            if (!clusteringPerResponseStructure.get(method).containsKey(strippedString)) {
                clusteringPerResponseStructure.get(method).put(strippedString, new AgglomerativeClustering2(featureAndWeightVector.getValue()));
            }

            AgglomerativeClustering2 clustering = clusteringPerResponseStructure.get(method).get(strippedString);

            // calculate the minimum distance of the individual to the clusters
            double cost = clustering.calculateMaxSimilarity(featureAndWeightVector.getKey());

            // Fitness is between 0 and 1.
            double fitness = 1.0 / (1 + cost);

            individual.setFitness(fitness);

            // decide whether to add individual to the archive
            getCollector().addToArchive(stripped.toString(), individual);

        }
        if (generationCount % Configuration.NEW_CLUSTERS_AFTER_GEN == 0) {
            for (String method : allFeatureVectors.keySet()) {
                for (String responseStructure : allFeatureVectors.get(method).keySet()) {
                    clusteringPerResponseStructure.get(method).get(responseStructure)
                            .cluster(allFeatureVectors.get(method).get(responseStructure));
                }
            }
        }
        generationCount += 1;

    }

    @Override
    public ArrayList<String> storeInformation() {
        ArrayList<String> info = new ArrayList<>();

        info.add("Methods covered: " + clusteringPerResponseStructure.keySet().size());
        for (String method: clusteringPerResponseStructure.keySet()) {
            info.add("\t" + method + ": ");
            info.add("\t\tStatusses covered: " + statuses.get(method).size() + ", namely: " + statuses.get(method).toString());
            info.add("\t\tStructures covered: " + clusteringPerResponseStructure.get(method).keySet().size());

            for (String structure : clusteringPerResponseStructure.get(method).keySet()) {
                info.add("\t\t\tClusters: " + clusteringPerResponseStructure.get(method).get(structure).getClusters().size());

                List<Integer> clusterSize = new ArrayList<>();

                StringBuilder individuals = new StringBuilder();

                for (List<List<Object>> cluster : clusteringPerResponseStructure.get(method).get(structure).getClusters()) {
                    clusterSize.add(cluster.size());
                    for (List<Object> vector: cluster) {
                        individuals.append("\t\t\t\t\t").append(vector.toString()).append("\n");
                    }
                    individuals.append("\n");
                }

                info.add("\t\t\t\t" + clusterSize.toString());
                info.add(individuals.toString());

            }
        }
        return info;
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
                        continue;
                    }

                    if (array.isNull(0)) {
                        featureVector.add("null");
                    }

                    Object arrayObject = array.get(0);
                    Object strippedArrayObject = strippedArray.get(0);

                    // we assume there are no arrays in arrays
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

}
