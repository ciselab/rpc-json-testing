package test_drivers;

import connection.Client;
import connection.ResponseObject;
import org.json.JSONObject;

public class RippledTestDriverTestNet extends RippledTestDriver {

    public RippledTestDriverTestNet(Client client) {
        super(client);
    }

    @Override
    public void prepareTest() throws Exception {
        // preparing
    }
}
