package util;

import java.util.Collection;
import java.util.Random;

public final class RandomSingleton {

    private static Random random;

    public static Random getRandom() {
        if (random == null) {
            random = new Random();
        }

        return random;
    }

    public static int getRandomIndex(Collection list) {
        return getRandom().nextInt(list.size());
    }

    public static void setSeed(long seed) {
        if (random != null) {
            throw new IllegalStateException("Set the seed before using the random singleton!");
        }
        random = new Random(seed);
    }
}
