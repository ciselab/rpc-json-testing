package test_drivers;

import connection.Client;
import connection.ResponseObject;
import org.json.JSONObject;

public abstract class TestDriver {

    private Client client;
    private Long runTime;
    private Long startTime;
    private boolean greenLightSignal;

    public TestDriver (Client client, Long runTime) {
        this.client = client;
        this.runTime = runTime;
        this.startTime = System.currentTimeMillis();
        this.greenLightSignal = true;
    }

    public void checkWhetherToStop() {
        if (System.currentTimeMillis() - startTime >= runTime) {
            this.greenLightSignal = false;
            System.out.println("Stop signal is given! The run time of the experiment is up.");
        }
    }

    public abstract void prepTest() throws Exception;

    public abstract ResponseObject runTest(String method, JSONObject request) throws Exception;

    public Client getClient() {
        return client;
    }

    public Long getStartTime() {
        return startTime;
    }

    public Boolean shouldContinue() {
        return greenLightSignal;
    }

}
