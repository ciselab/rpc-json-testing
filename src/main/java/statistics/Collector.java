package statistics;

import search.Individual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Collector {

    private static Collector instance;
    private static List<Individual> archive;
    private static Map<Integer, Integer> statusCodesTotal;
    private static Map<Integer, Integer> statusCodesArchive;

    private Map<String, MethodCoverage> internalCoverage;

    private Collector() {
        this.internalCoverage = new HashMap<>();
        this.archive = new ArrayList<>();
        this.statusCodesTotal = new HashMap<>();
        this.statusCodesArchive = new HashMap<>();
    }

    public static Collector getCollector() {
        if (instance == null) {
            instance = new Collector();
        }
        return instance;
    }

    public void addToArchive(Individual ind) {
        archive.add(ind);

        if (!statusCodesArchive.containsKey(ind.getResponseObject().getResponseCode())) {
            statusCodesArchive.put(ind.getResponseObject().getResponseCode(), 0);
        }
        statusCodesArchive.put(ind.getResponseObject().getResponseCode(), statusCodesArchive.get(ind.getResponseObject().getResponseCode()) + 1);
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

    public List<Individual> getArchive() {
        return archive;
    }

    public Map<Integer, Integer> getStatusCodesArchive() {
        return statusCodesArchive;
    }

    public Map<Integer, Integer> getStatusCodesTotal() {
        return statusCodesTotal;
    }
}
