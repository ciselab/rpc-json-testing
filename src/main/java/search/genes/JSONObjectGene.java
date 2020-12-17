package search.genes;

import org.json.JSONObject;
import search.Generator;
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

    public JSONObjectGene() {
        this.children = new HashMap<>();
    }

    public void addChild(StringGene key, Gene value) {
        if (value == null) {
            throw new IllegalStateException("Value cannot be null!!!");
        }

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
    public Gene mutate(Generator generator) {
        JSONObjectGene clone = this.copy();

        List<StringGene> keys = new ArrayList<>(clone.children.keySet());

        if (keys.size() == 0) {
            return clone;
        }

        int index = getRandom().nextInt(keys.size());
        StringGene key = keys.get(index);

        // TODO add or remove a gene based on the specification

        // TODO this always mutate exactly ONE CHILD (but we might want to mutate more)

        Gene child = clone.children.get(key);
        clone.addChild(key, child.mutate(generator));

        return clone;
    }

    @Override
    public JSONObjectGene copy() {
        JSONObjectGene clonedGene = new JSONObjectGene();
        for (StringGene gene : this.children.keySet()) {
            clonedGene.addChild(gene.copy(), this.children.get(gene).copy());
        }
        return clonedGene;
    }
}
