package search.genes.primitive;

import search.Generator;
import openRPC.SchemaSpecification;
import search.genes.Gene;
import util.config.Configuration;

import static util.RandomSingleton.getRandom;
import static util.RandomSingleton.getRandomBool;

public class LongGene extends ValueGene<Long> {

    public LongGene(SchemaSpecification schema, Long value) {
        super(schema, value);
    }

    /**
     * Mutate the Long value using polynomial mutation (based on EvoMaster implementation).
     * @param generator
     * @return
     */
    @Override
    public Gene mutate(Generator generator) {

        if (getRandomBool(Configuration.MUTATION_INSTEAD_OF_GENERATION)) {

            // Get minimum and maximum value of the parameter range
            SchemaSpecification schema = getSchema();
            Long lb = schema.getMin();
            Long ub = schema.getMax();

            // With some probability use the boundary cases for the value
            if (getRandomBool(Configuration.BOUNDARY_CASE_PROB)) {
                if (getRandom().nextDouble() < 0.5) {
                    return new LongGene(this.getSchema(), (long) lb);
                } else {
                    return new LongGene(this.getSchema(), (long) ub);
                }
            }

            Long distanceMinMax = ub - lb;

            // Double check that this is always above 0 (?)
            double newValue = getValue();

            double delta1 = (newValue - lb) / distanceMinMax;
            double delta2 = (ub - newValue) / distanceMinMax;
            double deltaq;

            double distributionIndex = 10.0;
            double pow = 1.0 / (distributionIndex + 1.0);

            double r = getRandom().nextDouble();
            if (r < 0.5) {
                double aux = 2.0 * r + (1.0 - 2.0 * r) * (Math.pow(1.0 - delta1, (distributionIndex + 1.0)));
                deltaq = Math.pow(aux, pow) - 1.0;
            }
            else {
                double aux = 2.0 * (1.0 - r) + 2.0 * (r - 0.5) * (Math.pow(1.0 - delta2, (distributionIndex + 1.0)));
                deltaq = 1.0 - Math.pow(aux, pow);
            }

            newValue = Math.round(newValue + deltaq * distanceMinMax);

            // This part does not allow for the creation of edge cases
            if (Configuration.NO_OUTSIDE_BOUNDARY_CASES) {
                if (newValue < lb)
                    newValue = lb;
                else if (newValue > ub)
                    newValue = ub;
            }

            return new LongGene(this.getSchema(), (long) newValue);

        } else {
            // Change gene into an entirely new value by generating new value
            return getNewGene(generator);
        }
    }

    @Override
    public Gene<Long> copy() {
        return new LongGene(getSchema(), this.getValue());
    }
}
