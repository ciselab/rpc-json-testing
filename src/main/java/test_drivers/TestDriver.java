package test_drivers;

import connection.Client;
import connection.ResponseObject;
import search.Individual;

import java.io.IOException;

public abstract class TestDriver {

    private Client client;

    public TestDriver (Client client) {
        this.client = client;
    }

    public abstract ResponseObject runTest(Individual individual) throws IOException, InterruptedException;

    public Client getClient() {
        return client;
    }
}
