package search.genes;

import search.Generator;
import search.openRPC.SchemaSpecification;

import static util.RandomSingleton.getRandom;

public class LongGene extends ValueGene<Long> {
    public LongGene(SchemaSpecification schema, Long value) {
        super(schema, value);
    }

    @Override
    public Gene mutate(Generator generator) {
        // Polynomial mutation, based on EvoMaster implementation
        if (getRandom().nextDouble() < 0.95) {

            // Get minimum and maximum value of the parameter range
            SchemaSpecification schema = getSchema();

            Long lb = schema.getMin();
            Long ub = schema.getMax();
            Long distanceMinMax = ub - lb;

            //double check that this is always above 0
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
            // (below the minimum and above the maximum)
//            if (newValue < lb)
//                newValue = lb;
//            else if (newValue > ub)
//                newValue = ub;

            return new LongGene(this.getSchema(), (long) newValue);

        } else {
            // change gene type (e.g. string instead of long, or random long within specification)
            return getNewGene(generator);
        }
    }


//    @Override
//    public Gene mutate(Generator generator) {
//        if (getRandom().nextDouble() < 0.95) {
//
//            // Get minimum and maximum value of the parameter range
//            SchemaSpecification schema = getSchema();
//
//            Long minimum = schema.getMin();
//            Long maximum = schema.getMax();
//
//            long newValue;
//            if (getRandom().nextBoolean()) {
//                // left
//                long diff = getValue() - minimum;
//                long mean = getValue();
//                long std = diff / 10;// fit the entire diff in 10 std
//
//                newValue = (long) Math.abs(mean - (getRandom().nextGaussian() * std));
//            } else {
//                // right
//                long diff = maximum - getValue();
//                long mean = getValue();
//                long std = diff / 10;// fit the entire diff in 10 std
//
//                newValue = (long) Math.abs((getRandom().nextGaussian() * std + mean));
//            }
//
//            newValue = Math.max(minimum, newValue);
//            newValue = Math.min(maximum, newValue);
//
//            return new LongGene(this.getSchema(), newValue);
//        } else {
//            // change gene type (e.g. string instead of long, or random long within specification)
//            return getNewGene(generator);
//        }
//    }

    @Override
    public Gene<Long> copy() {
        return new LongGene(getSchema(), this.getValue());
    }
}
