package connection;

import org.json.JSONObject;
import util.config.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Client that makes connection to a JSON-RPC API.
 */
public class Client {

    private URL serverURI;

    public Client(URL serverURI) {
        this.serverURI  = serverURI;
    }

    /**
     * Send a JSON-RPC request to the server and retrieve the response.
     * @param method
     * @param request
     * @return ResponseObject consisting of a HTTP status code and a JSON object
     * @throws IOException
     */
    public ResponseObject createRequest(String method, JSONObject request) throws IOException {
        return createRequest(method, request, 0);
    }

    /**
     * Send a JSON-RPC request to the server and retrieve the response.
     * Based on https://www.twilio.com/blog/5-ways-to-make-http-requests-in-java
     * @param method
     * @param request
     * @return ResponseObject consisting of a HTTP status code and a JSON object
     * @throws IOException
     */
    public ResponseObject createRequest(String method, JSONObject request, int retry) throws IOException {
        // Open a connection on the URL and cast the response
        HttpURLConnection con = (HttpURLConnection) serverURI.openConnection();
        con.setConnectTimeout(5000); // 5 second timeout

        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setDoOutput(true);
        con.setRequestMethod(method);

        // Create the request (my output towards the server)
        String jsonInputString = request.toString();
        try {
            OutputStream os = con.getOutputStream();
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        } catch (ConnectException e) {
            e.printStackTrace();
            System.out.println("ConnectException occurred while trying to get output stream!");
        } catch (IOException e) {
            e.printStackTrace();
        }

        String jsonOutputString;
        int responseCode;

        // Wait for response
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));

            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            jsonOutputString = response.toString();
            responseCode = con.getResponseCode();

        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            System.out.println("SocketTimeoutException! Response gets assigned statusCode -3.");
            jsonOutputString = "{}";
            responseCode = -3;

            if (retry < Configuration.MAX_ATTEMPTS) {
                System.out.println("Retrying... " + (retry + 1));
                return this.createRequest(method, request, retry + 1);
            }

        } catch (ConnectException e) {
            e.printStackTrace();
            System.out.println("ConnectException! Response gets assigned statusCode -1.");
            System.out.println("Request was: " + jsonInputString);
            jsonOutputString = "{}";
            responseCode = -1;

            if (retry < Configuration.MAX_ATTEMPTS) {
                System.out.println("Retrying... " + (retry + 1));
                return this.createRequest(method, request, retry + 1);
            }

        } catch (SocketException e) {
            e.printStackTrace();
            System.out.println("SocketException! Response gets assigned statusCode -2.");
            System.out.println("Request was: " + jsonInputString);
            jsonOutputString = "{}";
            responseCode = -2;

            if (retry < Configuration.MAX_ATTEMPTS) {
                System.out.println("Retrying... " + (retry + 1));
                return this.createRequest(method, request, retry + 1);
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IOException occurred! No response body but status code was " + con.getResponseCode());
            System.out.println("Request was: " + jsonInputString);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("responseMessage", con.getResponseMessage());
            // Add the request to make sure that we get a different response structure if the request is different
            jsonObject.put("request", request);
            jsonOutputString = jsonObject.toString();
            responseCode = con.getResponseCode();
        }

        JSONObject response = new JSONObject(jsonOutputString);

        con.disconnect();

        return new ResponseObject(method, request, responseCode, response);
    }

}