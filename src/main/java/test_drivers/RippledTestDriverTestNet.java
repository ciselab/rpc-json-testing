package test_drivers;

import connection.Client;
import connection.ResponseObject;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RippledTestDriverTestNet extends RippledTestDriver {

    public RippledTestDriverTestNet(Client client, Long runTime, boolean checkCoverage) {
        super(client, runTime, checkCoverage);
    }

    public RippledTestDriverTestNet(Client client, boolean checkCoverage) {
        super(client, checkCoverage);
    }


    @Override
    public void prepTest() {

    }

    @Override
    public ResponseObject runTest(String method, JSONObject request) throws Exception {
        List<String> accounts = new ArrayList<>();
        accounts.add("r3v7D5Sk5Vc5FEyUDQesg3aP2RLHFuEHG6");
        request = replaceKnownStrings(request, "__ACCOUNT__", accounts);
        List<String> keys = new ArrayList<>();
        keys.add("ssKtprBCgVHc5KWCUawQxqhduPB17");
        request = replaceKnownStrings(request, "__MASTER_KEY__", keys);

        return getClient().createRequest(method, request);
    }

}
