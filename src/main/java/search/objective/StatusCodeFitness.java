package search.objective;

import connection.ResponseObject;
import search.Generator;
import search.Individual;
import test_drivers.TestDriver;

import java.util.ArrayList;
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

        if (getTestDriver().shouldContinue()) {

            for (int i = 0; i < population.size(); i++) {
                int responseCode = responses.get(i).getResponseCode();
                if (!statusFrequencyTable.containsKey(responseCode)) {
                    statusFrequencyTable.put(responseCode, 0);
                }
                statusFrequencyTable.put(responseCode, statusFrequencyTable.get(responseCode) + 1);

                // If statuscode occurs only once in a large population, it is more rare than if it occurs once in a small population.
                double fitness = 1 / statusFrequencyTable.get(responses.get(i).getResponseCode());

                Individual ind = population.get(i);
                ind.setFitness(fitness);

                // If statuscode is relatively rare, add to archive.
                ARCHIVE_THRESHOLD = Math.min((100 / statusFrequencyTable.size()), ARCHIVE_THRESHOLD);
                // Decide whether to add individual to the archive
                if (responses.get(i).getResponseCode() > 499 && !getArchive().contains(ind)) {
                    this.addToArchive(ind, responses.get(i));
                } else if (fitness >= ARCHIVE_THRESHOLD && !getArchive().contains(ind)) {
                    this.addToArchive(ind, responses.get(i));
                }
            }

        }
    }

    @Override
    public ArrayList<String> storeInformation() {
        ArrayList<String> info = new ArrayList<>();
        info.add(statusFrequencyTable.toString());
        return info;
    }
}
