//package objective;
//
//import connection.Client;
//import connection.ResponseObject;
//import org.json.JSONArray;
//import org.json.JSONObject;
//import search.Generator;
//import search.Individual;
//import search.clustering.SimilarityMetric;
//import test_drivers.TestDriver;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.Queue;
//
//public class PairwiseFitness extends Fitness {
//
//    public PairwiseFitness(TestDriver testDriver) {
//        super(testDriver);
//    }
//
//    @Override
//    public void evaluate(Generator generator, List<Individual> population) {
//
//        List<ResponseObject> responses = getResponses(population);
//
//        // Map with API methods as key and a map as value. This second map has skeletons as key and a list of feature vectors as value.
//        Map<String, Map<String, List<List<Object>>>> allFeatureVectors = new HashMap<>();
//
//        double[][] similarityMatrix = new double[population.size()][population.size()];
//
//        for (int i = 0; i < population.size(); i++) {
//            String method = population.get(i).getApiMethod();
//            JSONObject request = population.get(i).toRequest();
//            JSONObject response = responses.get(i).getResponseObject();
//            similarityMatrix[i][i] = 1.0; // same object so similarity == 1
//
//            for (int j = i + 1; j < population.size(); j++) {
//                String method2 = population.get(j).getApiMethod();
//                JSONObject request2 = population.get(j).toRequest();
//                JSONObject response2 = responses.get(j).getResponseObject();
//
//                similarityMatrix[i][j] = calculateSimilarity(response, response2);
//                similarityMatrix[j][i] = similarityMatrix[i][j];
//            }
//        }
//
//        for (int i = 0; i < population.size(); i++) {
//            double minSimilarity = 1.0;
//
//            for (int j = 0; j < population.size(); j++) {
//                minSimilarity = Math.min(minSimilarity, similarityMatrix[i][j]);
//            }
//
//            population.get(i).setFitness(minSimilarity);
//
//            // System.out.println(population.get(i).toRequest().toString());
//            // System.out.println("fitness: " + minSimilarity);
//        }
//    }
//
//    /**
//     * compare all
//     * @param method1
//     * @param request1
//     * @param response1
//     * @param method2
//     * @param request2
//     * @param response2
//     * @return
//     */
//    public double calculateSimilarity(String method1, JSONObject request1, JSONObject response1, String method2, JSONObject request2, JSONObject response2) {
//
//        // method equal is 10 points
//        return 0.0;
//    }
//
//    /**
//     * Compare responses only
//     * @param response1
//     * @param response2
//     * @return
//     */
//    public double calculateSimilarity(JSONObject response1, JSONObject response2) {
//        Map<String, Object> keyValuePairs1 = getLeaveKeyValuePairs(response1);
//        Map<String, Object> keyValuePairs2 = getLeaveKeyValuePairs(response2);
//
//        // penalty = 2 if missing key
//        // penalty = 1.5 if values are differnet types
//        // penalty between 0 and 1 if value is different
//        // penalty 0 if same value
//        double penalty = 0;
//        for (String key : keyValuePairs1.keySet()) {
//            if (!keyValuePairs2.containsKey(key)) {
//                penalty += 2;
//                continue;
//            }
//
//            // key available compare
//            Object value1 = keyValuePairs1.get(key);
//            Object value2 = keyValuePairs2.get(key);
//
//            if (value1 == null) {
//                if (value2 != null) {
//                    penalty += 1.5;
//                }
//            } else if (value1 instanceof String) {
//                if (value2 instanceof String) {
//                    penalty += 1.0 - 1.0 / (1.0 + SimilarityMetric.stringDistance((String) value1, (String) value2));
//                } else {
//                    penalty += 1.5;
//                }
//            } else if (value1 instanceof Number) {
//                if (value2 instanceof Number) {
//                    penalty += 1.0 - 1.0 / (1.0 + SimilarityMetric.numberDistance(((Number) value1).doubleValue(), ((Number) value2).doubleValue()));
//                } else {
//                    penalty += 1.5;
//                }
//            } else if (value1 instanceof Boolean) {
//                if (value2 instanceof Boolean) {
//                    penalty += SimilarityMetric.boolDistance((Boolean) value1, (Boolean) value2);
//                } else {
//                    penalty += 1.5;
//                }
//            }
//
//            // remove key from other map
//            keyValuePairs2.remove(key);
//        }
//
//        for (String ignored : keyValuePairs2.keySet()) {
//            penalty += 2;
//        }
//
//        return 1.0 / (1.0 + penalty);
//    }
//
//    public Map<String, Object> getLeaveKeyValuePairs(JSONObject json) {
//        Map<String, Object> keyValuePairs = new HashMap<>();
//
//        // TODO this does not work when a key is used multiple times in the json
//
//        Queue<JSONObject> queue = new LinkedList<>();
//        queue.add(json);
//
//        while (!queue.isEmpty()) {
//            JSONObject object = queue.poll();
//
//            Iterator<String> it = object.keys();
//            while (it.hasNext()) {
//                String key = it.next();
//
//                // If the key has value null
//                if (object.isNull(key)) {
//                    keyValuePairs.put(key, null);
//                    continue;
//                }
//
//                Object smallerObject = object.get(key);
//
//                if (smallerObject instanceof JSONObject) {
//                    queue.add((JSONObject) smallerObject);
//                } else if (smallerObject instanceof JSONArray) {
//                    JSONArray array = ((JSONArray) smallerObject);
//
//                    if (array.length() == 0) {
//                        // TODO maybe add something here (empty array) (maybe add the length of the array as a value?)
//                        continue;
//                    }
//
//                    if (array.isNull(0)) {
//                        keyValuePairs.put(key + "_0", null);
//                        continue;
//                    }
//
//                    Object arrayObject = array.get(0);
//
//                    // TODO currently we assume there are no arrays in arrays
//                    // use first object of array
//                    if (arrayObject instanceof JSONObject) {
//                        queue.add((JSONObject) arrayObject);
//                    } else {
//                        keyValuePairs.put(key + "_0", arrayObject);
//                    }
//                } else {
//                    keyValuePairs.put(key, smallerObject);
//
//                }
//            }
//        }
//        return keyValuePairs;
//    }
//
//}
