package test_drivers;

import connection.Client;
import connection.ResponseObject;
import search.Individual;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RippledTestDriver extends TestDriver {

    public RippledTestDriver(Client client) {
        super(client);
    }

    public void prepareTest() throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("/bin/bash", "startRippled.sh");
        Process process = processBuilder.start();

        printResults(process);
        process.waitFor();
    }

    private static void printResults(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
    }

    public ResponseObject runTest(Individual individual) throws IOException, InterruptedException {
        prepareTest();
        ResponseObject responseObject = getClient().createRequest(individual.getHTTPMethod(), individual.toRequest());

        return responseObject;
    }


//    public static void main(String... args) {
//        RippledTestDriver r = new RippledTestDriver();
//
//        try {
//            r.prepareTest();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
