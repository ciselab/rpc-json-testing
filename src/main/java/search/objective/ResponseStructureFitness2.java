package search.objective;

import connection.Client;
import connection.ResponseObject;
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

public class ResponseStructureFitness2 extends Fitness {
    private static String STANDARD_STRING = "";
    private static Boolean STANDARD_BOOLEAN = true;
    private static Integer STANDARD_NUMBER = 0;

    private Map<String, Integer> structureFrequencyTable;


    public ResponseStructureFitness2(Client client) {
        super(client);
        this.structureFrequencyTable = new HashMap<>();
    }

    @Override
    public void evaluate(Generator generator, Individual individual) throws IOException {
        // Cannot evaluate one individual in this case. Evaluation is based on entire generation.
    }

    @Override
    public void evaluate(Generator generator, List<Individual> population) {

        List<ResponseObject> responses = getResponses(population);

        for (int i = 0; i < population.size(); i++) {
            String structureString = stripValues(population.get(i).toRequest(), responses.get(i).getResponseObject()).toString();

//            System.out.println("Ind " + i + ": ");
//            System.out.println(population.get(i).toRequest().toString());
//            System.out.println(responses.get(i).getResponseObject().toString());
//            System.out.println(structureString);

            if (!structureFrequencyTable.containsKey(structureString)) {
                structureFrequencyTable.put(structureString, 0);
            }
            structureFrequencyTable.put(structureString, structureFrequencyTable.get(structureString) + 1);
        }

        for (int i = 0; i < population.size(); i++) {
//            System.out.println(structureFrequencyTable.get(stripValues(responses.get(i).getResponseObject()).toString()));
            double fitness = (double) 1 / structureFrequencyTable.get(stripValues(population.get(i).toRequest(), responses.get(i).getResponseObject()).toString());
            population.get(i).setFitness(fitness);
        }

        System.out.println("Map: " + structureFrequencyTable.keySet().size());
    }
//
//    /**
//     * Copy the response JSONObject and remove the values.
//     * @param response
//     * @return JSONObject with standard values, but key structure intact.
//     */
//    public JSONObject stripValues(JSONObject response) {
//        JSONObject structure = new JSONObject(response.toString());
//
//        Queue<JSONObject> queue = new LinkedList<>();
//        queue.add(structure);
//
//        while(!queue.isEmpty()) {
//            JSONObject object = queue.poll();
//            Iterator<String> it = object.keys();
//            while (it.hasNext()) {
//                String key = it.next();
//                Object smallerObject = object.get(key);
//                if (smallerObject instanceof JSONObject) {
//                    queue.add((JSONObject) object.get(key));
//                } else if (smallerObject instanceof JSONArray) {
//                    JSONArray array = ((JSONArray) smallerObject);
//                    for (int i = 0; i < array.length(); i++) {
//                        Object arrayObject = array.get(i);
//                        if (arrayObject instanceof JSONObject) {
//                            queue.add((JSONObject) arrayObject);
//                        } else if (arrayObject instanceof JSONString) {
//                            array.put(i, STANDARD_STRING);
//                        } else if (arrayObject instanceof Number) {
//                            array.put(i, STANDARD_NUMBER);
//                        } else if (arrayObject instanceof Boolean) {
//                            array.put(i, STANDARD_BOOLEAN);
//                        }
//                        // TODO currently it is assuming no arrays in arrays
//                    }
//                } else if (smallerObject instanceof String) {
//                    object.put(key, STANDARD_STRING);
//                } else if (smallerObject instanceof Number) {
//                    object.put(key, STANDARD_NUMBER);
//                } else if (smallerObject instanceof Boolean) {
//                    object.put(key, STANDARD_BOOLEAN);
//                } else {
////                    System.out.println(smallerObject.toString());
////                    System.out.println(smallerObject.getClass());
//                }
//            }
//        }
//        return structure;
//    }

}
