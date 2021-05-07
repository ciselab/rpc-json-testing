package search.objective;

import connection.Client;
import connection.ResponseObject;
import org.json.JSONArray;
import org.json.JSONObject;
import search.Generator;
import search.Individual;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Use complexity of the request and response object (layers of JSON object) in the fitness calculation.
 */
public class ResponseStructureFitness3 extends Fitness {
    private static String STANDARD_STRING = "";
    private static Boolean STANDARD_BOOLEAN = true;
    private static Integer STANDARD_NUMBER = 0;

    private Map<String, Integer> structureFrequencyTable;

    private double ARCHIVE_THRESHOLD = 0.8;

    public ResponseStructureFitness3(Client client) {
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
//            System.out.println("Ind " + i + ": " + structureString);
            if (!structureFrequencyTable.containsKey(structureString)) {
                structureFrequencyTable.put(structureString, 0);
            }
            structureFrequencyTable.put(structureString, structureFrequencyTable.get(structureString) + 1);
        }

        double totalFitness = 0;

        for (int i = 0; i < population.size(); i++) {
            String structureString = stripValues(population.get(i).toRequest(), responses.get(i).getResponseObject()).toString();

            int inputComplexity = calculateComplexity(population.get(i).toRequest());
            int outputComplexity = calculateComplexity(responses.get(i).getResponseObject());

            double exploitationFitness = 1.0 / (1.0 + (double) structureFrequencyTable.get(structureString));
            // every key should be mutated atleast once
            double explorationFitness = (inputComplexity + outputComplexity);

//            System.out.println(structureFrequencyTable.get(stripValues(responses.get(i).getResponseObject()).toString()));
            double fitness = exploitationFitness * explorationFitness;
            totalFitness += fitness;
            population.get(i).setFitness(fitness);

//            ARCHIVE_THRESHOLD = Math.min((100 / structureFrequencyTable.size()), ARCHIVE_THRESHOLD); // if structure is relatively rare, add to archive.
            // decide whether to add individual to the archive
            if (fitness >= ARCHIVE_THRESHOLD && !archive.contains(population.get(i))) {
                this.addToArchive(population.get(i));
            }
        }

//        System.out.println("average fitness: " + totalFitness / population.size());
        System.out.println("Map: " + structureFrequencyTable.keySet().size());
    }


    private int calculateComplexity(JSONObject response) {
        int complexity = 0;

        JSONObject structure = new JSONObject(response.toString());

        Queue<JSONObject> queue = new LinkedList<>();
        queue.add(structure);

        while(!queue.isEmpty()) {
            JSONObject object = queue.poll();
            Iterator<String> it = object.keys();
            while (it.hasNext()) {
                complexity++;
                String key = it.next();
                Object smallerObject = object.get(key);
                if (smallerObject instanceof JSONObject) {
                    queue.add((JSONObject) object.get(key));
                } else if (smallerObject instanceof JSONArray) {
                    JSONArray array = ((JSONArray) smallerObject);
                    for (int i = 0; i < array.length(); i++) {
                        Object arrayObject = array.get(i);
                        if (arrayObject instanceof JSONObject) {
                            queue.add((JSONObject) arrayObject);
                        }
                        // TODO currently it is assuming no arrays in arrays
                    }
                }
            }
        }
        return complexity;
    }
}
