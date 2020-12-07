package search.genes;

import org.json.JSONObject;
import search.openRPC.Specification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static util.RandomSingleton.getRandom;

/**
 * JSONObjectGene represents the parameters (keys and corresponding values) of a method.
 */
public class JSONObjectGene extends NestedGene<JSONObject> {

    private Map<StringGene, Gene> children;

    public JSONObjectGene(String key) {
        super(key);
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
        return new ArrayList<>(children.values());
    }

    @Override
    public JSONObject toJSON() {
        JSONObject object = new JSONObject();
        for (StringGene key : children.keySet()) {
            object.put(key.toJSON(), children.get(key).toJSON());
        }
        return object;
    }

    @Override
    public Gene mutate(Specification specification) {
        JSONObjectGene clone = this.copy();

        List<StringGene> keys = new ArrayList<>(clone.children.keySet());

        if (keys.size() == 0) {
            return clone;
        }

        int index = getRandom().nextInt(keys.size());
        StringGene key = keys.get(index);

        // TODO this always mutate exactly ONE CHILD (but we might want to mutate more)
        Gene child = clone.children.get(key);
        clone.children.put(key, child.mutate(specification.getChild(child)));

        return clone;
    }

    @Override
    public JSONObjectGene copy() {
        JSONObjectGene clonedGene = new JSONObjectGene(this.getKey());
        for (Gene gene : this.getChildren()) {
            clonedGene.getChildren().add(gene);
        }
        return clonedGene;
    }
}
