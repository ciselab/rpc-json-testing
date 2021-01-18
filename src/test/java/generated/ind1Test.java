package generated;

import connection.Client;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ind1Test {
    private static String url_ripple = "https://s.altnet.rippletest.net:51234";
    private static Client client;

    @BeforeAll
    public static void prep () {
        try {
            client = new Client(new URL(url_ripple));
            } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test () {
        String method = "POST";
        JSONObject request = new JSONObject("{\"method\":\"account_lines\",\"params\":[{\"ledger_index\":2536092091,\"ledger_hash\":\"60De0ceAaA026C378b0Ed3ceD3aD4b5mFRcDdbdf1AeA0B9CC43F7b1D0E9D945FbA22\",\"account\":\"nJwu89Vu8bfTVJxvIi7MtIwO\"}]}");

        try {
            client.createRequest(method, request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}