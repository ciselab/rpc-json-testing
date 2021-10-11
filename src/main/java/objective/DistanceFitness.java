package objective;

import connection.ResponseObject;
import org.json.JSONArray;
import org.json.JSONObject;
import search.Generator;
import search.Individual;
import search.clustering.SimilarityMetric;
import statistics.Collector;
import util.Pair;
import util.Triple;

import java.util.*;

import static statistics.Collector.getCollector;
import static util.ObjectStripper.stripValues;

/**
 * Cluster once every X generations.
 */
public class DistanceFitness extends Fitness {

    // method -> response -> response object
    private Map<String, Map<String, JSONObject>> strippedResponses;
    // method -> response -> feature vector list
    private Map<String, Map<String, List<List<Object>>>> featureVectors;
    // method -> status code set
    private Map<String, Set<Integer>> statuses;

    private SimilarityMetric similarityMetric;

    public DistanceFitness() {
        super();
        this.strippedResponses = new HashMap<>();
        this.featureVectors = new HashMap<>();
        this.statuses = new HashMap<>();
        this.similarityMetric = new SimilarityMetric();
    }

    @Override
    public void evaluate(Generator generator, List<Individual> population) {
        for (Individual individual : population) {
            String method = individual.getDna().get(individual.getDna().size() - 1).getApiMethod();

            double[] fitness = new double[generator.getSpecification().getMethods().size()];

            // TODO not use this hack for worst output
            if (method.equals("random") ||
                    method.equals("tx") ||
                    method.equals("server_info") ||
                    method.equals("server_state")) {
                individual.setFitness(fitness);
                continue;
            }

            if (!featureVectors.containsKey(method)) {
                featureVectors.put(method, new HashMap<>());
                strippedResponses.put(method, new HashMap<>());
                statuses.put(method, new HashSet<>());
            }

            ResponseObject responseObject = individual.getResponseObject();
            JSONObject request = responseObject.getRequestObject();
            JSONObject response = responseObject.getResponseObject();

            JSONObject strippedRequest = stripValues(request, request);
            Collector.getCollector().addRequest(strippedRequest.toString());

            JSONObject strippedResponse = stripValues(request, response);
            String strippedString = strippedResponse.toString();

            // TODO check if subtree


            Pair<List<Object>, List<Integer>> featureAnddepthVector = getVector(response, stripValues(request, response, false));
            List<Object> featureVector = featureAnddepthVector.getKey();
            List<Integer> depthVector = featureAnddepthVector.getValue();

            // If the response is an empty object just give it a fitness of zero
            if (featureAnddepthVector.getKey().size() == 0) {
                individual.setFitness(fitness);
                continue;
            }
            double minDistance = Double.MAX_VALUE;

            if (featureVectors.get(method).containsKey(strippedString)) {
                for (List<Object> featureVector2: featureVectors.get(method).get(strippedString)) {
                    double distance = similarityMetric.calculateFeatureVectorDistanceSingle(featureVector, featureVector2, depthVector);
                    minDistance = Math.min(minDistance, distance);
                }

                fitness[generator.getSpecification().getMethodNames().indexOf(method)] = 1.0 - Math.exp(-0.5*minDistance);
            } else {
                featureVectors.get(method).put(strippedString, new ArrayList<>());
                strippedResponses.get(method).put(strippedString, strippedResponse);
                fitness[generator.getSpecification().getMethodNames().indexOf(method)] = 2.0;
            }

            individual.setFitness(fitness);

            if (minDistance != 0.0) {
                // don't add if the vectors are exactly equal (distance == 0)
                featureVectors.get(method).get(strippedString).add(featureVector);
            }
            statuses.get(method).add(responseObject.getResponseCode());


            getCollector().collect(method, responseObject.getResponseCode(), strippedString, String.valueOf(featureAnddepthVector.getKey()));

            // decide whether to add individual to the archive
            getCollector().addToArchive(strippedString, individual);
        }

        for (String method : featureVectors.keySet()) {
            int total = 0;
            for (String stripped : featureVectors.get(method).keySet()) {
                total += featureVectors.get(method).get(stripped).size();
            }

            System.out.println("\tMethod: " + method + "\n\t\tStatuses: " + statuses.get(method).size() + "\n\t\tStructures: " + featureVectors.get(method).size() +  "\n\t\tVectors: " + total);

        }
    }

    @Override
    public ArrayList<String> storeInformation() {
        ArrayList<String> info = new ArrayList<>();

        return info;
    }

    /**
     * Calculate the feature vector and the weight vector. Make use of depth of keys.
     *
     * @param stripped the stripped response JSONObject
     * @param response the response JSONObject
     * @return featureVector and depthVector
     */
    public Pair<List<Object>, List<Integer>> getVector(JSONObject response, JSONObject stripped) {
        JSONObject structure = new JSONObject(response.toString());

        List<Object> featureVector = new ArrayList<>();
        List<Integer> depthVector = new ArrayList<>();

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
                        depthVector.add(depth+1);
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
                    if (strippedArray.length() == 0) {
                        // // System.out.println(array);
                        // // System.out.println(strippedArray);
                    }
                    Object strippedArrayObject = strippedArray.get(0);

                    // TODO currently we assume there are no arrays in arrays
                    // use first object of array
                    if (arrayObject instanceof JSONObject) {
                        queue.add(new Triple<>((JSONObject) arrayObject, depth+1, (JSONObject) strippedArrayObject));
                    } else {
                        featureVector.add(arrayObject);
                        depthVector.add(depth+1);
                    }
                } else {
                    featureVector.add(smallerObject);
                    depthVector.add(depth+1);
                }
            }
        }
        return new Pair<>(featureVector, depthVector);
    }

}
