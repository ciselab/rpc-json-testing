package test_drivers;

import connection.Client;
import connection.ResponseObject;

import info.StatisticsKeeper;
import org.json.JSONObject;
import util.Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GanacheTestDriver extends TestDriver {

    private List<String> accounts;
    private List<String> keys;
    private StatisticsKeeper sk;
    private Long previousTimeStored;

    public GanacheTestDriver(Client client, Long runTime) {
        super(client, runTime);
        sk = new StatisticsKeeper();
        previousTimeStored = System.currentTimeMillis();
    }

    public GanacheTestDriver(Client client) {
        super(client);
        sk = new StatisticsKeeper();
        previousTimeStored = System.currentTimeMillis();
    }

    public void startServer() throws IOException {
        ProcessBuilder pb = new ProcessBuilder();

        pb.command("/blockchain-testing/scripts/startGanache.sh");

        pb.redirectErrorStream(true);

        Process p = pb.start();

        try {
            p.waitFor();
        } catch (InterruptedException e) {
            p.destroy();
        }
    }

    private void retrieveAccounts() throws IOException {
        accounts = new ArrayList<>();
        keys = new ArrayList<>();
        try {
            File myObj = new File("/ganache-cli/output.txt");
            Scanner myReader = new Scanner(myObj);

            boolean scanningAccounts = false;
            boolean scanningKeys = false;

            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();

                if (data.length() == 0) {
                    scanningAccounts = false;
                    scanningKeys = false;
                    continue;
                }

                if (data.contains("Available Accounts")) {
                    scanningAccounts = true;
                    myReader.nextLine();
                    continue;
                }

                if (scanningAccounts) {
                    accounts.add(data.split(" ")[1]);
                }

                if (data.contains("Private Keys is reached")) {
                    scanningKeys = true;
                    myReader.nextLine();
                    continue;
                }

                if (scanningKeys) {
                    keys.add(data.split(" ")[1]);
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
            e.printStackTrace();
        }
    }

    public void prepTest() throws Exception {
        checkCoverage();    // Check whether coverage needs to be documented.
        startServer();
        retrieveAccounts();
    }

    protected JSONObject replaceAccountStrings(JSONObject request, String account) {
        return new JSONObject(request.toString().replace("__ACCOUNT__", account));
    }

    /**
     * Before this method prepTust is run.
     * @param method
     * @param request
     * @return the responseObject (status code + response)
     * @throws Exception
     */
    public ResponseObject runTest(String method, JSONObject request) throws Exception {

        if (accounts == null) {
            throw new Exception("No accounts found! Something went wrong.");
        }

        request = replaceAccountStrings(request,  accounts.get(0));

        // TODO something with private keys as well
        return getClient().createRequest(method, request);
    }

    public void checkCoverage() throws IOException {
        // Check whether coverage should be measured
        Long currentTime = System.currentTimeMillis();
        if (currentTime - previousTimeStored >= Configuration.getRECORDING_COVERAGE_TIME()) {
            previousTimeStored = currentTime;

            String[] results = retrieveCoverage().split("\\|");

            double branchcoverage = Double.parseDouble(results[2].trim());
            double linecoverage = Double.parseDouble(results[4].trim());
            sk.recordCoverage(currentTime, branchcoverage, linecoverage);

            System.out.println("Intermediate coverage results at time: " + currentTime + " = branch cov: " + branchcoverage + " and line cov: " + linecoverage);
        }
    }

    public String retrieveCoverage() throws IOException {
        ProcessBuilder pb = new ProcessBuilder();

        pb.command("/blockchain-testing/scripts/coverageGanache.sh");

        pb.redirectErrorStream(true);

        Process p = pb.start();

        String coverage = "";
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(p.getInputStream()))) {

            String line;

            while ((line = reader.readLine()) != null) {
                if (line.contains("All files")) {
                    coverage = line;
                    break;
                }
            }
        }

        try {
            p.waitFor();
        } catch (InterruptedException e) {
            p.destroy();
        }

        return coverage;
    }

}
