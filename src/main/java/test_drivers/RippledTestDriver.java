package test_drivers;

import connection.Client;
import connection.ResponseObject;
import info.StatisticsKeeper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RippledTestDriver extends TestDriver {

    private JSONObject accounts;
    private StatisticsKeeper sk;
    private Long previousTimeStored;
    Long DELTA = (long) 60 * 1000;

    public RippledTestDriver(Client client) {
        super(client);
        sk = new StatisticsKeeper();
        previousTimeStored = System.currentTimeMillis();
    }

    public void startServer() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();

        processBuilder.command("/blockchain-testing/scripts/startRippled.sh");

        processBuilder.redirectErrorStream(true);

        Process p = processBuilder.start();

        try {
            int rc = p.waitFor();
        } catch (InterruptedException ex) {
            p.destroy();
        }
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

        return getClient().createRequest("POST", request);
    }

    public void prepTest() throws Exception {
        checkCoverage(); // check whether coverage should be stored

        startServer();
//        System.out.println("PROPOSE WALLETS");
        ResponseObject accounts = retrieveAccounts();
//        System.out.println(accounts.getResponseCode());
//        System.out.println(accounts.getResponseObject());
//        System.out.println("SENDING FROM GENESIS ACCOUNT");
        ResponseObject createAccounts = createAccounts(accounts.getResponseObject());
//        System.out.println(createAccounts.getResponseCode());
//        System.out.println(createAccounts.getResponseObject());
//        System.out.println("REPLACE ACCOUNTS IN REQUEST");
        this.accounts = accounts.getResponseObject();
    }

    protected JSONObject replaceAccountStrings(JSONObject request, String account) {
        return new JSONObject(request.toString().replace("__ACCOUNT__", account));
    }

    public ResponseObject runTest(String method, JSONObject request) throws Exception {

        if (accounts == null) {
            throw new Exception("No accounts found! Please call prepTest before runTest!!");
        }

        request = replaceAccountStrings(request,  accounts.getJSONObject("result").getString("account_id"));

        return getClient().createRequest(method, request);
    }

    public void checkCoverage() throws IOException {
        // Check whether coverage should be measured
        Long currentTime = System.currentTimeMillis();

        if (currentTime - previousTimeStored >= DELTA) {
            System.out.println((currentTime - previousTimeStored)/(1000*60) + " minutes have passed. Coverage is recorded.");
            previousTimeStored = currentTime;

            String cov = retrieveCoverage();
            String[] results = cov.split(" ");

            double linescovered = Double.parseDouble(results[0].replace("(", ""));
            double linetotal = Double.parseDouble(results[3].replace(")", ""));
            double branchescovered = Double.parseDouble(results[4].replace("(", ""));
            double branchtotal = Double.parseDouble(results[7].replace(")", ""));

            double lineCovPer = linescovered/linetotal;
            double branchCovPer = branchescovered/branchtotal;

            System.out.println("Intermediate coverage results for time " + currentTime + " , branch cov: " + branchCovPer + ", and line cov: " + lineCovPer);

            sk.recordCoverage(currentTime, branchCovPer, lineCovPer);
        }
    }

    public String retrieveCoverage() throws IOException {
        ProcessBuilder pb = new ProcessBuilder();

        pb.command("/blockchain-testing/scripts/coverageRippled.sh");

        pb.redirectErrorStream(true);

        Process p = pb.start();

        String coverage = "";
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(p.getInputStream()))) {

            String line;

            while ((line = reader.readLine()) != null) {
                if (line.contains("lines:") || (line.contains("branches:"))) {
                    System.out.println(line);

                    line = line.substring(line.indexOf("(") + 1);
                    line = line.substring(0, line.indexOf(")"));

                    coverage = coverage + " " + line;
                    System.out.println(coverage);
                }
            }
        }

        try {
            p.waitFor();
        } catch (InterruptedException e) {
            p.destroy();
        }

        return coverage.trim();
    }

}
