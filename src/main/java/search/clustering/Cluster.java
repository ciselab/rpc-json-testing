package search.clustering;

import util.datastructures.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Cluster.
 * Each cluster has a representative (the 'mean' value) and a radius (the maximum distance from mean to a cluster member).
 */
public class Cluster {

    private SimilarityMetric metric;
    private List<Integer> weightVector;

    private List<List<Object>> members;

    private List<Object> representative;
    private Double similarityThreshold;

    public Cluster(SimilarityMetric metric, List<Integer> weightVector, List<List<Object>> members) {
        this.metric = metric;
        this.weightVector = weightVector;
        this.members = new ArrayList<>();

        for (List<Object> member : members) {
            this.members.add(new ArrayList<>(member));
        }
    }

    public Pair<Boolean, Double> isWithin(List<Object> value) {
        double similarity = metric.calculateSimilaritySingle(this.representative, value, weightVector);

        return new Pair<>(similarity >= similarityThreshold, similarity);
    }

    // TODO unnecessary re-calculation of similarity
    public void findRepresentativeAndRadius() {
        int best = -1;
        double bestMean = 0;
        double bestMinSimilarity = -1;

        for (int i = 0; i < members.size(); i++) {
            double mean = 0;
            double minSimilarity = Double.MAX_VALUE;
            for (int j = 0; j < members.size(); j++) {
                if (i == j) {
                    continue;
                }
                double similarity = metric.calculateSimilaritySingle(members.get(i), members.get(j), weightVector);
                mean += similarity;

                minSimilarity = Math.min(minSimilarity, similarity);
            }

            mean /= members.size();

            if (mean >= bestMean) {
                bestMean = mean;
                best = i;
                bestMinSimilarity = minSimilarity;
            }
        }

        this.representative = members.get(best);
        this.similarityThreshold = bestMinSimilarity;
    }

    public List<Object> getRepresentative() {
        return representative;
    }

    public List<List<Object>> getMembers() {
        return members;
    }

    public int size() {
        return members.size();
    }
}
