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

        processBuilder.command("/blockchain-testing/startRippled.sh");
//        processBuilder.command("cmd", "/c", "startRippled.sh");

//        processBuilder.command("/bin/bash", "startRippled.sh");
//        processBuilder.command("bash", "-c", "kill", "$(lsof -t -i:5005)");
//        processBuilder.command("bash", "-c", "sleep", "120");
//        processBuilder.command("bash", "-c", "cd", "rippled-1.6.0/build/cmake/coverage");
//        processBuilder.command("bash", "-c", "./rippled", "-a", "-v", "--debug", "&", "disown");
//        processBuilder.command("bash", "-c", "sleep", "120");

//        # kill current rippled server at port 5005
//        kill $(lsof -t -i:5005)
//        sleep 120
//
//# start rippled server again
//        cd rippled-1.6.0/build/cmake/coverage
//            ./rippled -a -v --debug & disown
//        sleep 120

        Process process = processBuilder.start();

        printResults(process);
        process.waitFor();

        System.exit(0);
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
