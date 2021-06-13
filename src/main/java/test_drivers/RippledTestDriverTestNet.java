package test_drivers;

import connection.Client;
import connection.ResponseObject;
import org.json.JSONObject;

public class RippledTestDriverTestNet extends RippledTestDriver {

    public RippledTestDriverTestNet(Client client) {
        super(client);
    }

    @Override
    public ResponseObject runTest(String method, JSONObject request) throws Exception {
        // TODO find a different way (hardcode accounts)
//        startServer();
//        System.out.println("PROPOSE WALLETS");
//        ResponseObject accounts = retrieveAccounts();
//        System.out.println(accounts.getResponseCode());
//        System.out.println(accounts.getResponseObject());
//        System.out.println("SENDING FROM GENESIS ACCOUNT");
//        ResponseObject createAccounts = createAccounts(accounts.getResponseObject());
//        System.out.println(createAccounts.getResponseCode());
//        System.out.println(createAccounts.getResponseObject());
//        System.out.println("REPLACE ACCOUNTS IN REQUEST");
//        request = replaceAccountStrings(request, accounts.getResponseObject());

        System.out.println("MAKE REQUEST");
        ResponseObject responseObject = getClient().createRequest(method, request);

        return responseObject;
    }

}
