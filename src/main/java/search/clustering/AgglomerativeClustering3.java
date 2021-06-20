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

        clusters = inBetweenClusters.get(maxJumpIndex);

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
     * Called for each individual to retrieve fitness value.
     * Individual is added to be clustered later if distance is large enough.
     * @param value a feature vector
     * @return fitness value
     */
    public double addOne(List<Object> value) {
        for (Cluster cluster : clusters) {
            Pair<Boolean, Double> result = cluster.isWithin(value);
            // Assumes that values cannot belong to two clusters
            if (result.getKey()) {
                // TODO what should this return (should be low fitness value since it is within other cluster (maybe something with the number of members, then we also update the number of members))
                return result.getValue();
            }
        }

        // When feature vector did not belong to a cluster, save it to cluster later
        nonBelongers.add(value);

        return 1000; // TODO what is a realistic fitness value? distance?
    }

    public List<Cluster> getClusters() {
        return clusters;
    }

}


/**
 * Cluster.
 * Each cluster has a representative (the 'mean' value) and a radius (the maximum distance from mean to a cluster member).
 */
class Cluster {

    private SimilarityMetric metric;
    private List<Integer> weightVector;

    private List<List<Object>> members;

    private List<Object> representative;
    private Double radius;

    public Cluster(SimilarityMetric metric, List<Integer> weightVector, List<List<Object>> members) {
        this.metric = metric;
        this.weightVector = weightVector;
        this.members = members;
    }

    public Pair<Boolean, Double> isWithin(List<Object> value) {
        double similarity = metric.calculateSimilaritySingle(this.representative, value, weightVector);

        return new Pair<>(similarity < radius, similarity);
    }

// TODO unnecessary re-calculation of similarity
    public void findRepresentativeAndRadius() {
        int best = -1;
        double bestMean = Double.MAX_VALUE;
        double bestMax = -1;

        for (int i = 0; i < members.size(); i++) {
            double mean = 0;
            double max = 0;
            for (int j = 0; j < members.size(); j++) {
                if (i == j) {
                    continue;
                }
                double similarity = metric.calculateSimilaritySingle(members.get(i), members.get(j), weightVector);
                mean += similarity;

                max = Math.max(max, similarity);
            }

            mean /= members.size();

            if (mean < bestMean) {
                bestMean = mean;
                best = i;
                bestMax = max;
            }
        }

        this.representative = members.get(best);
        this.radius = bestMax;
    }

    public List<Object> getRepresentative() {
        return representative;
    }

    public Double getRadius() {
        return radius;
    }

    public List<List<Object>> getMembers() {
        return members;
    }
}