package search.genes;

import org.json.JSONObject;
import search.Generator;
import search.openRPC.SchemaSpecification;
import search.openRPC.Specification;

import static util.RandomSingleton.getRandom;

public class LongGene extends ValueGene<Long> {
    public LongGene(String key, Long value) {
        super(key, value);
    }

    @Override
    public Gene mutate(Generator generator) {
        if (getRandom().nextDouble() < 0.95) {

            // Get minimum and maximum value of the parameter range
            SchemaSpecification schema = generator.getSchema(getSpecPath(), "integer");

            Long minimum = schema.getMin();
            Long maximum = schema.getMax();

            long newValue;
            if (getRandom().nextBoolean()) {
                // left
                long diff = getValue() - minimum;
                long mean = getValue();
                long std = diff / 10;// fit the entire diff in 10 std
//                System.out.println("STD " + std);
                newValue = (long) Math.abs(mean - (getRandom().nextGaussian() * std));
            } else {
                // right
                long diff = maximum - getValue();
                long mean = getValue();
                long std = diff / 10;// fit the entire diff in 10 std
//                System.out.println("STD " + std);
                newValue = (long) Math.abs((getRandom().nextGaussian() * std + mean));
            }


//            System.out.println("max " + maximum);
//            System.out.println("min " + minimum);
            newValue = Math.max(minimum, newValue);
            newValue = Math.min(maximum, newValue);

//            double random = getRandom().nextDouble();
//
//            double difference = 0.25 * (maximum - minimum);
//            double newValue = 0;
//            if (getRandom().nextDouble() >= 0.5) {
//                newValue = this.getValue() + random * difference;
//                getRandom().nextGaussian() * std + mean
//            } else {
//                newValue = this.getValue() - random * difference;
//            }
//
//            long mutatedValue = Math.round(newValue);
//            if (mutatedValue < minimum) {
//                mutatedValue = minimum;
//            } else if (mutatedValue > maximum) {
//                mutatedValue = maximum;
//            }
            System.out.println("longGene changed from " + this.getValue() + " to " + newValue);

            return new LongGene(this.getSpecPath(), newValue);
        } else {
            // change gene type (e.g. string instead of long, or random long within specification)
            return getNewGene(generator);
        }
    }

    public Gene oldMutate(Specification specification) {
        if (getRandom().nextDouble() < 0.99) {

            // Get minimum and maximum value of the parameter range
            JSONObject typeSpecification = specification.getObject();
            Long minimum = typeSpecification.has("minimum") ? typeSpecification.getLong("minimum") : 0L;
            Long maximum = typeSpecification.has("maximum") ? typeSpecification.getLong("maximum") : Long.MAX_VALUE;

//            System.out.println("max: " + maximum);
//            System.out.println("min: " + minimum);

            // TODO: increase max, decrease min to be able to go outside the boundaries

            // The following is based on Deb et al. Gaussian mutation in RGAs

            double mutationStrength = 1.0/10.0; // TODO: figure out what mutationStrength should be/what range
            double std = mutationStrength / (maximum - minimum);

            System.out.println((maximum - minimum) * std);
            double uL = 0.5 * (erf((minimum - this.getValue()) / (Math.sqrt(2) * (maximum - minimum) * std)) + 1);
            double uR = 0.5 * (erf((maximum - this.getValue()) / (Math.sqrt(2) * (maximum - minimum) * std)) + 1);
//            System.out.println(uL);
//            System.out.println(uR);

            // Step 1: Create a random number in range [0,1]
            double mu = 0;
            double randomNumber = getRandom().nextDouble();
            if (randomNumber <= 0.5) {
                mu = 2 * uL * (1 - 2 * randomNumber);
            } else {
                mu = 2 * uR * (2 * randomNumber - 1);
            }

//            System.out.println("mu " + mu);
//            System.out.println(erfInv(mu));

            // Step 2: Use formula to create offspring from individual
            double mutatedValue = this.getValue() + Math.sqrt(2) * std * (maximum - minimum) * erfInv(mu);

            long newValue = Math.round(mutatedValue);

            System.out.println("old long: " + this.getValue() + " and new long: " + newValue);

            return new LongGene(getSpecPath(), newValue);
        }
        else {
            System.out.println("Mutation info: nothing changed, long");
            // TODO: change value entirely
            return this;
        }
    }

    // From https://introcs.cs.princeton.edu/java/21function/ErrorFunction.java.html
    // fractional error in math formula less than 1.2 * 10 ^ -7.
    // although subject to catastrophic cancellation when z in very close to 0
    // from Chebyshev fitting formula for erf(z) from Numerical Recipes, 6.2
    public double erf(double z) {
        double t = 1.0 / (1.0 + 0.5 * Math.abs(z));

        // use Horner's method
        double ans = 1 - t * Math.exp( -z*z   -   1.26551223 +
            t * ( 1.00002368 +
                t * ( 0.37409196 +
                    t * ( 0.09678418 +
                        t * (-0.18628806 +
                            t * ( 0.27886807 +
                                t * (-1.13520398 +
                                    t * ( 1.48851587 +
                                        t * (-0.82215223 +
                                            t * ( 0.17087277))))))))));
        if (z >= 0) return  ans;
        else        return -ans;
    }

    public double myErfInv(double x) {
        double sig = Math.signum(x);
        double alpha = (8*(Math.PI-3)) / (3*Math.PI * (4 - Math.PI));
        double a = 2/(Math.PI*alpha) + Math.log(1-Math.pow(x, 2))/2;
        double b = Math.log(1-Math.pow(x, 2))/alpha;
        double c = 2 / (Math.PI * alpha);
        double d = Math.log(1-Math.pow(x, 2))/2;
        double formula = Math.sqrt( Math.sqrt(Math.pow(a, 2) - b) - c + d);
        double answer = sig*formula;
        return answer;
    }

    // From https://alvinalexander.com/java/jwarehouse/commons-math3-3.6.1/src/main/java/org/apache/commons/math3/special/Erf.java.shtml
    /**
     * Returns the inverse erf.
     * <p>
     * This implementation is described in the paper:
     * <a href="http://people.maths.ox.ac.uk/gilesm/files/gems_erfinv.pdf">Approximating
     * the erfinv function</a> by Mike Giles, Oxford-Man Institute of Quantitative Finance,
     * which was published in GPU Computing Gems, volume 2, 2010.
     * The source code is available <a href="http://gpucomputing.net/?q=node/1828">here.
     * </p>
     * @param x the value
     * @return t such that x = erf(t)
     * @since 3.2
     */
    public double erfInv(final double x) {

        // beware that the logarithm argument must be
        // computed as (1.0 - x) * (1.0 + x),
        // it must NOT be simplified as 1.0 - x * x as this
        // would induce rounding errors near the boundaries +/-1
        double w = - Math.log((1.0 - x) * (1.0 + x));
        double p;

        if (w < 6.25) {
            w -= 3.125;
            p =  -3.6444120640178196996e-21;
            p =   -1.685059138182016589e-19 + p * w;
            p =   1.2858480715256400167e-18 + p * w;
            p =    1.115787767802518096e-17 + p * w;
            p =   -1.333171662854620906e-16 + p * w;
            p =   2.0972767875968561637e-17 + p * w;
            p =   6.6376381343583238325e-15 + p * w;
            p =  -4.0545662729752068639e-14 + p * w;
            p =  -8.1519341976054721522e-14 + p * w;
            p =   2.6335093153082322977e-12 + p * w;
            p =  -1.2975133253453532498e-11 + p * w;
            p =  -5.4154120542946279317e-11 + p * w;
            p =    1.051212273321532285e-09 + p * w;
            p =  -4.1126339803469836976e-09 + p * w;
            p =  -2.9070369957882005086e-08 + p * w;
            p =   4.2347877827932403518e-07 + p * w;
            p =  -1.3654692000834678645e-06 + p * w;
            p =  -1.3882523362786468719e-05 + p * w;
            p =    0.0001867342080340571352 + p * w;
            p =  -0.00074070253416626697512 + p * w;
            p =   -0.0060336708714301490533 + p * w;
            p =      0.24015818242558961693 + p * w;
            p =       1.6536545626831027356 + p * w;
        } else if (w < 16.0) {
            w = Math.sqrt(w) - 3.25;
            p =   2.2137376921775787049e-09;
            p =   9.0756561938885390979e-08 + p * w;
            p =  -2.7517406297064545428e-07 + p * w;
            p =   1.8239629214389227755e-08 + p * w;
            p =   1.5027403968909827627e-06 + p * w;
            p =   -4.013867526981545969e-06 + p * w;
            p =   2.9234449089955446044e-06 + p * w;
            p =   1.2475304481671778723e-05 + p * w;
            p =  -4.7318229009055733981e-05 + p * w;
            p =   6.8284851459573175448e-05 + p * w;
            p =   2.4031110387097893999e-05 + p * w;
            p =   -0.0003550375203628474796 + p * w;
            p =   0.00095328937973738049703 + p * w;
            p =   -0.0016882755560235047313 + p * w;
            p =    0.0024914420961078508066 + p * w;
            p =   -0.0037512085075692412107 + p * w;
            p =     0.005370914553590063617 + p * w;
            p =       1.0052589676941592334 + p * w;
            p =       3.0838856104922207635 + p * w;
        } else if (!Double.isInfinite(w)) {
            w = Math.sqrt(w) - 5.0;
            p =  -2.7109920616438573243e-11;
            p =  -2.5556418169965252055e-10 + p * w;
            p =   1.5076572693500548083e-09 + p * w;
            p =  -3.7894654401267369937e-09 + p * w;
            p =   7.6157012080783393804e-09 + p * w;
            p =  -1.4960026627149240478e-08 + p * w;
            p =   2.9147953450901080826e-08 + p * w;
            p =  -6.7711997758452339498e-08 + p * w;
            p =   2.2900482228026654717e-07 + p * w;
            p =  -9.9298272942317002539e-07 + p * w;
            p =   4.5260625972231537039e-06 + p * w;
            p =  -1.9681778105531670567e-05 + p * w;
            p =   7.5995277030017761139e-05 + p * w;
            p =  -0.00021503011930044477347 + p * w;
            p =  -0.00013871931833623122026 + p * w;
            p =       1.0103004648645343977 + p * w;
            p =       4.8499064014085844221 + p * w;
        } else {
            // this branch does not appears in the original code, it
            // was added because the previous branch does not handle
            // x = +/-1 correctly. In this case, w is positive infinity
            // and as the first coefficient (-2.71e-11) is negative.
            // Once the first multiplication is done, p becomes negative
            // infinity and remains so throughout the polynomial evaluation.
            // So the branch above incorrectly returns negative infinity
            // instead of the correct positive infinity.
            p = Double.POSITIVE_INFINITY;
        }
        return p * x;
    }

    @Override
    public Gene<Long> copy() {
        return new LongGene(getSpecPath(), this.getValue());
    }
}
