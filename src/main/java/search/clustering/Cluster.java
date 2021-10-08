package search.clustering;

import util.Pair;

import java.util.ArrayList;
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
    private Double radius;

    public Cluster(SimilarityMetric metric, List<Integer> weightVector, List<List<Object>> members) {
        this.metric = metric;
        this.weightVector = weightVector;
        this.members = new ArrayList<>();

        for (List<Object> member : members) {
            this.members.add(new ArrayList<>(member));
        }
    }

    /**
     * Calculates whether the value is within a cluster and the similarity
     * @param value
     * @return
     */
    public Pair<Boolean, Double> isWithin(List<Object> value) {
        double distance = metric.calculateFeatureVectorDistanceSingle(this.representative, value, weightVector);

        return new Pair<>(distance < radius, distance);
    }

    /**
     * Calculate the representative which is the member with the smallest average distance
     * Calculate the radius which is the maximum distance form the representative to the farthest feature vector
     */
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
                double distance = metric.calculateFeatureVectorDistanceSingle(members.get(i), members.get(j), weightVector);
                mean += distance;
                max = Math.max(max, distance);
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

    public int size() {
        return members.size();
    }
}