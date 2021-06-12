package search.objective;

import connection.Client;
import connection.ResponseObject;
import search.Generator;
import search.Individual;
import test_drivers.TestDriver;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatusCodeFitness extends Fitness {
    private Map<Integer, Integer> statusFrequencyTable;

    private double ARCHIVE_THRESHOLD = 0.8;

    public StatusCodeFitness(TestDriver testDriver) {
        super(testDriver);
        this.statusFrequencyTable = new HashMap<>();
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

            Individual ind = population.get(i);
            ind.setFitness(fitness);

            ARCHIVE_THRESHOLD = Math.min((100 / statusFrequencyTable.size()), ARCHIVE_THRESHOLD); // if statuscode is relatively rare, add to archive.
            // decide whether to add individual to the archive
            if (fitness >= ARCHIVE_THRESHOLD && !getArchive().contains(ind)) {
                this.addToArchive(ind);
            }

        }

        System.out.println(statusFrequencyTable);
    }
}
