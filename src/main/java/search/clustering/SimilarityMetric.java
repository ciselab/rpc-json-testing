package search.clustering;

import util.Configuration;

import java.util.List;

public class SimilarityMetric {

    /**
     * Gives the average Euclidean similarity of the clusters
     * @param a cluster a
     * @param b cluster b
     * @return the average Euclidean similarity
     */
    public double calculateSimilarity(List<List<Object>> a, List<List<Object>> b, List<Integer> depthVector) {
        Double distance = calculateFeatureVectorDistance(a, b, depthVector);

        return 1.0 / (1.0 + distance);
    }

    /**
     * Gives the Euclidean similarity of two feature vectors
     * @param a cluster a
     * @param b cluster b
     * @return the average Euclidean similarity
     */
    public double calculateSimilaritySingle(List<Object> a, List<Object> b, List<Integer> depthVector) {
        Double distance = calculateFeatureVectorDistanceSingle(a, b, depthVector);

        return 1.0 / (1.0 + distance);
    }

    public double calculateFeatureVectorDistance(List<List<Object>> a, List<List<Object>> b, List<Integer> depthVector) {
        Double distance = 0.0;
        for (List<Object> featureVectorA : a) {
            for (List<Object> featureVectorB : b) {
                distance += calculateFeatureVectorDistanceSingle(featureVectorA, featureVectorB, depthVector);
            }
        }

        distance /= (a.size() * b.size());
        return distance;
    }

    /**
     * Calculates euclidean distance of 2 feature vectors
     * @param a
     * @param b
     * @return
     */
    public double calculateFeatureVectorDistanceSingle(List<Object> a, List<Object> b, List<Integer> depthVector) {
        double distance = 0;

        for (int i = 0; i < a.size(); i++) {
            Object objectA = a.get(i);
            Object objectB = b.get(i);
            if (!objectA.getClass().equals(objectB.getClass()) && !(objectA instanceof Number && objectB instanceof Number)) {
                throw new IllegalArgumentException("Comparing different classes is not possible\n" + a + "\n" + b);
            }

            double scalar = (1.0 / (double) depthVector.get(i));

            if (objectA instanceof String) {
                distance += Math.pow(stringDistance((String) objectA, (String) objectB), 2) * scalar;
            } else if (objectA instanceof Boolean) {
                distance += Math.pow(boolDistance((Boolean) objectA, (Boolean) objectB), 2) * scalar;
            } else if (objectA instanceof Number) {
                distance += Math.pow(numberDistance(((Number) objectA).doubleValue(), ((Number) objectB).doubleValue()), 2) * scalar;
            }
        }

        return Math.sqrt(distance);
    }

    public static double boolDistance(Boolean a, Boolean b) {
        return a == b ? 0 : 1;
    }

    public static double numberDistance(Double a, Double b) {
        return Math.abs(a - b) / Math.max(1, Math.max(Math.abs(a), Math.abs(b)));
    }

    /**
     * Levenshtein distance.
     * The minimum number of single-character edits (insertions, deletions or substitutions) required to change one word into the other.
     * @param a
     * @param b
     * @return
     */
    public static double stringDistance(String a, String b) {
        // TODO seems to be incorrect somehow
        double[][] distance = new double[a.length() + 1][b.length() + 1];

        for (int i = 1; i <= a.length(); i++) {
            distance[i][0] = i;
        }

        for (int j = 1; j <= b.length(); j++) {
            distance[0][j] = j;
        }

        for (int j = 1; j <= b.length(); j++) {
            for (int i = 1; i <= a.length(); i++) {
                int cost = 0;
                if (a.charAt(i-1) != b.charAt(j-1)) {
                    cost = 1;
                }

                distance[i][j] = Math.min(distance[i - 1][j] + 1,
                                    Math.min(distance[i][j - 1] + 1,
                                        distance[i - 1][j - 1]) + cost);
            }
        }

        double dist = distance[a.length()][b.length()];

        return dist / Math.max(1, Math.max(a.length(), b.length()));
    }

}

