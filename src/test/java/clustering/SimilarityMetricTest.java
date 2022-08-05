package clustering;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static search.clustering.SimilarityMetric.stringDistance;

class SimilarityMetricTest {

    @Test
    public void stringDistanceTest() {
        double distance = stringDistance("actNotFound", "lgrNotFound");
        assertEquals(3, distance);
    }

    @Test
    public void stringDistanceTest2() {
        double distance = stringDistance("testing2", "test");
        assertEquals(4, distance);
    }

    @Test
    public void stringDistanceTest3() {
        double distance = stringDistance("test", "testing2");
        assertEquals(4, distance);
    }


}