package test_drivers;

import connection.Client;
import connection.ResponseObject;
import org.json.JSONObject;

import java.util.List;

public abstract class TestDriver {

    private Client client;
    private Long runTime;
    private boolean greenLightSignal;
    private static Long startTime;
    private boolean checkCoverage;

    public TestDriver (Client client, Long runTime, boolean checkCoverage) {
        this.client = client;
        this.runTime = runTime;
        this.greenLightSignal = true;
        this.startTime = System.currentTimeMillis();
        this.checkCoverage = checkCoverage;
    }

    public TestDriver (Client client, boolean checkCoverage) {
        // runtime is set on 5 minutes, necessary for tests
        this(client, (5 * 60 * 1000L), checkCoverage);
    }

    public void checkWhetherToStop() {
        if (System.currentTimeMillis() - startTime >= runTime) {
            this.greenLightSignal = false;
            System.out.println("Stop signal is given! The run time of the experiment is up.");
        }
    }


    protected JSONObject replaceKnownStrings(JSONObject request, String constant, List<String> replacements) {
        String stringObj = request.toString();

        for (int i = 0; i < replacements.size(); i++) {
            stringObj = stringObj.replace(constant + i, replacements.get(i));
        }

        return new JSONObject(stringObj);
    }

    public abstract void prepTest() throws Exception;

    public abstract ResponseObject runTest(String method, JSONObject request) throws Exception;

    public Client getClient() {
        return client;
    }

    public static Long getStartTime() {
        return startTime;
    }

    public Long getTimeLeft() {
        return runTime - (System.currentTimeMillis() - startTime);
    }

    public Boolean shouldContinue() {
        return greenLightSignal;
    }

    public boolean shouldCheckCoverage() {
        return checkCoverage;
    }
}
