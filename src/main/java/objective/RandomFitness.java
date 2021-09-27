package objective;

import org.json.JSONObject;
import search.Generator;
import search.Individual;
import util.Configuration;
import java.util.ArrayList;
import java.util.List;
import static util.RandomSingleton.getRandomBool;
import static statistics.Collector.getCollector;

/**
 * RandomFitness creates random fitness values for individuals (based on Gaussian distribution).
 */
public class RandomFitness extends Fitness {

    public RandomFitness() {
        super();
    }

    @Override
    public void evaluate(Generator generator, List<Individual> population) {
        // Call methods
        for (int i = 0; i < population.size(); i++) {
            Individual individual = population.get(i);
            JSONObject request = individual.toTotalJSONObject();
            JSONObject response = individual.getResponseObject().getResponseObject();

            JSONObject stripped = stripValues(request, response);

            // decide whether to add individual to the archive
            if (getRandomBool(1 - Configuration.ARCHIVE_THRESHOLD_RANDOM)) {
                getCollector().addToArchive(stripped.toString(), individual);
            }
        }
    }

    @Override
    public ArrayList<String> storeInformation() {
        return new ArrayList<>();
    }

}
