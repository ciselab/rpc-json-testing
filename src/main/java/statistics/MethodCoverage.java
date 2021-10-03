package statistics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MethodCoverage {
    public Set<Integer> statuses;
    public Map<String, Set<String>> structures;

    public MethodCoverage() {
        this.statuses = new HashSet<>();
        this.structures = new HashMap<>();
    }
}
