package test_drivers;

import connection.Client;
import connection.ResponseObject;
import org.json.JSONObject;

public class RippledTestDriverTestNet extends RippledTestDriver {

    public RippledTestDriverTestNet(Client client, Long runTime) {
        super(client, runTime);
    }

    @Override
    public void prepTest() throws Exception {

    }

    @Override
    public ResponseObject runTest(String method, JSONObject request) throws Exception {
        request = replaceAccountStrings(request, "rhZq6BoEYNWD7gGDw6gieyBwF8Z9JHoWzK");

        return getClient().createRequest(method, request);
    }

}
