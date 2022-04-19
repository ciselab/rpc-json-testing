package test_drivers;

import connection.Client;
import connection.ResponseObject;
import org.json.JSONObject;
import sun.plugin.dom.exception.InvalidStateException;
import util.config.BudgetType;
import util.config.Configuration;
import util.config.CoverageIntervalType;

import java.io.IOException;
import java.util.List;

public abstract class TestDriver {

    private Client client;

    private Long startTime;
    private Long generation;
    private Long evaluation;

    private Long lastCoverageStored;

    private boolean firstServerPreparation;

    public TestDriver (Client client) {
        this.client = client;
        this.startTime = System.currentTimeMillis();
        this.generation = 0L;
        this.evaluation = 0L;

        this.lastCoverageStored = 0L;

        this.firstServerPreparation = true;
    }

    protected JSONObject replaceKnownStrings(JSONObject request, String constant, List<String> replacements) {
        String stringObj = request.toString();

        for (int i = 0; i < replacements.size(); i++) {
            stringObj = stringObj.replace(constant + i, replacements.get(i));
        }

        return new JSONObject(stringObj);
    }


    /**
     * This method is executed before each test to make sure the server is in the same state for each test.
     * @throws Exception
     */
    public void prepareTest() throws Exception {
        prepareServer();

        if (firstServerPreparation) {
            checkCoverage(true);
            firstServerPreparation = false;
            prepareServer();
        }
    }


    public abstract void prepareServer() throws Exception;

    public abstract ResponseObject runTest(String method, JSONObject request) throws Exception;

    public Client getClient() {
        return client;
    }

    private Long getTimeLeft() {
        final long runtime = (Configuration.BUDGET * 60 * 1000);
        return runtime - (System.currentTimeMillis() - startTime);
    }

    public boolean shouldContinue() {
        if (Configuration.BUDGET_TYPE == BudgetType.TIME) {
            if (getTimeLeft() <= 0) {
                System.out.println("Stop signal is given! The time budget of the experiment has run out.");
                return false;
            }
        } else if (Configuration.BUDGET_TYPE == BudgetType.EVALUATIONS) {
            if (this.evaluation >= Configuration.BUDGET) {
                System.out.println("Stop signal is given! The evaluation budget of the experiment has run out.");
                return false;
            }
        } else if (Configuration.BUDGET_TYPE == BudgetType.GENERATION) {
            if (this.generation >= Configuration.BUDGET) {
                System.out.println("Stop signal is given! The generation budget of the experiment has run out.");
                return false;
            }
        } else {
            throw new InvalidStateException("Unknown budget type");
        }

        return true;
    }

    public void checkCoverage() throws IOException {
        checkCoverage(false);
    }

    /**
     * Check whether coverage should be measured (when a certain time has passed).
     * @param force this is to make sure that coverage is recorded regardless of the interval, it does not override the "COVERAGE_CHECK" parameter
     * @throws IOException
     */
    public void checkCoverage(boolean force) throws IOException {
        if (!Configuration.COVERAGE_CHECK) {
            return;
        }

        Long currentTime = System.currentTimeMillis();
        Long timePassed = currentTime - startTime;

        if (Configuration.COVERAGE_INTERVAL_TYPE == CoverageIntervalType.TIME) {
            if (force || currentTime - lastCoverageStored >= Configuration.INTERVAL) {
                recordCoverage(timePassed, this.generation, this.evaluation);
                lastCoverageStored = currentTime;
            }
        } else if (Configuration.COVERAGE_INTERVAL_TYPE == CoverageIntervalType.EVALUATIONS) {
            if (force || this.evaluation - lastCoverageStored >= Configuration.INTERVAL) {
                recordCoverage(timePassed, this.generation, this.evaluation);
                lastCoverageStored = this.evaluation;
            }
        } else if (Configuration.COVERAGE_INTERVAL_TYPE == CoverageIntervalType.GENERATION) {
            if (force || this.generation - lastCoverageStored >= Configuration.INTERVAL) {
                recordCoverage(timePassed, this.generation, this.evaluation);
                lastCoverageStored = this.generation;
            }
        }
    }

    public abstract void recordCoverage(Long timePassed, Long generation, Long evaluation) throws IOException;

    public long nextGeneration() {
        return ++generation;
    }

    public long nextEvaluation() {
        return ++evaluation;
    }
}
