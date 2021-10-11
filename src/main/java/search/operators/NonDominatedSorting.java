package search.operators;

import search.Individual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// stolen from
// https://gitlab.com/tweet.dimitri/jga/-/blob/master/src/main/java/jga/operators/scoring/comparing/FastNonDominated.java
public class NonDominatedSorting {

    public static void compare(List<Individual> population) {
        List<List<Individual>> fronts = new ArrayList<>();
        fronts.add(new ArrayList<>());

        Map<Individual, List<Individual>> dominatedSets = new HashMap<>();
        Map<Individual, Integer> dominatedCounters = new HashMap<>();

        for (Individual p : population) {
            List<Individual> dominationSet = new ArrayList<>();
            int dominatedCounter = 0;
            for (Individual q : population) {
                int dom = 0;

                for (int i = 0; i < p.getFitness().length; i++) {
                    if (p.getFitness()[i] > q.getFitness()[i]) {
                        dom++;
                        if (dom < 0) {
                            // P and Q do not dominate each other
                            break;
                        }
                    } else if (p.getFitness()[i] < q.getFitness()[i]) {
                        dom--;
                        if (dom > 0) {
                            // P and Q do not dominate each other
                            break;
                        }
                    } else {
                        // P and Q do not dominate each other
                        break;
                    }
                }

                if (dom == p.getFitness().length) {
                    dominationSet.add(q);
                } else if (dom == -p.getFitness().length) {
                    dominatedCounter++;
                }

            }

            if (dominatedCounter == 0) {
                // add to front
                fronts.get(0).add(p);
            }

            dominatedSets.put(p, dominationSet);
            dominatedCounters.put(p, dominatedCounter);
        }

        int frontIndex = 0;

        while (!fronts.get(frontIndex).isEmpty()) {
            List<Individual> nextFront = new ArrayList<>();
            for (Individual p : fronts.get(frontIndex)) {
                for (Individual q : dominatedSets.get(p)) {
                    int updatedDominatedCounter = dominatedCounters.get(q) - 1;
                    if (updatedDominatedCounter == 0) {
                        // add to front
                        nextFront.add(q);
                    }
                }
            }
            frontIndex++;
            fronts.add(nextFront);
        }

        crowdingDistanceAssignment(fronts);
    }

    public static void crowdingDistanceAssignment(List<List<Individual>> fronts) {
        int lastScore = 0;
        for (List<Individual> front : fronts) {
            int l = front.size();

            for (Individual i : front) {
                i.setScore(0);
            }

            if (front.size() == 0) {
                continue;
            }

            for (int j = 0; j < front.get(0).getFitness().length; j++) {
                final int objective = j;
                front.sort((o1, o2) -> Double.compare(o2.getFitness()[objective], o1.getFitness()[objective]));

                // Make sure the boundary values are always selected
                front.get(0).setScore(Integer.MAX_VALUE);
                front.get(l - 1).setScore(Integer.MAX_VALUE);

                double maxDiff = (front.get(l - 1).getFitness()[j] - front.get(0).getFitness()[j]);
                for (int i = 1; i < l - 1; i++) {
                    // Avoid integer overflows
                    if (front.get(i).getScore() == Integer.MAX_VALUE) {
                        continue;
                    }

                    // increase a individuals score by a maximum of l and a minimum of 0
                    int score = front.get(i).getScore() + (int) (((front.get(i + 1).getFitness()[j] - front.get(i - 1).getFitness()[j]) / maxDiff) * l);
                    front.get(i).setScore(score);
                }
            }

            front.sort((o1, o2) -> Integer.compare(o2.getScore(), o1.getScore()));

            for (Individual i : front) {
                i.setScore(lastScore++);
            }
        }

    }

}
