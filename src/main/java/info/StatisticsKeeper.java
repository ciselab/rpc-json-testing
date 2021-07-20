package info;

import util.Triple;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StatisticsKeeper {
    private long startTime;

    private List<Triple<Long, Double, Double>> coverageOverTime;

    public StatisticsKeeper() {
        this.startTime = System.nanoTime();
        this.coverageOverTime = new ArrayList<>();
    }

    /**
     * Record the coverage of a certain time.
     * @param time
     * @param branchCoverage
     * @param lineCoverage
     * @throws IOException
     */
    public void recordCoverage(long time, double branchCoverage, double lineCoverage) {
        this.coverageOverTime.add(new Triple<>(time, branchCoverage, lineCoverage));

        // Append coverage to file immediately
        try {
            FileWriter writer = new FileWriter("coverage_over_time.txt", true);
            for (int i = 0; i < coverageOverTime.size(); i++) {
                Long t = coverageOverTime.get(i).getKey();
                double bc = coverageOverTime.get(i).getValue();
                double lc = coverageOverTime.get(i).getValue2();
                writer.write("Time: " + t + ", branch coverage: " + bc +  ", line coverage: " + lc + System.lineSeparator());
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long getStartTime() {
        return startTime;
    }

}
