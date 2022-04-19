package test_drivers;

import connection.Client;
import connection.ResponseObject;
import org.json.JSONObject;
import statistics.CoverageRecorder;
import util.config.Configuration;

import java.util.ArrayList;
import java.util.List;

public class RippledTestDriverTestNet extends RippledTestDriver {

    public RippledTestDriverTestNet(Client client, CoverageRecorder coverageRecorder) {
        super(client, coverageRecorder);
        // cannot get coverage with this mode
        Configuration.COVERAGE_CHECK = false;
    }


    @Override
    public void prepareServer() {

    }

    /**
     * Run the test by sending the individual's request to the server. The placeholder Strings are replaced by values specific to the server state.
     * For the TestNet this means these values need to be adjusted by generating new account information on the rippled website
     * (due to the fact that the TestNet is reset every once in a while).
     *
     * @param method
     * @param request
     * @return ResponseObject the server's response to the request
     * @throws Exception
     */
    @Override
    public ResponseObject runTest(String method, JSONObject request) throws Exception {
        // IMPORTANT: NUMBER_OF_ACCOUNTS in Configuration must be set according to the number of accounts specified in this method.
        List<String> accounts = new ArrayList<>();
        accounts.add("r32SJaT9rdBgX5rSP4R4McaJyqyNPmQUFV");
        accounts.add("rGfnfgGvPKeB4Ug3EurSee6m1VGMeQkvJ1");
        request = replaceKnownStrings(request, "__ACCOUNT__", accounts);
        List<String> keys = new ArrayList<>();
        keys.add("shHv7ngSvWX3zhrb2NShERjwYfrAT");
        keys.add("shYEDpCF1nNBqerUTZj9CUSJpHpDN");
        request = replaceKnownStrings(request, "__MASTER_KEY__", keys);

        System.out.println(request.toString());

        ResponseObject object = getClient().createRequest(method, request);

        checkCoverage();
        this.nextEvaluation();

        return object;
    }

}
