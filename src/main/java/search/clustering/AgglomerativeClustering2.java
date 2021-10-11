package search.clustering;

import java.util.ArrayList;
import java.util.List;

public class AgglomerativeClustering2 {

    private SimilarityMetric metric;

    // Each value in the feature vector has a certain weight/importance
    private List<Integer> depthVector;

    // List of all the clusters (each cluster in the list has one or more items in its cluster (items are featureVector (of different types)).
    private List<List<List<Object>>> clusters;
    private List<List<Double>> similarityMatrix;

    public AgglomerativeClustering2(List<Integer> depthVector) {
        this.metric = new SimilarityMetric();
        this.clusters = new ArrayList<>();
        this.similarityMatrix = new ArrayList<>();
        this.depthVector = depthVector;
    }

    /**
     * Clusters individuals.
     * @param values feature vectors of the current population
     */
    public void cluster(List<List<Object>> values) {
        List<List<List<Object>>> clusters = new ArrayList<>();
        List<List<List<List<Object>>>> inBetweenClusters = new ArrayList<>();

        // Create a cluster for each featore vector
        for (List<Object> datapoint : values) {
            List<List<Object>> cluster = new ArrayList<>();
            cluster.add(datapoint);
            clusters.add(cluster);
        }

        similarityMatrix = new ArrayList<>();

        for (int i = 0; i < clusters.size(); i++) {
            similarityMatrix.add(new ArrayList<>());
            for (int j = i + 1; j < clusters.size(); j++) {
                double similarity = this.metric.calculateSimilarity(clusters.get(i), clusters.get(j), depthVector);
                similarityMatrix.get(i).add(similarity);
            }
        }

        List<Double> similarityJumps = new ArrayList<>();

        while(true) {
            inBetweenClusters.add(deepCopy(clusters));
            int index1 = -1;
            int index2 = -1;
            double maxSimilarity = 0;

            for (int i = 0; i < clusters.size(); i++) {
                for (int j = i + 1; j < clusters.size(); j++) {
                    double similarity = similarityMatrix.get(i).get(j - (i + 1));

                    if (similarity >= maxSimilarity) {
                        maxSimilarity = similarity;
                        index1 = i;
                        index2 = j;
                    }
                }
            }

            if (index1 == -1) {
                // nothing to merge
                break;
            }

            similarityJumps.add(maxSimilarity);

            assert index2 > index1;

            // remove similarity columns
            for (int i = 0; i < clusters.size(); i++) {
                // handle symmetry matrix
                int index2ToRemove = index2 - (i + 1);
                int index1ToRemove = index1 - (i + 1);

                if (index2ToRemove >= 0) {
                    similarityMatrix.get(i).remove(index2ToRemove);
                }

                if (index1ToRemove >= 0) {
                    similarityMatrix.get(i).remove(index1ToRemove);
                }
            }

            similarityMatrix.remove(index2);
            similarityMatrix.remove(index1);

            // remove clusters
            List<List<Object>> cluster1 = clusters.remove(index2);
            List<List<Object>> cluster2 = clusters.remove(index1);

            // merge cluster at index1 and index2
            cluster1.addAll(cluster2);

            clusters.add(cluster1);

            for (int i = 0; i < clusters.size() - 1; i++) {
                double similarity = this.metric.calculateSimilarity(cluster1, clusters.get(i), depthVector);
                similarityMatrix.get(i).add(similarity);
            }

            similarityMatrix.add(new ArrayList<>());
        }

        double maxJump = 0.0;
        int maxJumpIndex = 0;

        for (int i = 0; i < similarityJumps.size() - 1; i++) {
            double dissimilarityCurrent = (1.0 / similarityJumps.get(i)) - 1.0;
            double dissimilarityNext = (1.0 / similarityJumps.get(i + 1)) - 1.0;

            double jump = dissimilarityNext - dissimilarityCurrent;
            if (maxJump <= jump) {
                maxJump = jump;
                maxJumpIndex = i;
            }
        }

        this.clusters = inBetweenClusters.get(maxJumpIndex);
    }

    /**
     * Create a copy of all the clusters.
     * @param toCopy
     * @return
     */
    private List<List<List<Object>>> deepCopy(List<List<List<Object>>> toCopy) {
        List<List<List<Object>>> copy = new ArrayList<>();

        for (List<List<Object>> inner : toCopy) {
            List<List<Object>> innerCopy = new ArrayList<>();
            for (List<Object> inner2 : inner) {
                List<Object> innerCopy2 = new ArrayList<>(inner2);
                innerCopy.add(innerCopy2);
            }
            copy.add(innerCopy);
        }

        return copy;
    }

    /**
     * Compute the maximum similarity of a new value to any of the clusters.
     * @param newValue
     * @return double maxSimilarity
     */
    public double calculateMaxSimilarity(List<Object> newValue) {
        double maxSimilarity = -1;
        List<List<Object>> newList = new ArrayList<>();
        newList.add(newValue);
        for (int i = 0; i < clusters.size(); i++) {
            double sim = this.metric.calculateSimilarity(clusters.get(i), newList, depthVector);
            if (sim > maxSimilarity) {
                maxSimilarity = sim;
            }
        }
        return maxSimilarity;
    }
    
    public List<List<List<Object>>> getClusters() {
        return clusters;
    }
}
