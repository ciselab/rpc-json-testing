package search.clustering;

import util.datastructures.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Agglomerative clustering every few generations. Makes use of mean and radius per cluster.
 * Used in DiversityBasedFitness.
 */
public class AgglomerativeClustering3 {

    // Each value in the feature vector has a certain weight/importance.
    private List<Integer> weightVector;

    private SimilarityMetric metric;

    // Matrix of similarity between all clustered individuals.
    private List<List<Double>> similarityMatrix;

    private List<Cluster> clusters;

    // List of feature vectors of individuals that do not belong to existing clusters.
    private List<List<Object>> nonBelongers;

    public AgglomerativeClustering3(List<Integer> weightVector) {
        this.weightVector = weightVector;
        this.metric = new SimilarityMetric();
        this.similarityMatrix = new ArrayList<>();
        this.clusters = new ArrayList<>();
        this.nonBelongers = new ArrayList<>();
    }

    /**
     * Gather all feature vectors (corresponding to individuals) that should be clustered.
     * @return list of all feature vectors to be clustered.
     */
    private List<List<Object>> getValuesToCluster() {
        List<List<Object>> values = new ArrayList<>(nonBelongers);

        // Add the median of every cluster to the list of individuals that did not belong to an existing cluster.
        for (Cluster c : clusters) {
            values.add(c.getRepresentative());
        }
        // TODO we may want to add some other cluster members beside the mean/representative.

        // Reset the list of individuals that did not belong to existing clusters.
        nonBelongers = new ArrayList<>();

        return values;
    }

    /**
     * Cluster feature vectors (corresponding to individuals).
     */
    public void cluster() {
        List<List<Object>> values = getValuesToCluster();

        List<List<List<Object>>> clusters = new ArrayList<>();

        // List of states of clusters (each cycle of agglomerative clustering, clusters are merged, we keep track of each stage)
        List<List<List<List<Object>>>> inBetweenClusters = new ArrayList<>();

        // Create a cluster for each feature vector (corresponding to an individual).
        for (List<Object> datapoint : values) {
            List<List<Object>> cluster = new ArrayList<>();
            cluster.add(datapoint);
            clusters.add(cluster);
        }

        similarityMatrix = new ArrayList<>();

        // Fill in similarity matrix.
        for (int i = 0; i < clusters.size(); i++) {
            similarityMatrix.add(new ArrayList<>());
            for (int j = i + 1; j < clusters.size(); j++) {
                double similarity = this.metric.calculateSimilarity(clusters.get(i), clusters.get(j), weightVector);
                similarityMatrix.get(i).add(similarity);
            }
        }

        List<Double> similarityJumps = new ArrayList<>();

        // Perform agglomerative clustering.
        while(true) {
            inBetweenClusters.add(deepCopy(clusters));
            int index1 = -1;
            int index2 = -1;
            double maxSimilarity = 0;

            // Find two most similar clusters.
            for (int i = 0; i < clusters.size(); i++) {
                for (int j = i + 1; j < clusters.size(); j++) {
                    double similarity = similarityMatrix.get(i).get(j - (i + 1));

                    if (similarity >= maxSimilarity) {
                        maxSimilarity = similarity;
                        index1 = i;
                        index2 = j;
                        System.out.println("similarity" + similarity);
                    }
                }
            }

            similarityJumps.add(maxSimilarity);

            if (index1 == -1) {
                // Nothing (left) to merge
                break;
            }
            assert index2 > index1;

            // Remove similarity columns.
            for (int i = 0; i < clusters.size(); i++) {
                // Handle symmetry matrix.
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

            // Remove clusters (they will be merged).
            List<List<Object>> cluster1 = clusters.remove(index2);
            List<List<Object>> cluster2 = clusters.remove(index1);

            // Merge cluster at index1 and index2.
            cluster1.addAll(cluster2);
            // Add merged clusters to the list of clusters as one.
            clusters.add(cluster1);

            // Update similarity matrix.
            for (int i = 0; i < clusters.size() - 1; i++) {
                double similarity = this.metric.calculateSimilarity(cluster1, clusters.get(i), weightVector);
                similarityMatrix.get(i).add(similarity);
            }

            similarityMatrix.add(new ArrayList<>());
        }

        double maxJump = 0.0;
        int maxJumpIndex = 0;
        boolean allSame = true;

        for (int i = 0; i < similarityJumps.size() - 1; i++) {
            // Check whether clusters are different.
            if (similarityJumps.get(0).equals(similarityJumps.get(i + 1))) {
                allSame = false;
            }

            double dissimilarityCurrent = (1.0 / similarityJumps.get(i)) - 1.0;
            double dissimilarityNext = (1.0 / similarityJumps.get(i + 1)) - 1.0;

            double jump = dissimilarityNext - dissimilarityCurrent;
            if (maxJump <= jump) {
                maxJump = jump;
                maxJumpIndex = i;
            }
        }

        // Pick clustering version where the distance jump is maximal
        // Average linkage (the average distance between each point in one cluster to every point in the other cluster)
        clusters = inBetweenClusters.get(maxJumpIndex);

        if (allSame) {
            clusters = inBetweenClusters.get(inBetweenClusters.size() - 1);
        }


        this.clusters = new ArrayList<>();
        // Create actual clusters with representatives and such
        for (List<List<Object>> cluster : clusters) {
            Cluster c = new Cluster(metric, weightVector, cluster);
            c.findRepresentativeAndRadius();
            this.clusters.add(c);
        }
    }

    /**
     * Create a copy of all the clusters.
     * @param toCopy
     * @return copy of the clusters
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
     * Called for each individual to retrieve similarity.
     * Individual is added to be clustered later if distance is large enough.
     * @param value a feature vector
     * @return similarity
     */
    public double addOne(List<Object> value) {
        for (Cluster cluster : clusters) {
            Pair<Boolean, Double> result = cluster.isWithin(value);
            // Assumes that values cannot belong to two clusters
            if (result.getKey()) {
                return result.getValue();
            }
        }

        // When feature vector did not belong to a cluster, save it to cluster later
        nonBelongers.add(value);

        return 0;
    }

    public List<Cluster> getClusters() {
        return clusters;
    }

}
