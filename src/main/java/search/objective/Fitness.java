package search.objective;

import connection.ResponseObject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;
import search.Chromosome;
import search.Generator;
import search.Individual;
import test_drivers.TestDriver;
import util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public abstract class Fitness {

    private TestDriver testDriver;
    private List<Individual> archive;
    private Map<Integer, Integer> statusCodesTotal;
    private Map<Integer, Integer> statusCodesArchive;

    public Fitness(TestDriver testDriver) {
        this.testDriver = testDriver;
        this.archive = new ArrayList<>();
        this.statusCodesTotal = new HashMap<>();
        this.statusCodesArchive = new HashMap<>();
    }

    /**
     * Evaluate the population on fitness.
     * @param generator
     * @param population
     */
    public abstract void evaluate(Generator generator, List<Individual> population);

    /**
     * Store information regarding the process of the run.
     */
    public abstract List<String> storeInformation();

    /**
     * Get all responses from current generation of requests (i.e. individuals).
     *
     * @param population
     * @return list of ResponseObjects
     */
    public List<ResponseObject> getResponses(List<Individual> population) {
        List<ResponseObject> responses = new ArrayList<>();

        double averageEvalTime = 0;

        for (int i = 0; i < population.size(); i++) {

            if (testDriver.shouldContinue()) {

                Individual individual = population.get(i);
                long start = System.nanoTime();
                try {
//                System.out.println("Preparing tests");
                    testDriver.prepTest();

                    ResponseObject responseObject = null;

                    for (int j = 0; j < individual.getDna().size(); j++) {
                        Chromosome chromosome = individual.getDna().get(j);
                        responseObject = testDriver.runTest(chromosome.getHTTPMethod(), chromosome.toRequest());
                    }

                    if (responseObject == null) {
                        ResponseObject ro = new ResponseObject("", new JSONObject(),-999, new JSONObject());
                        responses.add(ro);
                        System.out.println("ResponseObject is null. This should never be the case!");
                        throw new Exception("Individual with zero chromosomes!!!");
                    }

                    responses.add(responseObject);

//                    System.out.println("Status code of response on individual " + i + " is " + responseObject.getResponseCode());

                    if (!statusCodesTotal.containsKey(responseObject.getResponseCode())) {
                        statusCodesTotal.put(responseObject.getResponseCode(), 0);
                    }
                    statusCodesTotal.put(responseObject.getResponseCode(), statusCodesTotal.get(responseObject.getResponseCode()) + 1);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                averageEvalTime += (System.nanoTime() - start);

                testDriver.checkWhetherToStop();
            }

        }

        averageEvalTime /= (population.size() * 1000000);
//        System.out.println("Average test time: " + averageEvalTime + " ms");
        return responses;
    }

    private static String STANDARD_STRING = "";
    private static Boolean STANDARD_BOOLEAN = true;
    private static Integer STANDARD_NUMBER = 0;
    /**
     * Copy the response JSONObject and remove the values.
     * Also remove keys with values which where given in the request.
     * Finally also remove keys which are possibly random.
     *
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

        while (!queue.isEmpty()) {
            Pair<JSONObject, JSONObject> pair = queue.poll();
            JSONObject object = pair.getKey();
            JSONObject strippedObject = pair.getValue();

            Iterator<String> it = object.keys();
            while (it.hasNext()) {
                String key = it.next();

                Object smallerObject = object.get(key);
                if (smallerObject instanceof JSONObject) {
                    JSONObject smallerStrippedObject = new JSONObject();
                    strippedObject.put(key, smallerStrippedObject);
                    queue.add(new Pair<>((JSONObject) object.get(key), smallerStrippedObject));
                } else if (smallerObject instanceof JSONArray) {
                    JSONArray array = ((JSONArray) smallerObject);

                    JSONArray smallerStrippedArray = new JSONArray();

                    strippedObject.put(key, smallerStrippedArray);

                    if (array.length() > 0) {
                        Object arrayObject = array.get(0);
                        if (arrayObject instanceof JSONObject) {
                            JSONObject evenSmallerObject = new JSONObject();
                            smallerStrippedArray.put(0, evenSmallerObject);

                            queue.add(new Pair<>((JSONObject) arrayObject, evenSmallerObject));
                        } else if (arrayObject instanceof String) {
                            smallerStrippedArray.put(0, STANDARD_STRING);
                        } else if (arrayObject instanceof Number) {
                            smallerStrippedArray.put(0, STANDARD_NUMBER);
                        } else if (arrayObject instanceof Boolean) {
                            smallerStrippedArray.put(0, STANDARD_BOOLEAN);
                        } else if (array.isNull(0)) {
                            smallerStrippedArray.put(0, "null");
                        }
                        // TODO currently it is assuming no arrays in arrays
                    }
                } else if (!(requestKeyValuePairs.containsKey(key) && requestKeyValuePairs.get(key).equals(smallerObject))) {
                    if (smallerObject instanceof String) {
                        strippedObject.put(key, STANDARD_STRING);
                    } else if (smallerObject instanceof Number) {
                        strippedObject.put(key, STANDARD_NUMBER);
                    } else if (smallerObject instanceof Boolean) {
                        strippedObject.put(key, STANDARD_BOOLEAN);
                    } else if (object.isNull(key)) {
                        strippedObject.put(key, "null");
                    }
                }
            }
        }
        return copy;
    }

    /**
     * Check the key and value pairs in the request, match them with the key and value pairs in the response and if they are a match, do not include this pair in the feature vector.
     * @param request
     * @return the key-value pairs in the response JSON Object.
     */
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

    public List<Individual> getArchive() {
        return archive;
    }

    public void addToArchive(Individual ind, ResponseObject res) {
        archive.add(ind);

        if (!statusCodesArchive.containsKey(res.getResponseCode())) {
            statusCodesArchive.put(res.getResponseCode(), 0);
        }
        statusCodesArchive.put(res.getResponseCode(), statusCodesArchive.get(res.getResponseCode()) + 1);
    }

    public Map<Integer, Integer> getStatusCodesTotal() {
        return statusCodesTotal;
    }

    public Map<Integer, Integer> getStatusCodesArchive() {
        return statusCodesArchive;
    }

    public TestDriver getTestDriver() {
        return testDriver;
    }

}
