package util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static util.RandomSingleton.getRandomUniqueIndices;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class RandomSingletonTest {

    @Test
    public void getRandomUniqueIndicesTest() {

        // 1 - When the list is empty, an empty set should be returned (no mutations can be done).
        List<String> requests = new ArrayList<>(); // For the purpose of this test it does not matter what the contents of the list are
        int mutations = 2;
        assertEquals(new HashSet<>(), getRandomUniqueIndices(requests, mutations));

        // 2 - When the list is smaller than or equal to the mutations that need to be done, all indices of the list are returned.
        String request1 = "request1";
        requests.add(request1);
        Set<Integer> options = new HashSet<>();
        options.add(0);
        assertEquals(options, getRandomUniqueIndices(requests, mutations));

        // 3 - When the list is larger than the number of mutations, unique indices of the list equal to the number of mutations are returned.
        String request2 = "request2";
        String request3 = "request3";
        requests.add(request2);
        requests.add(request3);
        options.add(1);
        options.add(2);

        Set<Integer> indices = getRandomUniqueIndices(requests, mutations);

        assertNotEquals(new HashSet<>(), indices);
        assertNotEquals(options, indices);
        assertEquals(mutations, indices.size());

        // Indices in the list must be unique
        if (indices.contains(0)) {
            assertNotEquals(indices.contains(1), indices.contains(2));
        } else {
            assertEquals(indices.contains(1), indices.contains(2));
            assertNotEquals(indices.contains(0), indices.contains(1));
        }
    }
}
