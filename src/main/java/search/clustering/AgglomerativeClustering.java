package search.clustering;

import java.util.ArrayList;
import java.util.List;

/**
 * Agglomerative clustering for every new individual. Stores all clusters.
 * Used in ResponseFitnessClustering2.
 */
public class AgglomerativeClustering {

    private SimilarityMetric metric;

    // Each value in the feature vector has a certain weight/importance
    private List<Integer> weightVector;

    // List of all the clusters (each cluster in the list has one or more items in its cluster (items are featureVector (of different types)).
    // clusters, cluster, featureVector (of different types)
    private List<List<List<Object>>> clusters;
    private List<List<Double>> similarityMatrix;

    private List<List<Object>> values;

    public AgglomerativeClustering(List<Integer> weightVector) {
        this.metric = new SimilarityMetric();
        this.values = new ArrayList<>();
        this.clusters = new ArrayList<>();
        this.similarityMatrix = new ArrayList<>();
        this.weightVector = weightVector;
    }

    /**
     * Cluster individual.
     * Computes the cost of the newValue.
     * @param newValue feature vector of an individual
     * @return minimum distance to any cluster of an individual
     */
    public double cluster(List<Object> newValue) {
        int oldNumberOfClusters = clusters.size();
        List<List<List<Object>>> clusters = new ArrayList<>();
        List<List<List<List<Object>>>> inBetweenClusters = new ArrayList<>();

        for (List<Object> old : values) {
            List<List<Object>> c1 = new ArrayList<>();
            List<List<Object>> c2 = new ArrayList<>();

            c1.add(old);
            c2.add(newValue);

            // if the value is already found skip it
            if (metric.calculateSimilarity(c1, c2, weightVector) == 1.0) {
                return 1000000;
            }
        }

        this.values.add(newValue);

        // Create a new cluster for each feature vector
        for (List<Object> datapoint : values) {
            List<List<Object>> cluster = new ArrayList<>();
            cluster.add(datapoint);
            clusters.add(cluster);
        }

        similarityMatrix = new ArrayList<>();

        for (int i = 0; i < clusters.size(); i++) {
            similarityMatrix.add(new ArrayList<>());
            for (int j = i + 1; j < clusters.size(); j++) {
                double similarity = this.metric.calculateSimilarity(clusters.get(i), clusters.get(j), weightVector);
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
                double similarity = this.metric.calculateSimilarity(cluster1, clusters.get(i), weightVector);
                similarityMatrix.get(i).add(similarity);
            }

            similarityMatrix.add(new ArrayList<>());
        }

        double maxJump = 0;
        int maxJumpIndex = 0;

        List<Double> jumps = new ArrayList<>();

        for (int i = 0; i < similarityJumps.size() - 1; i++) {
            double dissimilarityCurrent = (1.0 / similarityJumps.get(i)) - 1.0;
            double dissimilarityNext = (1.0 / similarityJumps.get(i + 1)) - 1.0;

            double jump = dissimilarityNext - dissimilarityCurrent;
            if (maxJump <= jump) {
                maxJump = jump;
                maxJumpIndex = i + 1; ///after the jump there is new cluster
            }

            jumps.add(jump);
        }

        this.clusters = inBetweenClusters.get(maxJumpIndex);

        // check if number of clusters stayed the same (i.e. no new input vector)
        if (this.clusters.size() == oldNumberOfClusters) {
            return 100000; // no new cluster (individual is not different enough)
        }

        // check if a cluster was removed
        if (this.clusters.size() < oldNumberOfClusters) {
            return 0;
        }

        // a cluster was added!
        int clusterOfNewValue = -1;

        // retrieve cluster index of newValue
        for (int i = 0; i < clusters.size(); i++) {
            if (clusters.get(i).contains(newValue)) {
                clusterOfNewValue = i;
            }
        }

        // in the case the value is nowhere to be found while it should be in a cluster
        if (clusterOfNewValue == -1) {
            throw new RuntimeException("Should not be possible");
        }

        double maxSimilarity = 0;

        // find most similar other cluster
        for (int j = 0; j < clusters.size(); j++) {
            if (clusterOfNewValue == j) {
                continue;
            }

            double similarity = similarityMatrix.get(clusterOfNewValue).get(j - (clusterOfNewValue + 1));

            if (similarity >= maxSimilarity) {
                maxSimilarity = similarity;
            }
        }

        return maxSimilarity;
    }

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

    public List<List<List<Object>>> getClusters() {
        return clusters;
    }
}
