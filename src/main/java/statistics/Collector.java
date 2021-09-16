package statistics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Collector {

    private static Collector instance;

    public static Collector getCollector() {
        if (instance == null) {
            instance = new Collector();
        }

        return instance;
    }

    private Map<String, MethodCoverage> internalCoverage;

    private Collector() {
        this.internalCoverage = new HashMap<>();
    }

    public void collect(String method, Integer status, String structure, String vector) {
        if (!internalCoverage.containsKey(method)) {
            internalCoverage.put(method, new MethodCoverage());
        }

        internalCoverage.get(method).statusses.add(status);

        if (!internalCoverage.get(method).structures.containsKey(structure)) {
            internalCoverage.get(method).structures.put(structure, new HashSet<>());
        }

        internalCoverage.get(method).structures.get(structure).add(vector);
    }

    public Map<String, MethodCoverage> getInternalCoverage() {
        return internalCoverage;
    }
}

