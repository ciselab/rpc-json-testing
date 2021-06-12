package test_drivers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class RippledTestDriver {

    ProcessBuilder processBuilder = new ProcessBuilder();

    public void prepareTest() throws IOException {
//        processBuilder.command("cmd", "/c", "dir");

        processBuilder.command("cmd", "/c", "start", "startRippled.sh");
        Process process = processBuilder.start();

        printResults(process);
    }

    public static void printResults(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = "";
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
    }

    public static void main(String... args) {
        RippledTestDriver r = new RippledTestDriver();

        try {
            r.prepareTest();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
