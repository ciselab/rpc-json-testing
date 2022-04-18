package util;

import java.util.*;

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
     * Retrieve n unique random indices
     * @param list the list to take indices for
     * @param n the number of indices
     * @return index
     */
    public static Set<Integer> getRandomUniqueIndices(Collection list, int n) {
        Set<Integer> options = new HashSet<>();

        if (list.size() == 0) {
            return options;
        }

        for (int i = 0; i < list.size(); i++) {
            options.add(i);
        }

        if (list.size() <= n) {
            return options;
        }

        Set<Integer> indices = new HashSet<>();

        for (int i = 0; i < n; i++) {
            int index = util.RandomSingleton.getRandom().nextInt(options.size());

            Integer choice = (Integer) options.toArray()[index];

            options.remove(choice);

            indices.add(choice);
        }

        return indices;
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
