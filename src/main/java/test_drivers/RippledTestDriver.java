package test_drivers;

import connection.Client;
import connection.ResponseObject;
import statistics.CoverageRecorder;
import org.json.JSONArray;
import org.json.JSONObject;
import util.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

public class RippledTestDriver extends TestDriver {

    private List<JSONObject> accounts;
    private CoverageRecorder sk;
    private Long previousTimeStored;
    private boolean atStart;

    public RippledTestDriver(Client client, Long runTime) {
        super(client, runTime);
        sk = new CoverageRecorder();
        previousTimeStored = System.currentTimeMillis();
        atStart = true;
    }

    public RippledTestDriver(Client client) {
        super(client);
        sk = new CoverageRecorder();
        previousTimeStored = System.currentTimeMillis();
        atStart = true;
    }

    public void startServer() throws IOException {
        System.out.println("Starting server function");
        ProcessBuilder processBuilder = new ProcessBuilder();

        processBuilder.command("/blockchain-testing/scripts/rippled/startRippled.sh");

        processBuilder.redirectErrorStream(true);
        System.out.println("starting");

        Process p = processBuilder.start();

        try {
            System.out.println("waiting");
            p.waitFor();
        } catch (InterruptedException ex) {
            p.destroy();
        }
        System.out.println("done waiting");

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

    private void transferCurrencyToAccounts(JSONObject accounts) throws IOException {
        JSONObject request = new JSONObject();
        request.put("method", "acccount_info");
        JSONArray params = new JSONArray();
        JSONObject paramObj = new JSONObject();
        paramObj.put("account", accounts.getJSONObject("result").getString("account_id")); // genesis secret

        params.put(0, paramObj);
        request.put("params", params);

        System.out.println("BEFORE");
        System.out.println(getClient().createRequest("POST", request).getResponseObject().toString());;


        request = new JSONObject();
        request.put("method", "submit");
        params = new JSONArray();
        paramObj = new JSONObject();
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

        getClient().createRequest("POST", request);



        request = new JSONObject();
        request.put("method", "acccount_info");
        params = new JSONArray();
        paramObj = new JSONObject();
        paramObj.put("account", accounts.getJSONObject("result").getString("account_id")); // genesis secret

        params.put(0, paramObj);
        request.put("params", params);

        System.out.println("AFTER");
        System.out.println(getClient().createRequest("POST", request).getResponseObject().toString());
    }

    /**
     * There is no consensus process in stand-alone mode so the ledger index must be manually advanced.
     * @throws IOException
     */
    private void manuallyAdvanceLedger() throws IOException {
        JSONObject request = new JSONObject();
        request.put("method", "ledger_accept");

        ResponseObject responseObject = getClient().createRequest("POST", request);
        // TODO this value could also be used in requests
        int ledgerIndex = Integer.parseInt(responseObject.getResponseObject().getJSONObject("result").getString("ledger_current_index"));
    }

    public void prepTest() throws Exception {
        System.out.println("checking coverage");
        checkCoverage(); // check whether coverage should be stored
        System.out.println("preparing server");
        prepareServer();
        System.out.println("atstart");

        if (atStart) {
            recordCoverage(System.currentTimeMillis());
            atStart = false;
            prepareServer();
        }
        System.out.println("end of preperations");

    }

    public void prepareServer() throws IOException {
        startServer();

        this.accounts = new ArrayList<>();

         System.out.println("Accounts are being gathered.");
        for (int i = 0; i < Configuration.NUMBER_OF_ACCOUNTS; i++) {
            System.out.println("retrieving account " + i);
            ResponseObject accounts = retrieveAccounts();
            if (!accounts.getResponseObject().has("result")) {
                continue;
            }
            System.out.println("filling account " + i);

            transferCurrencyToAccounts(accounts.getResponseObject());
            this.accounts.add(accounts.getResponseObject());
            System.out.println("done account " + i);

        }
         System.out.println("Accounts are successfully prepared.");
    }

    public ResponseObject runTest(String method, JSONObject request) throws Exception {

        // System.out.println("Test will now run.");

        if (accounts == null) {
            throw new Exception("No accounts found! Please call prepTest before runTest!!");
        }

        if (accounts.size() == 0) {
            System.out.println("No accounts found due to errors! Current test will be useless!");
        }

        List<String> accountStrings = new ArrayList<>();
        List<String> masterKeyStrings = new ArrayList<>();
        List<String> masterSeedStrings = new ArrayList<>();
        List<String> masterSeedHexStrings = new ArrayList<>();
        List<String> publicKeyStrings = new ArrayList<>();
        List<String> publicKeyHexStrings = new ArrayList<>();

        for (JSONObject account : accounts) {
            accountStrings.add(account.getJSONObject("result").getString("account_id"));
            masterKeyStrings.add(account.getJSONObject("result").getString("master_key"));
            masterSeedStrings.add(account.getJSONObject("result").getString("master_seed"));
            masterSeedHexStrings.add(account.getJSONObject("result").getString("master_seed_hex"));
            publicKeyStrings.add(account.getJSONObject("result").getString("public_key"));
            publicKeyHexStrings.add(account.getJSONObject("result").getString("public_key_hex"));
        }

        request = replaceKnownStrings(request, "__ACCOUNT__", accountStrings);
        request = replaceKnownStrings(request, "__MASTER_KEY__", masterKeyStrings);
        request = replaceKnownStrings(request, "__MASTER_SEED__", masterSeedStrings);
        request = replaceKnownStrings(request, "__MASTER_SEED_HEX__", masterSeedHexStrings);
        request = replaceKnownStrings(request, "__PUBLIC_KEY__", publicKeyStrings);
        request = replaceKnownStrings(request, "__PUBLIC_KEY_HEX__", publicKeyHexStrings);

        ResponseObject responseObject = getClient().createRequest(method, request);
//        manuallyAdvanceLedger();

        // System.out.println("Test was successfully run.");

        return responseObject;
    }

    public void checkCoverage() throws IOException {
        // Check whether coverage should be measured
        Long currentTime = System.currentTimeMillis();

        if (currentTime - previousTimeStored >= Configuration.RECORD_COVERAGE_INTERVAL) {
            previousTimeStored = currentTime;
            recordCoverage(currentTime);
        }
    }

    public void recordCoverage(Long currentTime) throws IOException {
        System.out.println("Recording coverage function");
        ProcessBuilder pb = new ProcessBuilder();

        pb.command("/blockchain-testing/scripts/rippled/coverageRippled.sh");

        pb.redirectErrorStream(true);

        System.out.println("starting coverage process");
        Process p = pb.start();

        System.out.println("reading");
        StringBuilder coverage = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(p.getInputStream()))) {

            String line;

            while ((line = reader.readLine()) != null) {
                if (line.contains("lines:") || (line.contains("branches:"))) {
                    line = line.substring(line.indexOf("(") + 1);
                    line = line.substring(0, line.indexOf(")"));
                    coverage.append(" ").append(line);
                }
            }
        }
        System.out.println("done reading");

        try {
            System.out.println("waiting on process");
            p.waitFor();
        } catch (InterruptedException e) {
            p.destroy();
        }
        System.out.println("done waiting on process");

        String cov = coverage.toString().trim();

        String[] results = cov.split(" ");

        double linescovered = Double.parseDouble(results[0].replace("(", ""));
        double linetotal = Double.parseDouble(results[3].replace(")", ""));
        double branchescovered = Double.parseDouble(results[4].replace("(", ""));
        double branchtotal = Double.parseDouble(results[7].replace(")", ""));

        double lineCovPer = linescovered / linetotal;
        double branchCovPer = branchescovered / branchtotal;

        System.out.println("recording coverage");
        sk.recordCoverage(currentTime, branchCovPer, lineCovPer);
        System.out.println("done recording");
    }

}
