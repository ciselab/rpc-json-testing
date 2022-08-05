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
import static util.ObjectStripper.stripValues;

/**
 * This ResponseSkeletonFitness uses the stripValues function.
 */
public class ResponseSkeletonFitness extends Fitness {

    private Map<String, Integer> structureFrequencyTable;
    // Count the number of generations
    private int generationCount;

    public ResponseSkeletonFitness() {
        super();
        this.structureFrequencyTable = new HashMap<>();
        this.generationCount = 0;
    }

    @Override
    public void evaluate(Generator generator, List<Individual> population) {
        // Fill in hashmap with structure frequency
        for (Individual individual : population) {

            String structureString = stripValues(individual.toTotalJSONObject(), individual.getResponseObject().getResponseObject()).toString();

            if (!structureFrequencyTable.containsKey(structureString)) {
                structureFrequencyTable.put(structureString, 0);
            }
            structureFrequencyTable.put(structureString, structureFrequencyTable.get(structureString) + 1);

            // Evaluate individual compared to the map
            // Fitness is between 0 and 1.
            double fitness = (double) 1 / structureFrequencyTable.get(stripValues(individual.toTotalJSONObject(),
                    individual.getResponseObject().getResponseObject()).toString());
            individual.setFitness(fitness);

            // decide whether to add individual to the archive
            getCollector().addToArchive(individual.getResponseObject().getResponseObject().toString(), individual);
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
    public ArrayList<String>  storeInformation() {
        ArrayList<String> info = new ArrayList<>();
        info.add("Map: " + structureFrequencyTable.keySet().size());
        info.add(structureFrequencyTable.toString());
        return info;
    }

}
