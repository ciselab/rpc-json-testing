package test_drivers;

import connection.Client;
import connection.ResponseObject;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RippledTestDriverTestNet extends TestDriver {

    public RippledTestDriverTestNet(Client client, Long runTime) {
        super(client, runTime);
    }

    public RippledTestDriverTestNet(Client client) {
        super(client);
    }


    @Override
    public void prepTest() throws Exception {

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
