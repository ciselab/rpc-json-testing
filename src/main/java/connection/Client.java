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
    /**
     * Send a JSON-RPC request to the server and retrieve the response.
     * @param method
     * @param request
     * @return ResponseObject consisting of a HTTP status code and a JSON object
     * @throws IOException
     */
    public ResponseObject createRequest(String method, JSONObject request) throws IOException {
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

        String jsonOutputString;

        // Wait for response
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
            
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            jsonOutputString = response.toString();
        }
        catch(IOException e) {
            System.out.println(method);
            System.out.println(request.toString(2));
            e.printStackTrace();
            //TODO: fix this 400 error exception
            jsonOutputString = "{}";
        }

        JSONObject response = new JSONObject(jsonOutputString);

        con.disconnect();

//        System.out.println(request.toString(2));
//        System.out.println(response.toString(2));

        return new ResponseObject(con.getResponseCode(), response);
    }

//    public CompletableFuture<JSONObject> sendRequest(int id, JSONObject jsonObject) {
//        CompletableFuture<JSONObject> future = new CompletableFuture<>();
//
//        futures.put(id, future);
//
//        this.send(jsonObject.toString());
//
//        return future;
//    }

}