package test_drivers;

import connection.Client;
import connection.ResponseObject;
import search.Individual;

public class RippledTestDriver extends TestDriver {

    public RippledTestDriver(Client client) {
        super(client);
    }

    public void prepareTest() throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder();

        processBuilder.command("/blockchain-testing/startRippled.sh");

        processBuilder.redirectErrorStream(true);

        Process p = processBuilder.start();

        int rc = p.waitFor();
        System.out.println("Process ended with rc =" + rc);
    }

    public ResponseObject runTest(Individual individual) throws Exception {
        prepareTest();
        ResponseObject responseObject = getClient().createRequest(individual.getHTTPMethod(), individual.toRequest());

        return responseObject;
    }

}
