package connection;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Client that makes connection to a JSON-RPC API.
 */
public class Client {
    private URL serverURI;

    public Client(URL serverURI) throws IOException {
        this.serverURI  = serverURI;
    }

    // Based on https://www.twilio.com/blog/5-ways-to-make-http-requests-in-java
    public int createRequest(String method, JSONObject request) throws IOException {
        // Open a connection on the URL and cast the response
        HttpURLConnection con = (HttpURLConnection) serverURI.openConnection();

        con.setRequestProperty("Content-Type", "application/json; utf-8");

        con.setDoOutput(true);

        con.setRequestMethod(method);

        String jsonInputString = request.toString();
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        String jsonOutputString = "";

        try(BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            jsonOutputString = response.toString();
        }

        // Print the response
        System.out.println(new JSONObject(jsonOutputString));

        return con.getResponseCode();
    }
    
}