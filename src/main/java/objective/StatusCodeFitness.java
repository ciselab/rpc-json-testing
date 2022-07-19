package objective;

import search.Generator;
import search.Individual;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static statistics.Collector.getCollector;
import static util.IO.writeFile;

public class StatusCodeFitness extends Fitness {
    private Map<Integer, Integer> statusFrequencyTable;
    private int generationCount;

    public StatusCodeFitness() {
        this.statusFrequencyTable = new HashMap<>();
        this.generationCount = 0;
    }


    @Override
    public void evaluate(Generator generator, List<Individual> population) {
        for (Individual ind : population) {
            int responseCode = ind.getResponseObject().getResponseCode();
            if (!statusFrequencyTable.containsKey(responseCode)) {
                statusFrequencyTable.put(responseCode, 0);
            }
            statusFrequencyTable.put(responseCode, statusFrequencyTable.get(responseCode) + 1);

            // If status code occurs only once in a large population, it is more rare than if it occurs once in a small population.
            // Fitness is between 0 and 1.
            double fitness = 1 / statusFrequencyTable.get(ind.getResponseObject().getResponseCode());

            ind.setFitness(fitness);

            // Decide whether to add individual to the archive
            getCollector().addToArchive(ind.getResponseObject().getResponseObject().toString(), ind);
        }

        try {
            String info = "Generation: " + generationCount
                    + System.lineSeparator()
                    + storeInformation().toString();

            writeFile(info, "clustering.txt", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        generationCount += 1;
    }

    @Override
    public ArrayList<String> storeInformation() {
        ArrayList<String> info = new ArrayList<>();
        info.add(statusFrequencyTable.toString());
        return info;
    }
}
