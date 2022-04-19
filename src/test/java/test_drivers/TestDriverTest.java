package test_drivers;

import connection.Client;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import statistics.CoverageRecorder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestDriverTest {

    @Test
    public void replaceKnownStringsTest() throws MalformedURLException {
        String url_server = "https://s.altnet.rippletest.net:51234"; // The url for the Ripple JSON-RPC API ledger (testnet)
        URL url = new URL(url_server);
        Client client = new Client(url);
        Long runTime = new Long(1 * 60 * 1000);
        TestDriver testDriver = new RippledTestDriver(client, new CoverageRecorder());

        String constant = "__ACCOUNT__";
        JSONObject request = new JSONObject();
        request.put("sender", "__ACCOUNT__0");
        request.put("receiver", "__ACCOUNT__1");
        List<String> replacements = new ArrayList<>();
        replacements.add("existingAccount0");
        replacements.add("existingAccount1");
        JSONObject newRequest = testDriver.replaceKnownStrings(request, constant, replacements);

        assertEquals("existingAccount0", newRequest.get("sender"));
        assertEquals("existingAccount1", newRequest.get("receiver"));
    }
}