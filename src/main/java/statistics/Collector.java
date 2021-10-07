package statistics;

import search.Individual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Collector {

    private static Collector instance;
    private static Map<String, Integer> requestArchive;
    private static Archive archive;
    private static int generation;

    private static Map<Integer, Integer> statusCodesTotal;
    private static Map<Integer, Integer> statusCodesArchive;
    private static ArrayList<Map<Integer, Integer>> statusCodesPerGen;
    private static ArrayList<Map<Integer, Integer>> statusCodesInArchivePerGen;

    private static Map<String, Integer> methodCountTotal;
    private static Map<String, Integer> methodCountArchive;
    private static ArrayList<Map<String, Integer>> methodCountPerGen;
    private static ArrayList<Map<String, Integer>> methodInArchiveCount;

    private Map<String, MethodCoverage> internalCoverage;

    private Collector() {
        generation = 0;
        archive = new Archive();

        this.internalCoverage = new HashMap<>();

        statusCodesTotal = new HashMap<>();
        statusCodesArchive = new HashMap<>();
        statusCodesPerGen = new ArrayList<>();
        statusCodesInArchivePerGen = new ArrayList<>();

        methodCountTotal = new HashMap<>();
        methodCountArchive = new HashMap<>();
        methodCountPerGen = new ArrayList<>();
        methodInArchiveCount = new ArrayList<>();

        requestArchive = new HashMap<>();
    }

    public static Collector getCollector() {
        if (instance == null) {
            instance = new Collector();
        }
        return instance;
    }

    public void nextGeneration() {
        generation += 1;
    }

    public void addToArchive(String key, Individual ind) {
        boolean newMemberInArchive = archive.putWithSecondaryObjectives(key, ind);

        // TODO this method is always called
        if (newMemberInArchive) {
            // Update status codes occurrences statistics
            if (!statusCodesArchive.containsKey(ind.getResponseObject().getResponseCode())) {
                statusCodesArchive.put(ind.getResponseObject().getResponseCode(), 0);
            }
            statusCodesArchive.put(ind.getResponseObject().getResponseCode(), statusCodesArchive.get(ind.getResponseObject().getResponseCode()) + 1);

            // Update API method occurrences statistics
            String currentMethod = ind.getDna().get(ind.getDna().size() - 1).getApiMethod();
            if (!methodCountArchive.containsKey(currentMethod)) {
                methodCountArchive.put(currentMethod, 0);
            }
            methodCountArchive.put(currentMethod, methodCountArchive.get(currentMethod) + 1);
        }
    }

    public void collect(String method, Integer status, String structure, String vector) {
        if (!internalCoverage.containsKey(method)) {
            internalCoverage.put(method, new MethodCoverage());
        }

        internalCoverage.get(method).statuses.add(status);

        if (!internalCoverage.get(method).structures.containsKey(structure)) {
            internalCoverage.get(method).structures.put(structure, new HashSet<>());
        }

        internalCoverage.get(method).structures.get(structure).add(vector);
    }

    public void countStatusCodes(Individual ind) {
        if (statusCodesPerGen.size() != generation) {
            statusCodesPerGen.add(new HashMap<>());
        }

        // Count status codes per generation
        int currentStatusCode = ind.getResponseObject().getResponseCode();
        if (!statusCodesPerGen.get(generation-1).containsKey(currentStatusCode)) {
            statusCodesPerGen.get(generation-1).put(currentStatusCode, 0);
        }
        statusCodesPerGen.get(generation-1).put(currentStatusCode, statusCodesPerGen.get(generation-1).get(currentStatusCode)+1);

        // Count status codes total
        if (!statusCodesTotal.containsKey(currentStatusCode)) {
            statusCodesTotal.put(currentStatusCode, 0);
        }
        statusCodesTotal.put(currentStatusCode, statusCodesTotal.get(currentStatusCode)+1);
    }

    public void countStatusCodesInArchivePerGen(Individual ind) {
        if (statusCodesInArchivePerGen.size() != generation) {
            statusCodesInArchivePerGen.add(new HashMap<>());
        }

        // Count status codes in the archive per generation
        int currentStatusCode = ind.getResponseObject().getResponseCode();
        if (!statusCodesInArchivePerGen.get(generation-1).containsKey(currentStatusCode)) {
            statusCodesInArchivePerGen.get(generation-1).put(currentStatusCode, 0);
        }
        statusCodesInArchivePerGen.get(generation-1).put(currentStatusCode, statusCodesInArchivePerGen.get(generation-1).get(currentStatusCode)+1);
    }

    public void countMethods(Individual ind) {
        if (methodCountPerGen.size() != generation) {
            methodCountPerGen.add(new HashMap<>());
        }

        // Count methods per generation
        String currentMethod = ind.getDna().get(ind.getDna().size()-1).getApiMethod();
        if (!methodCountPerGen.get(generation-1).containsKey(currentMethod)) {
            methodCountPerGen.get(generation-1).put(currentMethod, 0);
        }
        methodCountPerGen.get(generation-1).put(currentMethod, methodCountPerGen.get(generation-1).get(currentMethod)+1);

        // Count methods total
        if (!methodCountTotal.containsKey(currentMethod)) {
            methodCountTotal.put(currentMethod, 0);
        }
        methodCountTotal.put(currentMethod, methodCountTotal.get(currentMethod)+1);

    }

    public void countMethodInArchivePerGen(Individual ind) {
        if (methodInArchiveCount.size() != generation) {
            methodInArchiveCount.add(new HashMap<>());
        }

        // Count methods per generation
        String currentMethod = ind.getDna().get(ind.getDna().size()-1).getApiMethod();
        if (!methodInArchiveCount.get(generation-1).containsKey(currentMethod)) {
            methodInArchiveCount.get(generation-1).put(currentMethod, 0);
        }
        methodInArchiveCount.get(generation-1).put(currentMethod, methodInArchiveCount.get(generation-1).get(currentMethod)+1);
    }

    public Map<String, MethodCoverage> getInternalCoverage() {
        return internalCoverage;
    }

    public Archive getArchive() {
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

    public ArrayList<Map<String, Integer>> getMethodCountPerGen() {
        return methodCountPerGen;
    }

    public ArrayList<Map<String, Integer>> getMethodInArchiveCount() {
        return methodInArchiveCount;
    }

    public Map<String, Integer> getMethodCountTotal() {
        return methodCountTotal;
    }

    public Map<String, Integer> getMethodCountArchive() {
        return methodCountArchive;
    }

    public int getGeneration() {
        return generation;
    }

    public void addRequest(String request) {
//        // System.out.println(request);
        if (!requestArchive.containsKey(request)) {
            requestArchive.put(request, 0);
        }
        requestArchive.put(request, requestArchive.get(request) + 1);
    }

    public Map<String, Integer> getRequestArchive() {
        return requestArchive;
    }
}
