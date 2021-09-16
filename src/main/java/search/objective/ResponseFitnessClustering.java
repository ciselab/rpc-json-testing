package search.objective;

import search.Generator;
import search.Individual;
import search.clustering.AgglomerativeClustering;

import util.Configuration;
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
 * Cluster every individual.
 */
public class ResponseFitnessClustering extends Fitness {

    private static String separator = "/";

    // MAP<METHOD, MAP<STRUCTURE, LIST<PREVIOUS VALUES>>>
    private Map<String, Map<String, AgglomerativeClustering>> clusteringPerResponseStructure;
    private Map<String, Set<Integer>> statuses;

    public ResponseFitnessClustering() {
        super();
        this.clusteringPerResponseStructure = new HashMap<>();
        this.statuses = new HashMap<>();
    }

    @Override
    public void evaluate(Generator generator, List<Individual> population) {
        for (Individual individual : population) {
            String method = individual.getDna().get(individual.getDna().size() - 1).getMethod();
            JSONObject request = individual.toTotalJSONObject();
            JSONObject response = individual.getResponseObject().getResponseObject();

            JSONObject stripped = stripValues(request, response);
            String strippedString = stripped.toString();

            Pair<List<Object>, List<Integer>> featureAndWeightVector = getVector(response, stripped);

            // If empty object just give it a fitness of zero
            if (featureAndWeightVector.getKey().size() == 0) {
                individual.setFitness(0);
                continue;
            }

            if (!clusteringPerResponseStructure.containsKey(method)) {
                clusteringPerResponseStructure.put(method, new HashMap<>());
                statuses.put(method, new HashSet<>());
            }

            statuses.get(method).add(individual.getResponseObject().getResponseCode());


            if (!clusteringPerResponseStructure.get(method).containsKey(strippedString)) {
                clusteringPerResponseStructure.get(method).put(strippedString, new AgglomerativeClustering(featureAndWeightVector.getValue()));
            }

            AgglomerativeClustering clustering = clusteringPerResponseStructure.get(method).get(strippedString);

            double cost = clustering.cluster(featureAndWeightVector.getKey());

            double fitness = 1.0 / (1 + cost);
            // TODO not use this hack for worst output
            if (individual.getDna().get(individual.getDna().size() - 1).getMethod().equals("random") ||
                individual.getDna().get(individual.getDna().size() - 1).getMethod().equals("server_info") ||
                individual.getDna().get(individual.getDna().size() - 1).getMethod().equals("server_state")) {
                fitness = 0;
            }
            individual.setFitness(fitness);

            // decide whether to add individual to the archive
            if (individual.getResponseObject().getResponseCode() > 499 && !getArchive().contains(individual)) {
                this.addToArchive(individual);
            } else if (fitness >= Configuration.ARCHIVE_THRESHOLD && !getArchive().contains(individual)) {
                this.addToArchive(individual);
            }
        }
    }

    @Override
    public ArrayList<String> storeInformation() {
        ArrayList<String> info = new ArrayList<>();

        info.add("Methods covered: " + clusteringPerResponseStructure.keySet().size());
        for (String method : clusteringPerResponseStructure.keySet()) {
            info.add("\t" + method + ": ");
            info.add("\t\tStatusses covered: " + statuses.get(method).size() + ", namely: " + statuses.get(method).toString());
            info.add("\t\tStructures covered: " + clusteringPerResponseStructure.get(method).keySet().size());

            for (String structure : clusteringPerResponseStructure.get(method).keySet()) {
                info.add("\t\t\tClusters: " + clusteringPerResponseStructure.get(method).get(structure).getClusters().size());

                List<Integer> clusterSize = new ArrayList<>();

                StringBuilder individuals = new StringBuilder();

                for (List<List<Object>> cluster : clusteringPerResponseStructure.get(method).get(structure).getClusters()) {
                    clusterSize.add(cluster.size());
                    for (List<Object> vector : cluster) {
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
     * Calculate the feature vector and the weight vector.
     *
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
            Triple<JSONObject, Integer, JSONObject> pair = queue.poll();
            JSONObject object = pair.getKey();
            Integer depth = pair.getValue();
            JSONObject strippedObject = pair.getValue2();

            Iterator<String> it = object.keys();
            while (it.hasNext()) {
                String key = it.next();

                // Skip this key if the value is null or if it does not exist in the stripped JSONObject
                if (object.isNull(key)) {
                    // TODO should we do this? It  can occur that an error_message is null for example.
                    if (stripped.has(key)) {
                        featureVector.add("null");
                        weightVector.add(depth + 1);
                    }
                    continue;
                }

                if (!strippedObject.has(key)) {
                    continue;
                }

                Object smallerObject = object.get(key);
                Object strippedSmallerObject = strippedObject.get(key);
                if (smallerObject instanceof JSONObject) {
                    queue.add(new Triple<>((JSONObject) smallerObject, depth + 1, (JSONObject) strippedSmallerObject));
                } else if (smallerObject instanceof JSONArray) {
                    JSONArray array = ((JSONArray) smallerObject);
                    JSONArray strippedArray = ((JSONArray) strippedSmallerObject);


                    if (array.length() == 0) {
                        // TODO maybe add something here (empty array) (maybe add the length of the array as a value)
                        continue;
                    }

                    if (array.isNull(0)) {
                        // TODO maybe add this
                        featureVector.add("null");
                    }

                    Object arrayObject = array.get(0);
                    Object strippedArrayObject = strippedArray.get(0);

                    // TODO assumes no arrays in arrays
                    // just take first object of array
                    if (arrayObject instanceof JSONObject) {
                        queue.add(new Triple<>((JSONObject) arrayObject, depth + 1, (JSONObject) strippedArrayObject));
                    } else {
                        featureVector.add(arrayObject);
                        weightVector.add(depth + 1);
                    }
                } else {
                    featureVector.add(smallerObject);
                    weightVector.add(depth + 1);
                }
            }
        }

        return new Pair<>(featureVector, weightVector);
    }

}
