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

    private static ArrayList<Map<Integer, Integer>> statusCodesPerGen;
    private static ArrayList<Map<Integer, Integer>> statusCodesInArchivePerGen;
    private static ArrayList<Map<String, Integer>> methodCount;
    private static ArrayList<Map<String, Integer>> methodInArchiveCount;

    private Map<String, MethodCoverage> internalCoverage;

    private Collector() {
        this.internalCoverage = new HashMap<>();

        archive = new ArrayList<>();
        statusCodesTotal = new HashMap<>();
        statusCodesArchive = new HashMap<>();

        statusCodesPerGen = new ArrayList<>();
        statusCodesInArchivePerGen = new ArrayList<>();
        methodCount = new ArrayList<>();
        methodInArchiveCount = new ArrayList<>();

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

    public void countStatusCodesPerGen(Individual ind, int generation) {
        if (statusCodesPerGen.size() != generation) {
            statusCodesPerGen.add(new HashMap<>());
        }

        // Count status codes per generation
        int currentStatusCode = ind.getResponseObject().getResponseCode();
        if (statusCodesPerGen.get(generation-1).containsKey(currentStatusCode)) {
            statusCodesPerGen.get(generation-1).put(currentStatusCode, statusCodesPerGen.get(generation-1).get(currentStatusCode)+1);
        } else {
            statusCodesPerGen.get(generation-1).put(currentStatusCode, 1);
        }
    }

    public void countStatusCodesInArchivePerGen(Individual ind, int generation) {
        if (statusCodesInArchivePerGen.size() != generation) {
            statusCodesInArchivePerGen.add(new HashMap<>());
        }

        // Count status codes in the archive per generation
        int currentStatusCode = ind.getResponseObject().getResponseCode();
        if (statusCodesInArchivePerGen.get(generation-1).containsKey(currentStatusCode)) {
            statusCodesInArchivePerGen.get(generation-1).put(currentStatusCode, statusCodesInArchivePerGen.get(generation-1).get(currentStatusCode)+1);
        } else {
            statusCodesInArchivePerGen.get(generation-1).put(currentStatusCode, 1);
        }
    }

    public void countMethodPerGen(Individual ind, int generation) {
        if (methodCount.size() != generation) {
            methodCount.add(new HashMap<>());
        }

        // Count methods per generation
        String currentMethod = ind.getDna().get(ind.getDna().size()-1).getApiMethod();
        if (methodCount.get(generation-1).containsKey(currentMethod)) {
            methodCount.get(generation-1).put(currentMethod, methodCount.get(generation-1).get(currentMethod)+1);
        } else {
            methodCount.get(generation-1).put(currentMethod, 1);
        }
    }

    public void countMethodInArchivePerGen(Individual ind, int generation) {
        if (methodInArchiveCount.size() != generation) {
            methodInArchiveCount.add(new HashMap<>());
        }

        // Count methods per generation
        String currentMethod = ind.getDna().get(ind.getDna().size()-1).getApiMethod();
        if (methodInArchiveCount.get(generation-1).containsKey(currentMethod)) {
            methodInArchiveCount.get(generation-1).put(currentMethod, methodInArchiveCount.get(generation-1).get(currentMethod)+1);
        } else {
            methodInArchiveCount.get(generation-1).put(currentMethod, 1);
        }
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


    public ArrayList<Map<Integer, Integer>> getStatusCodesPerGen() {
        return statusCodesPerGen;
    }

    public ArrayList<Map<Integer, Integer>> getStatusCodesInArchivePerGen() {
        return statusCodesInArchivePerGen;
    }

    public ArrayList<Map<String, Integer>> getMethodCount() {
        return methodCount;
    }

    public ArrayList<Map<String, Integer>> getMethodInArchiveCount() {
        return methodInArchiveCount;
    }
}
