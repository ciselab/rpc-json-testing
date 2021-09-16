package statistics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MethodCoverage {
    public Set<Integer> statusses;
    public Map<String, Set<String>> structures;

    public MethodCoverage() {
        this.statusses = new HashSet<>();
        this.structures = new HashMap<>();
    }
}
