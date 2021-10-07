package search.clustering;

import org.junit.jupiter.api.Test;

class SimilarityMetricTest {

    @Test
    void sameBoolDistance() {
        assert SimilarityMetric.boolDistance(true, true) == 0.0;
        assert SimilarityMetric.boolDistance(false, false) == 0.0;
    }

    @Test
    void sameNumberDistance() {
        assert SimilarityMetric.numberDistance(0.0, 0.0) == 0.0;
        assert SimilarityMetric.numberDistance(1.0, 1.0) == 0.0;
    }

    @Test
    void sameStringDistance() {
        assert SimilarityMetric.stringDistance("true", "true") == 0.0;
        assert SimilarityMetric.stringDistance("false", "false") == 0.0;
    }


    @Test
    void notSameBoolDistance() {
        assert SimilarityMetric.boolDistance(true, false) == 1.0;
        assert SimilarityMetric.boolDistance(false, true) == 1.0;
    }

    @Test
    void notSameNumberDistance() {
        for (double i = 0; i < 10; i++) {
            for (double j = 0; j < 10; j++) {
                assert SimilarityMetric.numberDistance(i, j) == Math.abs(i - j);
            }
        }

    }

    @Test
    void notSameStringDistance() {
        System.out.println(SimilarityMetric.stringDistance("true", "truu"));
        System.out.println(SimilarityMetric.stringDistance("false", "falseu"));
        System.out.println(SimilarityMetric.stringDistance("falseu", "false"));

        assert SimilarityMetric.stringDistance("true", "truu") == 1.0;
        assert SimilarityMetric.stringDistance("false", "falseu") == 2.0;
        assert SimilarityMetric.stringDistance("falseu", "false") == 1.0; // weird

    }
}