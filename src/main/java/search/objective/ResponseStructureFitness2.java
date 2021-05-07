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

/**
 * This ResponseStructureFitness uses the stripValues function from Fitness.
 */
public class ResponseStructureFitness2 extends Fitness {
    private static String STANDARD_STRING = "";
    private static Boolean STANDARD_BOOLEAN = true;
    private static Integer STANDARD_NUMBER = 0;

    private Map<String, Integer> structureFrequencyTable;

    private double ARCHIVE_THRESHOLD = 0.8;

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

        // Fill in hashmap with structure frequency
        for (int i = 0; i < population.size(); i++) {
            String structureString = stripValues(population.get(i).toRequest(), responses.get(i).getResponseObject()).toString();
//            System.out.println("Ind " + i + ": " + structureString);
            if (!structureFrequencyTable.containsKey(structureString)) {
                structureFrequencyTable.put(structureString, 0);
            }
            structureFrequencyTable.put(structureString, structureFrequencyTable.get(structureString) + 1);
//        }
//
            // Evaluate individual compared to the map
//        for (int i = 0; i < population.size(); i++) {
//            System.out.println(structureFrequencyTable.get(stripValues(responses.get(i).getResponseObject()).toString()));
            double fitness = (double) 1 / structureFrequencyTable.get(stripValues(population.get(i).toRequest(), responses.get(i).getResponseObject()).toString());
            population.get(i).setFitness(fitness);

//            ARCHIVE_THRESHOLD = Math.min((100 / structureFrequencyTable.size()), ARCHIVE_THRESHOLD); // if structure is relatively rare, add to archive.
            // decide whether to add individual to the archive
            if (fitness >= ARCHIVE_THRESHOLD && !archive.contains(population.get(i))) {
                this.addToArchive(population.get(i));
            }
        }

        System.out.println("Map: " + structureFrequencyTable.keySet().size());
    }

}
