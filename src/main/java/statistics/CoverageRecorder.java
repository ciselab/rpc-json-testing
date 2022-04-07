package statistics;

import util.datastructures.Triple;
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
     * @param milliSecondsSinceStart
     * @param linesCovered
     * @param lineTotal
     * @param branchesCovered
     * @param branchTotal
     * @throws IOException
     */
    public void recordCoverage(long milliSecondsSinceStart, double linesCovered, int lineTotal, double branchesCovered, int branchTotal) {

        Long minutesSinceStart = (milliSecondsSinceStart - getStartTime()) / (60 * 1000);

        double lineCoverage = linesCovered / lineTotal;
        double branchCoverage = branchesCovered / branchTotal;

        System.out.println("Intermediate coverage results at time: " + minutesSinceStart + " = branch cov: " + branchCoverage + " and line cov: " + lineCoverage);

        this.coverageOverTime.add(new Triple<>(minutesSinceStart, branchCoverage, lineCoverage));

        // Append coverage to file immediately
        try {
            FileWriter writer = new FileWriter("coverage_over_time.txt", true);
            writer.write("Time: " + minutesSinceStart + " minutes, branch coverage: " + branchCoverage +  " (" + branchesCovered + " branches), line coverage: " + lineCoverage + " (" + linesCovered + " lines)" + System.lineSeparator());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
