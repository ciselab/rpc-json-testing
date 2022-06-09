package test_drivers;

import connection.Client;
import connection.ResponseObject;
import statistics.CoverageRecorder;
import org.json.JSONArray;
import org.json.JSONObject;
import util.config.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RippledTestDriver extends TestDriver {

    private List<JSONObject> accounts;
    private CoverageRecorder coverageRecorder;

    public RippledTestDriver(Client client, CoverageRecorder coverageRecorder) {
        super(client);
        this.coverageRecorder = coverageRecorder;
    }

    /**
     * Execute the script to start up the rippled server.
     * @throws IOException
     */
    public void startServer() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();

        processBuilder.command("/blockchain-testing/scripts/rippled/startRippled.sh");

        processBuilder.redirectErrorStream(true);

        Process p = processBuilder.start();

        try {
            p.waitFor();
        } catch (InterruptedException ex) {
            p.destroy();
        }
    }

    /**
     * Create and send a request to create an account.
     * @return the response object from the server containing information on the created account.
     * @throws IOException
     */
    private ResponseObject retrieveAccount() throws IOException {
        JSONObject request = new JSONObject();
        request.put("method", "wallet_propose");
        JSONArray params = new JSONArray();
        JSONObject paramObj = new JSONObject();
        paramObj.put("key_type", "secp256k1");
        params.put(0, paramObj);
        request.put("params", params);
        return getClient().createRequest("POST", request);
    }

    /**
     * Create and send a request to transfer currency to the previously created accounts.
     * @param accounts
     * @throws IOException
     */
    private void transferCurrencyToAccounts(JSONObject accounts) throws IOException {
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

        getClient().createRequest("POST", request);
    }

    /**
     * Prepare the server by creating accounts and making sure they have an amount of currencies.
     * @throws IOException
     */
    public void prepareServer() throws IOException {
        startServer();

        this.accounts = new ArrayList<>();

//        System.out.println("Test is being prepared.");
        for (int i = 0; i < Configuration.NUMBER_OF_ACCOUNTS; i++) {
            ResponseObject accounts = retrieveAccount();
            if (!accounts.getResponseObject().has("result")) {
                continue;
            }
            transferCurrencyToAccounts(accounts.getResponseObject());
            this.accounts.add(accounts.getResponseObject());
        }
//        System.out.println("Test was successfully prepared.");
    }

    /**
     * Run the test by sending the individual's request to the server.
     * Before this is done, the placeholder Strings are replaced by values specific to the server state.
     * @param method
     * @param request
     * @return ResponseObject the server's response
     * @throws Exception
     */
    public ResponseObject runTest(String method, JSONObject request) throws Exception {
        System.out.println("Test will now run.");

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

        for (int i = 0; i < accounts.size(); i++) {
            accountStrings.add(accounts.get(i).getJSONObject("result").getString("account_id"));
            masterKeyStrings.add(accounts.get(i).getJSONObject("result").getString("master_key"));
            masterSeedStrings.add(accounts.get(i).getJSONObject("result").getString("master_seed"));
            masterSeedHexStrings.add(accounts.get(i).getJSONObject("result").getString("master_seed_hex"));
            publicKeyStrings.add(accounts.get(i).getJSONObject("result").getString("public_key"));
            publicKeyHexStrings.add(accounts.get(i).getJSONObject("result").getString("public_key_hex"));
        }

        request = replaceKnownStrings(request, "__ACCOUNT__", accountStrings);
        request = replaceKnownStrings(request, "__MASTER_KEY__", masterKeyStrings);
        request = replaceKnownStrings(request, "__MASTER_SEED__", masterSeedStrings);
        request = replaceKnownStrings(request, "__MASTER_SEED_HEX__", masterSeedHexStrings);
        request = replaceKnownStrings(request, "__PUBLIC_KEY__", publicKeyStrings);
        request = replaceKnownStrings(request, "__PUBLIC_KEY_HEX__", publicKeyHexStrings);

        ResponseObject responseObject = getClient().createRequest(method, request);

//        System.out.println("Test was successfully run.");

        checkCoverage();
        this.nextEvaluation();

        return responseObject;
    }



    /**
     * Run the script to compute the coverage and read and store the results.
     * @param timePassed
     * @throws IOException
     */
    public void recordCoverage(Long timePassed, Long generation, Long evaluation) throws IOException {
        ProcessBuilder pb = new ProcessBuilder();

        pb.command("/blockchain-testing/scripts/rippled/coverageRippled.sh");

        pb.redirectErrorStream(true);

        Process p = pb.start();

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

        try {
            p.waitFor();
        } catch (InterruptedException e) {
            p.destroy();
        }

        String cov = coverage.toString().trim();
        String[] results = cov.split(" ");
        int linescovered = Integer.parseInt(results[0].replace("(", ""));
        int linetotal = Integer.parseInt(results[3].replace(")", ""));
        int branchescovered = Integer.parseInt(results[4].replace("(", ""));
        int branchtotal = Integer.parseInt(results[7].replace(")", ""));

        coverageRecorder.recordCoverage(timePassed, generation, evaluation, linescovered, linetotal, branchescovered, branchtotal);
    }

}
