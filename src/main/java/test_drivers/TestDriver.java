package test_drivers;

import connection.Client;
import connection.ResponseObject;
import org.json.JSONObject;

public abstract class TestDriver {

    private Client client;

    public TestDriver (Client client) {
        this.client = client;
    }

    public abstract void prepTest() throws Exception;

    public abstract ResponseObject runTest(String method, JSONObject request) throws Exception;

    public Client getClient() {
        return client;
    }
}
