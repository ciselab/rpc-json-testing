package test_drivers;

import connection.Client;
import connection.ResponseObject;

import statistics.CoverageRecorder;
import org.json.JSONObject;

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
    private CoverageRecorder coverageRecorder;

    public GanacheTestDriver(Client client, CoverageRecorder coverageRecorder) {
        super(client);
        this.coverageRecorder = coverageRecorder;
    }

    /**
     * Execute the script to start up the ganache server.
     * @throws IOException
     */
    public void startServer() throws IOException {
        ProcessBuilder pb = new ProcessBuilder();

        pb.command("/blockchain-testing/scripts/ganache/startGanache.sh");

        pb.redirectErrorStream(true);

        Process p = pb.start();

        try {
            p.waitFor();
        } catch (InterruptedException e) {
            p.destroy();
        }
    }

    /**
     * Read the account information that it outputted upon starting the server.
     * @throws IOException
     */
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

    /**
     * Prepare the server by reading and storing the provided account information.
     * @throws IOException
     */
    public void prepareServer() throws Exception {
        startServer();
        retrieveAccounts();
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

        request = replaceKnownStrings(request, "__ACCOUNT__",  accounts);
        request = replaceKnownStrings(request, "__MASTER_KEY__",  keys);

        // TODO something with private keys as well
        ResponseObject object = getClient().createRequest(method, request);

        checkCoverage();
        this.nextEvaluation();

        return object;
    }

    /**
     * Run the script to compute the coverage and read and store the results.
     * @param timePassed
     * @throws IOException
     */
    public void recordCoverage(Long timePassed, Long generation, Long evaluation) throws IOException {
        ProcessBuilder pb = new ProcessBuilder();

        pb.command("/blockchain-testing/scripts/ganache/coverageGanache.sh");

        pb.redirectErrorStream(true);

        Process p = pb.start();

        String branchcov = "";
        String linecov = "";
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(p.getInputStream()))) {

            String line;

            while ((line = reader.readLine()) != null) {
                if (line.contains("Branches")) {
                    branchcov = line;

                }
                if (line.contains("Lines")) {
                    linecov = line;
                    break;
                }
            }
        }

        try {
            p.waitFor();
        } catch (InterruptedException e) {
            p.destroy();
        }

        String branches = branchcov
                .split("\\(")[1]
                .split("\\)")[0];

        double branchesCovered = Double.parseDouble(branches.split("/")[0]);
        int branchTotal = Integer.parseInt(branches.split("/")[1].trim());

        String lines = linecov
                .split("\\(")[1]
                .split("\\)")[0];

        double linesCovered = Double.parseDouble(lines.split("/")[0]);
        int linesTotal = Integer.parseInt(lines.split("/")[1].trim());

        coverageRecorder.recordCoverage(timePassed, generation, evaluation, linesCovered, linesTotal, branchesCovered, branchTotal);
    }

}
