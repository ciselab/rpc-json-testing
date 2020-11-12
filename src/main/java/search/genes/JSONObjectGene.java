package search.genes;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSONObjectGene represents the parameters (keys and corresponding values) of a method.
 */
public class JSONObjectGene extends NestedGene<JSONObject> {

    private Map<StringGene, Gene> children;

    public JSONObjectGene() {
        this.children = new HashMap<>();
    }

    public void addChild(StringGene key, Gene value) {
        this.children.put(key, value);
    }

    @Override
    public boolean hasChildren() {
        return !children.isEmpty();
    }

    @Override
    public List<Gene> getChildren() {
        return (List<Gene>) children.values();
    }

    @Override
    public JSONObject toJSON() {
        JSONObject object = new JSONObject();

        for (StringGene key : children.keySet()) {
            object.put(key.toJSON(), children.get(key).toJSON());
        }

        return object;
    }
}
