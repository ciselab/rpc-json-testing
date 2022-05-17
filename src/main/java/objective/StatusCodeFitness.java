package objective;

import search.Generator;
import search.Individual;
import util.config.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static statistics.Collector.getCollector;

public class StatusCodeFitness extends Fitness {
    private Map<Integer, Integer> statusFrequencyTable;

    public StatusCodeFitness() {
        this.statusFrequencyTable = new HashMap<>();
    }

    @Override
    public void evaluate(Generator generator, List<Individual> population) {
        for (Individual ind : population) {
            int responseCode = ind.getResponseObject().getResponseCode();
            if (!statusFrequencyTable.containsKey(responseCode)) {
                statusFrequencyTable.put(responseCode, 0);
            }
            statusFrequencyTable.put(responseCode, statusFrequencyTable.get(responseCode) + 1);

            // If statuscode occurs only once in a large population, it is more rare than if it occurs once in a small population.
            // Fitness is between 0 and 1.
            double fitness = 1 / statusFrequencyTable.get(ind.getResponseObject().getResponseCode());

            ind.setFitness(fitness);

            // If statuscode is relatively rare, add to archive.
            double archive_threshold = Math.min((100 / statusFrequencyTable.size()), Configuration.ARCHIVE_THRESHOLD);
            // Decide whether to add individual to the archive
            if (ind.getResponseObject().getResponseCode() > 499) {
                getCollector().addToArchive(ind.getResponseObject().getResponseObject().toString(), ind);
            } else if (fitness >= archive_threshold) {
                getCollector().addToArchive(ind.getResponseObject().getResponseObject().toString(), ind);
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
