package util;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public final class RandomSingleton {

    private static Random random;

    public static Random getRandom() {
        if (random == null) {
            random = new Random();
        }

        return random;
    }

    /**
     * Retrieve a random index from a list.
     * @param list
     * @return index
     */
    public static int getRandomIndex(Collection list) {
        return getRandom().nextInt(list.size());
    }

    /**
     * Retrieve a boolean value based on a probability.
     * @param probability
     * @return boolean value
     */
    public static boolean getRandomBool(double probability) {
        return getRandom().nextDouble() < probability;
    }

    /**
     * Retrieve a random probability out of a list of probabilities.
     * @param probs
     * @return index
     */
    public static int getChoiceWithChance(double[] probs) {
        double choice = getRandom().nextDouble();
        double sum = 0;
        for (int i = 0; i < probs.length; i++) {
            sum += probs[i];
            if (choice > sum) {
                return i;
            }
        }
        return probs.length - 1;
    }

    public static void setSeed(long seed) {
        if (random != null) {
            throw new IllegalStateException("Set the seed before using the random singleton!");
        }
        random = new Random(seed);
    }
}
