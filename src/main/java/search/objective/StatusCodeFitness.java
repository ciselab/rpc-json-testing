package search.objective;

import connection.Client;
import connection.ResponseObject;
import search.Generator;
import search.Individual;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatusCodeFitness extends Fitness {
    private Map<Integer, Integer> statusFrequencyTable;

    private double ARCHIVE_THRESHOLD;

    public StatusCodeFitness(Client client) {
        super(client);
        this.statusFrequencyTable = new HashMap<>();
    }

    @Override
    public void evaluate(Generator generator, Individual individual) throws IOException {
        // Cannot evaluate one individual in this case. Evaluation is based on entire generation.
    }

    @Override
    public void evaluate(Generator generator, List<Individual> population) {

        List<ResponseObject> responses = getResponses(population);

//        this.statusFrequencyTable = new HashMap<>();
        for (int i = 0; i < population.size(); i++) {
            int responseCode = responses.get(i).getResponseCode();
            if (!statusFrequencyTable.containsKey(responseCode)) {
                statusFrequencyTable.put(responseCode, 0);
            }
            statusFrequencyTable.put(responseCode, statusFrequencyTable.get(responseCode)+1);
//        }
//
//        for (int i = 0; i < population.size(); i++) {
            // If statuscode occurs only once in a large population, it is more rare than if it occurs once in a small population.
            double fitness = 1 / statusFrequencyTable.get(responses.get(i).getResponseCode());
            System.out.println(responseCode);

            Individual ind = population.get(i);
            ind.setFitness(fitness);

            ARCHIVE_THRESHOLD = Math.min((100 / statusFrequencyTable.size()), ARCHIVE_THRESHOLD); // if statuscode is relatively rare, add to archive.
            // decide whether to add individual to the archive
            if (fitness >= ARCHIVE_THRESHOLD && !archive.contains(ind)) {
                this.addToArchive(ind);
            }

        }

        System.out.println(statusFrequencyTable);
    }
}
