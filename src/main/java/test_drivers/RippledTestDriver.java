package test_drivers;

import connection.Client;
import connection.ResponseObject;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class RippledTestDriver extends TestDriver {

    private JSONObject accounts;

    public RippledTestDriver(Client client) {
        super(client);
    }

    public void startServer() throws InterruptedException, IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();

        processBuilder.command("/blockchain-testing/startRippled.sh");

        processBuilder.redirectErrorStream(true);

        Process p = processBuilder.start();

        int rc = p.waitFor();
        p.destroy();
        
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
        paramObj.put("secret", "snoPBrXtMeMyMHUVTgbuqAfg1SUTb"); // genesis secret
//        paramObj.put("offline", false);
//        paramObj.put("fee_multi_max", 1000);

        JSONObject txJson = new JSONObject();
        txJson.put("TransactionType", "Payment");
        txJson.put("Account", "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"); // genesis account
        txJson.put("Destination", accounts.getJSONObject("result").getString("account_id"));
        txJson.put("Amount", "250000000"); // reserve is 200,000,000 XRP drops

        paramObj.put("tx_json", txJson);

        params.put(0, paramObj);
        request.put("params", params);

        System.out.println(request.toString(2));
        return getClient().createRequest("POST", request);
    }

        public void prepTest() throws Exception {
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
        this.accounts = accounts.getResponseObject();
    }

    private JSONObject replaceAccountStrings(JSONObject request, JSONObject accounts) {
        return new JSONObject(request.toString().replace("__ACCOUNT__", accounts.getJSONObject("result").getString("account_id")));
    }

    public ResponseObject runTest(String method, JSONObject request) throws Exception {
        if (accounts == null) {
            throw new Exception("No accounts found! Please call prepTest before runTest!!");
        }

        request = replaceAccountStrings(request, accounts);

        return getClient().createRequest(method, request);
    }

}
