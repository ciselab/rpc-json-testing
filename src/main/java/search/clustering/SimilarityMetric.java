package search.clustering;

import java.util.List;

public class SimilarityMetric {

    final private double THRESHOLD_DIFFERENT_FEATURES = 0.5;

    /**
     * Gives the average Euclidean similarity of the clusters
     * @param a cluster a
     * @param b cluster b
     * @return the average Euclidean similarity
     */
    public double calculateSimilarity(List<List<Object>> a, List<List<Object>> b, List<Integer> weightVector) {
        Double distance = 0.0;
        for (List<Object> featureVectorA : a) {
            for (List<Object> featureVectorB : b) {
                distance += calculateFeatureVectorDistance(featureVectorA, featureVectorB, weightVector);
            }
        }

        distance /= (a.size() * b.size());

        return 1.0 / (1.0 + distance);
    }

    /**
     * Gives the Euclidean similarity of two feature vectors
     * @param a cluster a
     * @param b cluster b
     * @return the average Euclidean similarity
     */
    public double calculateSimilaritySingle(List<Object> a, List<Object> b, List<Integer> weightVector) {
        Double distance = calculateFeatureVectorDistance(a, b, weightVector);

        return 1.0 / (1.0 + distance);
    }

    /**
     * Calculates euclidean distance of 2 feature vectors
     * @param a
     * @param b
     * @return
     */
    private double calculateFeatureVectorDistance(List<Object> a, List<Object> b, List<Integer> weightVector) {
        double distance = 0;
        int differentFeatures = 0;

        for (int i = 0; i < a.size(); i++) {
            Object objectA = a.get(i);
            Object objectB = b.get(i);
            if (!objectA.getClass().equals(objectB.getClass()) && !(objectA instanceof Number && objectB instanceof Number)) {
                throw new IllegalArgumentException("Comparing different classes is not possible\n" + a + "\n" + b);
            }

            if (objectA instanceof String) {
                double maxStringDistance = 20;
                double tempDistance = Math.pow(Math.min(stringDistance((String) objectA, (String) objectB), maxStringDistance), 2) * (1.0 / (double) weightVector.get(i));
                distance += tempDistance;
                if (tempDistance != 0) {
                    differentFeatures += 1;
                }

            } else if (objectA instanceof Boolean) {
                double tempDistance = Math.pow(boolDistance((Boolean) objectA, (Boolean) objectB), 2) * (1.0 / (double) weightVector.get(i));
                distance += tempDistance;
                if (tempDistance != 0) {
                    differentFeatures += 1;
                }
            } else if (objectA instanceof Number) {
                double tempDistance = Math.pow(numberDistance(((Number) objectA).doubleValue(), ((Number) objectB).doubleValue()), 2) * (1.0 / (double) weightVector.get(i));
                distance += tempDistance;
                if (tempDistance != 0) {
                    differentFeatures += 1;
                }
            }
        }
        // If half of the features in the vector are not different, two vectors are not different enough and distance should be 0.
        if (differentFeatures / a.size() < THRESHOLD_DIFFERENT_FEATURES) {
            return 0;
        }
        return Math.sqrt(distance);
    }

    public static double boolDistance(Boolean a, Boolean b) {
        return a == b ? 0 : 1;
    }

    public static double numberDistance(Double a, Double b) {
        return Math.abs(a - b);
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

//        for (int i = 0; i < distance.length; i++) {
//            System.out.println(Arrays.toString(distance[i]));
//        }
        double dist = distance[a.length()][b.length()];

        return dist;
    }

}

