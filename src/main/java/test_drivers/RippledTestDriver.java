package test_drivers;

import connection.Client;
import connection.ResponseObject;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class RippledTestDriver extends TestDriver {

    public RippledTestDriver(Client client) {
        super(client);
    }

    public void startServer() throws InterruptedException, IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();

        processBuilder.command("/blockchain-testing/startRippled.sh");

        processBuilder.redirectErrorStream(true);

        Process p = processBuilder.start();

        int rc = p.waitFor();
        System.out.println("Process ended with rc =" + rc);
    }

    private ResponseObject retrieveAccounts() throws IOException {
        JSONObject request = new JSONObject();
        request.put("method", "wallet_propose");
        JSONArray params = new JSONArray();
        JSONObject paramObj = new JSONObject();
        paramObj.put("key_type", "secp256k1");
        params.put(0, paramObj);
        request.put("params", params);
        return getClient().createRequest("POST", request);
    }

    private ResponseObject createAccounts(JSONObject accounts) throws IOException {
        JSONObject request = new JSONObject();
        request.put("method", "submit");
        JSONArray params = new JSONArray();
        JSONObject paramObj = new JSONObject();
        paramObj.put("key_type", "secp256k1");
        paramObj.put("secret", "snoPBrXtMeMyMHUVTgbuqAfg1SUTb"); // genesis secret

        JSONObject txJson = new JSONObject();
        txJson.put("TransactionType", "Payment");
        JSONObject amount = new JSONObject();
        amount.put("currency", "XRP");
        amount.put("issuer", "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"); // genesis account
        accounts.put("value", "2000");
        txJson.put("Account", "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"); // genesis account
        txJson.put("Destination", accounts.getJSONObject("results").getString("account_id"));

        paramObj.put("tx_json", txJson);
        params.put(0, paramObj);
        request.put("params", params);

        return getClient().createRequest("POST", request);
    }

    private JSONObject replaceAccountStrings(JSONObject request, JSONObject accounts) {
        return new JSONObject(request.toString().replace("__ACCOUNT__", accounts.getJSONObject("results").getString("account_id")));
    }

    public ResponseObject runTest(String method, JSONObject request) throws Exception {
        startServer();
        System.out.println("PROPOSE WALLETS");
        ResponseObject accounts = retrieveAccounts();
        System.out.println(accounts.getResponseCode());
        System.out.println(accounts.getResponseObject());
        System.out.println("SENDING FROM GENESIS ACCOUNT");
        ResponseObject createAccounts = createAccounts(accounts.getResponseObject());
        System.out.println(createAccounts.getResponseCode());
        System.out.println(createAccounts.getResponseObject());
        System.out.println("REPLACE ACCOUNTS IN REQUEST");
        request = replaceAccountStrings(request, accounts.getResponseObject());

        System.out.println("MAKE REQUEST");
        ResponseObject responseObject = getClient().createRequest(method, request);

        return responseObject;
    }

}
