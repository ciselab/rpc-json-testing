package test_drivers;

import connection.Client;
import connection.ResponseObject;
import search.Individual;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RippledTestDriver extends TestDriver {

    public RippledTestDriver(Client client) {
        super(client);
    }

    public void prepareTest() throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder();

        System.out.println("line 1");

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

        System.out.println("2");
        processBuilder.redirectErrorStream(true);
        System.out.println("3");

        Process p = processBuilder.start();
        System.out.println("4");

        String output = loadStream(p.getInputStream());
        System.out.println("5");

        String error  = loadStream(p.getErrorStream());
        System.out.println("6");

//        int rc = p.waitFor();
//        System.out.println("Process ended with rc=" + rc);
        System.out.println("\nStandard Output:\n");
        System.out.println(output);
        System.out.println("\nStandard Error:\n");
        System.out.println(error);

        System.exit(0);
    }

    private String loadStream(InputStream s) throws Exception
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(s));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line=br.readLine()) != null) {
            System.out.println(line);
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    public ResponseObject runTest(Individual individual) throws Exception {
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
