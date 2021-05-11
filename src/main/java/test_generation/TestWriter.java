package test_generation;

import search.Individual;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TestWriter {

    private String url;
    private String testDirectory;

    public TestWriter(String url, String testDirectory) {
        this.url = url;
        this.testDirectory = testDirectory;
    }

    public void writeTest(Individual individual, String name) throws IOException {
        String test = "package generated;\n" +
            "\n" +
            "import connection.Client;\n" +
            "import org.json.JSONObject;\n" +
            "import org.junit.jupiter.api.BeforeAll;\n" +
            "import org.junit.jupiter.api.Test;\n" +
            "\n" +
            "import java.io.IOException;\n" +
            "import java.net.MalformedURLException;\n" +
            "import java.net.URL;\n" +
            "\n" +
            "public class " + name + " {\n" +
            "    private static String url_ripple = \"" + url + "\";\n" +
            "    private static Client client;\n" +
            "\n" +
            "    @BeforeAll\n" +
            "    public static void prep () {\n" +
            "        try {\n" +
            "            client = new Client(new URL(url_ripple));\n" +
            "            } catch (MalformedURLException e) {\n" +
            "            e.printStackTrace();\n" +
            "        } catch (IOException e) {\n" +
            "            e.printStackTrace();\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    @Test\n" +
            "    public void test () {\n" +
            "        String method = \"" + individual.getHTTPMethod() + "\";\n" +
            "        JSONObject request = new JSONObject(\"" + individual.toRequest().toString().replace("\\", "\\\\").replace("\"", "\\\"")  + "\");\n\n" +
            "        try {\n" +
            "            client.createRequest(method, request);\n" +
            "        } catch (IOException e) {\n" +
            "            e.printStackTrace();\n" +
            "        }\n" +
            "    }\n" +
            "}";

        try (FileWriter fw = new FileWriter(new File(testDirectory + "/" + name + ".java"))) {
            fw.write(test);
        }
    }
}
