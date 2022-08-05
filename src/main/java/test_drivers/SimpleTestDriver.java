package test_drivers;

import connection.Client;
import connection.ResponseObject;
import org.json.JSONObject;
import statistics.CoverageRecorder;
import util.config.Configuration;

import java.io.IOException;

/**
 * A simple TestDriver that can be used for any JSON-RPC system (does not record intermediate coverage).
 */
public class SimpleTestDriver extends TestDriver {

    public SimpleTestDriver(Client client, CoverageRecorder coverageRecorder) {
        super(client);
        // cannot get coverage with this mode
        Configuration.COVERAGE_CHECK = false;
    }


    @Override
    public void prepareServer() {

    }

    /**
     * Run the test.
     * @param method
     * @param request
     * @return ResponseObject the server's response to the request
     * @throws Exception
     */
    @Override
    public ResponseObject runTest(String method, JSONObject request) throws Exception {

        ResponseObject object = getClient().createRequest(method, request);

        checkCoverage();
        this.nextEvaluation();

        return object;
    }

    @Override
    public void recordCoverage(Long timePassed, Long generation, Long evaluation) throws IOException {

    }

}
