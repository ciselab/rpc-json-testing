package search.genes;

import org.json.JSONObject;
import search.Generator;
import search.openRPC.SchemaSpecification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static util.RandomSingleton.getRandom;
import static util.RandomSingleton.getRandomIndex;

/**
 * JSONObjectGene represents the parameters (keys and corresponding values) of a method.
 */
public class JSONObjectGene extends NestedGene<JSONObject> {

    private Map<StringGene, Gene> children;

    public JSONObjectGene(SchemaSpecification schema) {
        super(schema);
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

        // if there are no parameters/children and there is no schema, do not do anything.
        if (getSchema() == null && keys.isEmpty()) {
            return clone;
        }

        // if there is no schema it means this is the main parameters object
        if (getSchema() == null) {
            // TODO this always mutates exactly ONE CHILD (but we might want to mutate more later)

            int index = getRandom().nextInt(keys.size());
            StringGene key = keys.get(index);

            Gene child = clone.children.get(key);
            clone.addChild(key, child.mutate(generator));

            return clone;
        }

        // if the JSONObjectGene has no children, it will stay empty.
        if (getSchema().getChildSchemaSpecification().keySet().isEmpty()) {
            return clone;
        }

        Map<String, List<SchemaSpecification>> children = getSchema().getChildSchemaSpecification();

        // TODO this always mutate exactly ONE CHILD (but we might want to mutate more)
        double choice = getRandom().nextDouble();

        if ((choice <= 0.1 || keys.isEmpty()) && clone.addRandomNonRequiredChild(generator)) {
            return clone;
        } else if (choice <= 0.2 && clone.removeRandomChild()) {
            return clone;
        } else if (choice <= 0.4 && !keys.isEmpty()) {
            // replace random child
            int index = getRandomIndex(keys);
            StringGene key = keys.get(index);

            List<SchemaSpecification> options = children.get(key.getValue());

            clone.addChild(key, generator.generateValueGene(options.get(getRandomIndex(options))));
        } else if (!keys.isEmpty()) {
            // mutate random child
            int index = getRandom().nextInt(keys.size());
            StringGene key = keys.get(index);

            Gene child = clone.children.get(key);
            clone.addChild(key, child.mutate(generator));
        }

        return clone;
    }
    
    /**
     * Add one of the missing non-required children of the JSONObjectGene.
     */
    public boolean addRandomNonRequiredChild(Generator generator) {
        Set<String> nonRequiredKeys = new HashSet<>(this.getSchema().getChildSchemaSpecification().keySet());
        nonRequiredKeys.removeAll(this.getSchema().getRequiredKeys());
        for (StringGene child : this.children.keySet()) {
            nonRequiredKeys.remove(child.getValue());
        }

        if (nonRequiredKeys.isEmpty()) {
            return false;
        }

        int index = getRandomIndex(nonRequiredKeys);
        String childKey = (String) nonRequiredKeys.toArray()[index];
        List<SchemaSpecification> options = this.getSchema().getChildSchemaSpecification().get(childKey);

        Gene newChild = generator.generateValueGene(options.get(getRandomIndex(options)));
        this.addChild(new StringGene(null, childKey), newChild);
        return true;
    }

    /**
     * Remove one of the (required or non-required) children of the JSONObjectGene.
     */
    public boolean removeRandomChild() {
        Set<String> usedKeys = new HashSet<>();

        // Add all children
        for (StringGene child : this.children.keySet()) {
            usedKeys.add(child.getValue());
        }

        // Remove required children from the set (containing child to be removed) from with a certain probability
//        if (getRandom().nextDouble() > 0.75) {
//            usedKeys.removeAll(this.getSchema().getRequiredKeys());
//        }

        // If there are no keys, there are no children that can be removed.
        if (usedKeys.isEmpty()) {
            return false;
        }
        int index = getRandomIndex(usedKeys);
        String childKey = (String) usedKeys.toArray()[index];

        for (StringGene key : children.keySet()) {
            if (key.getValue().equals(childKey)) {
                this.children.remove(key);
                return true;
            }
        }

        throw new RuntimeException("This should not be possible!");
    }

    @Override
    public JSONObjectGene copy() {
        JSONObjectGene clonedGene = new JSONObjectGene(getSchema());
        for (StringGene gene : this.children.keySet()) {
            clonedGene.addChild(gene.copy(), this.children.get(gene).copy());
        }
        return clonedGene;
    }
}
