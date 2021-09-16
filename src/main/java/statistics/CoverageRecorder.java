package statistics;

import util.Triple;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static test_drivers.TestDriver.getStartTime;

public class CoverageRecorder {

    private List<Triple<Long, Double, Double>> coverageOverTime;

    public CoverageRecorder() {
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
        long minutesSinceStart = (time - getStartTime()) * 60 * 1000;

        this.coverageOverTime.add(new Triple<>(time, branchCoverage, lineCoverage));

        // Append coverage to file immediately
        try {
            FileWriter writer = new FileWriter("coverage_over_time.txt", true);
            // For when coverage should be written to file all at once
//            for (int i = 0; i < coverageOverTime.size(); i++) {
//                Long t = coverageOverTime.get(i).getKey();
//                double bc = coverageOverTime.get(i).getValue();
//                double lc = coverageOverTime.get(i).getValue2();
//                writer.write("Time: " + t + ", branch coverage: " + bc +  ", line coverage: " + lc + System.lineSeparator());
//            }
            writer.write("Time: " + minutesSinceStart + " minutes, branch coverage: " + branchCoverage +  ", line coverage: " + lineCoverage + System.lineSeparator());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
