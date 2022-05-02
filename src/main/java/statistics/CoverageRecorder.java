package statistics;

import java.io.FileWriter;
import java.io.IOException;

public class CoverageRecorder {

     public CoverageRecorder() {
         try {
             FileWriter writer = new FileWriter("coverage_over_time.txt", true);
             writer.write("Time"
                     + "," + "Generation"
                     + "," + "Evaluation"
                     + "," + "Branches Covered"
                     + "," + "Branches Total"
                     + "," + "Branch Coverage"
                     + "," + "Lines Covered"
                     + "," + "Lines Total"
                     + "," + "Line Coverage"
                     + System.lineSeparator()
             );
             writer.close();
         } catch (IOException e) {
             e.printStackTrace();
         }

     }
    /**
     * Record the coverage of a certain time.
     * @param timePassed
     * @param linesCovered
     * @param lineTotal
     * @param branchesCovered
     * @param branchTotal
     * @throws IOException
     */
    public void recordCoverage(long timePassed, long generation, long evaluation, double linesCovered, int lineTotal, double branchesCovered, int branchTotal) {

        double lineCoverage = linesCovered / lineTotal;
        double branchCoverage = branchesCovered / branchTotal;
        Long minutesSinceStart = (timePassed) / (60 * 1000);
        System.out.println("Intermediate coverage results at time: " + minutesSinceStart + " mins = branch cov: " + branchCoverage + " and line cov: " + lineCoverage);

        // Append coverage to file immediately
        try {
            FileWriter writer = new FileWriter("coverage_over_time.txt", true);
            writer.write(String.valueOf(timePassed)
                    + "," + generation
                    + "," + evaluation
                    + "," + branchesCovered
                    + "," + branchTotal
                    + "," + branchCoverage
                    + "," + linesCovered
                    + "," + lineTotal
                    + "," + lineCoverage
                    + System.lineSeparator()
            );
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
