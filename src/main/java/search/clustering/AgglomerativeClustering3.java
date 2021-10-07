package search.clustering;

import util.Pair;

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

        // Add the mean of every cluster to the list of individuals that did not belong to an existing cluster.
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

        // List of states of clusters (each cycle of agglomerative clustering clusters change)
        List<List<List<List<Object>>>> inBetweenClusters = new ArrayList<>();

        // Create a cluster for each feature vector (corresponding to an individual).
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

            similarityJumps.add(maxSimilarity);

            if (index1 == -1) {
                // nothing to merge
                break;
            }

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

            // remove rows
            similarityMatrix.remove(index2);
            similarityMatrix.remove(index1);

            // remove clusters
            List<List<Object>> cluster1 = clusters.remove(index2);
            List<List<Object>> cluster2 = clusters.remove(index1);

            // merge cluster at index1 and index2
            cluster1.addAll(cluster2);

            clusters.add(cluster1);

            // calculate similarity column
            for (int i = 0; i < clusters.size() - 1; i++) {
                double similarity = this.metric.calculateSimilarity(cluster1, clusters.get(i), weightVector);
                similarityMatrix.get(i).add(similarity);
            }

            // calculate similarity row
            similarityMatrix.add(new ArrayList<>());
        }

        double maxJump = 0.0;
        int maxJumpIndex = 0;
        boolean allSame = similarityJumps.get(0) == 1.0;

        for (int i = 0; i < similarityJumps.size() - 1; i++) {
            if (similarityJumps.get(i + 1) != 1.0) {
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

        clusters = inBetweenClusters.get(maxJumpIndex);

        if (allSame) {
            if (similarityJumps.size() > 1) {
                System.out.println("ALL SAME");
                System.out.println(similarityJumps);
                System.out.println("n inbetween clusters " + inBetweenClusters.size());
                System.out.println("n similarity jumps " + similarityJumps.size());

                System.out.println(inBetweenClusters.get(inBetweenClusters.size() - 1).size());
                System.out.println();
            }
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
     * Called for each individual to retrieve distance value.
     * Individual is added to be clustered later if distance is large enough.
     * @param value a feature vector
     * @return cost
     */
    public double addOne(List<Object> value) {
        // For number safety return / 2
        Double closestCluster = Double.MAX_VALUE / 2;
        for (Cluster cluster : clusters) {
            Pair<Boolean, Double> result = cluster.isWithin(value);

            closestCluster = Math.min(closestCluster, result.getValue());

            // Assumes that values cannot belong to two clusters
            if (result.getKey()) {
                // similarity
                return result.getValue();
            }
        }

        // When feature vector did not belong to a cluster, save it to cluster later
        nonBelongers.add(value);

        // returns the distance to the closest cluster
        return closestCluster;
    }

    public List<Cluster> getClusters() {
        return clusters;
    }

}
