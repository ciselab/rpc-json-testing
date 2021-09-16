package search.genes;

import org.json.JSONObject;
import search.Generator;
import search.openRPC.SchemaSpecification;
import util.Configuration;

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
            throw new IllegalStateException("Value cannot be null!");
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

        // If there are no parameters/children and there is no schema, do not do anything.
        if (getSchema() == null && keys.isEmpty()) {
            return clone;
        }

        // If there is no schema it means this is the main parameters object.
        if (getSchema() == null) {
            for (int i = 0; i < keys.size(); i++) {
                if (util.RandomSingleton.getRandomBool(1 / keys.size())) {
                    StringGene key = keys.get(i);
                    Gene child = clone.children.get(key);
                    clone.addChild(key, child.mutate(generator));
                }
            }
            return clone;
        }

        // If the JSONObjectGene has no children, it will stay empty.
        if (getSchema().getChildSchemaSpecification().keySet().isEmpty()) {
            return clone;
        }

        Map<String, List<SchemaSpecification>> children = getSchema().getChildSchemaSpecification();

        for (int i = 0; i < keys.size(); i++) {
            if (util.RandomSingleton.getRandomBool(1 / keys.size())) {
                double choice = getRandom().nextDouble();

                if ((choice <= Configuration.ADD_NONREQUIRED_CHILD_PROB || keys.isEmpty()) && clone.addChild(generator)) {
                    // Add missing child/parameter to the object
                    i -= 1;
                } else if (choice <= (Configuration.ADD_NONREQUIRED_CHILD_PROB + Configuration.REMOVE_CHILD_PROB) && clone.removeChild(keys.get(i).getValue())) {
                    // Remove child/parameter from the object
                } else if (choice <= (Configuration.ADD_NONREQUIRED_CHILD_PROB + Configuration.REMOVE_CHILD_PROB + (1 - Configuration.MUTATION_INSTEAD_OF_GENERATION)) && !keys.isEmpty()) {
                    // Replace value of child/parameter with a newly generated one
                    StringGene key = keys.get(i);
                    List<SchemaSpecification> options = children.get(key.getValue());
                    clone.addChild(key, generator.generateValueGene(options.get(getRandomIndex(options))));
                } else if (!keys.isEmpty()) {
                    // Mutate child/parameter
                    StringGene key = keys.get(i);
                    Gene child = clone.children.get(key);
                    clone.addChild(key, child.mutate(generator));
                }
            }
        }
        return clone;
    }
    
    /**
     * Add one of the missing children of the JSONObjectGene.
     */
    public boolean addChild(Generator generator) {
        Set<String> missingKeys = new HashSet<>(this.getSchema().getChildSchemaSpecification().keySet());

        for (StringGene child : this.children.keySet()) {
            missingKeys.remove(child.getValue());
        }

        if (missingKeys.isEmpty()) {
            return false;
        }

        int index = getRandomIndex(missingKeys);
        String childKey = (String) missingKeys.toArray()[index];
        List<SchemaSpecification> options = this.getSchema().getChildSchemaSpecification().get(childKey);

        Gene newChild = generator.generateValueGene(options.get(getRandomIndex(options)));
        this.addChild(new StringGene(null, childKey), newChild);
        return true;
    }

    /**
     * Remove one of the children of the JSONObjectGene.
     */
    public boolean removeChild(String childKey) {
        for (StringGene key : children.keySet()) {
            if (key.getValue().equals(childKey)) {
                this.children.remove(key);
                return true;
            }
        }

        return false;
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
