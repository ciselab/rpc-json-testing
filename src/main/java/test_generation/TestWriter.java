package test_generation;

import search.Chromosome;
import search.Individual;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Creates a test file corresponding to an individual.
 */
public class TestWriter {

    private String url;
    private String testDirectory;
    private String testDriver;

    public TestWriter(String url, String testDirectory, String testDriver) {
        this.url = url;
        this.testDirectory = testDirectory;
        this.testDriver = testDriver;
    }

    public void writeTest(Individual individual, String name) throws IOException {
        StringBuilder test = new StringBuilder("package generated;\n" +
                "\n" +
                "import connection.Client;\n" +
                "import test_drivers.TestDriver;\n" +
                "import test_drivers." + testDriver + ";\n" +
                "import org.json.JSONObject;\n" +
                "import org.junit.jupiter.api.BeforeAll;\n" +
                "import org.junit.jupiter.api.Test;\n" +
                "import statistics.CoverageRecorder;\n" +
                "\n" +
                "import java.io.IOException;\n" +
                "import java.net.MalformedURLException;\n" +
                "import java.net.URL;\n" +
                "\n" +
                "public class " + name + " {\n" +
                "    private static String url_server = \"" + url + "\";\n" +
                "    private static TestDriver testDriver;\n" +
                "\n" +
                "    @BeforeAll\n" +
                "    public static void prep () {\n" +
                "        try {\n" +
                "            Client client = new Client(new URL(url_server));\n" +
                "            testDriver = new " + testDriver + "(client, new CoverageRecorder());\n" +
                "        } catch (MalformedURLException e) {\n" +
                "            e.printStackTrace();\n" +
                "        } catch (IOException e) {\n" +
                "            e.printStackTrace();\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    @Test\n" +
                "    public void test () {\n" +
                "        try {\n" +
                "            testDriver.prepareTest();\n" +
                "            String method;\n" +
                "            JSONObject request;\n\n");

        for (Chromosome chromosome : individual.getDna()) {
            test.append("            method = \"").append(chromosome.getHTTPMethod()).append("\";\n").append("            request = new JSONObject(\"").append(chromosome.toRequest().toString().replace("\\", "\\\\").replace("\"", "\\\"")).append("\");\n").append("            testDriver.runTest(method, request);\n\n");
        }

        test.append("        } catch (Exception e) {\n" + "            e.printStackTrace();\n" + "        }\n" + "    }\n" + "}");
        try (FileWriter fw = new FileWriter(new File(testDirectory + "/" + name + ".java"))) {
            fw.write(test.toString());
        }
    }
}
