package search.genes;

import org.json.JSONArray;
import search.Generator;
import openRPC.SchemaSpecification;
import util.Configuration;

import java.util.ArrayList;
import java.util.List;

import static util.RandomSingleton.getRandom;

/**
 * ArrayGene represents the genes in an individual (the parameters of a method).
 */
public class ArrayGene extends NestedGene<JSONArray> {

    private List<Gene> children;

    public ArrayGene(SchemaSpecification schema) {
        super(schema);
        this.children = new ArrayList<>();
    }

    public void addChild(Gene gene) {
        this.children.add(gene);
    }

    @Override
    public boolean hasChildren() {
        return !children.isEmpty();
    }

    @Override
    public List<Gene> getChildren() {
        return children;
    }

    @Override
    public JSONArray toJSON() {
        JSONArray jsonArray = new JSONArray();

        for (Gene child : this.getChildren()) {
            jsonArray.put(child.toJSON());
        }

        return jsonArray;
    }

    @Override
    public ArrayGene mutate(Generator generator) {
        ArrayGene clone = this.copy();

        // If there is no schema it means this is the main parameters array
        if (getSchema() == null) {
            for (int i = 0; i < clone.children.size(); i++) {
                if (util.RandomSingleton.getRandomBool(1 / clone.children.size())) {
                    Gene child = clone.children.get(i);
                    child = child.mutate(generator);

                    // Make sure child is not another array (stripValues does not support arrays in arrays)
                    while (child instanceof ArrayGene) {
                        child = child.mutate(generator);
                    }
                    clone.children.set(i, child);
                }
            }
            return clone;
        }

        // If the array is always empty according to specification, it stays empty.
        // TODO or maybe it should be filled with something?
//        if (getSchema().getArrayItemSchemaSpecification().isEmpty()) {
//            return clone;
//        }

        // TODO we only take the first specification for now but this could be an anyof/oneof so we should take into account that we have to change the type of all child genes
        List<SchemaSpecification> children = getSchema().getArrayItemSchemaSpecification();

        // Mutate elements of the array
        for (int i = 0; i < clone.children.size(); i++) {
            if (util.RandomSingleton.getRandomBool(1 / clone.children.size())) {
                double choice = getRandom().nextDouble();

                if (clone.children.size() < this.getSchema().getLength() && (clone.children.size() == 0 || choice <= Configuration.ADD_ELEMENT_PROB)) {
                    // Add a child (change gene into a different type or generate new value)
                    Gene child = generator.generateValueGene(children.get(i));
                    while (child instanceof ArrayGene) {
                        child = generator.generateValueGene(children.get(i));
                    }
                    clone.children.add(i, child);
//                    i += 1;
                } else if (clone.children.size() > 1 && choice <= (Configuration.REMOVE_ELEMENT_PROB + Configuration.ADD_ELEMENT_PROB)) {
                    // Remove a child
                    clone.children.remove(i);
                    i -= 1;
                } else {
                    // Mutate a child (or more)
                    Gene child = clone.children.get(i);
                    child = child.mutate(generator);
                    // Make sure child is not another array (stripValues does not support arrays in arrays)
                    while (child instanceof ArrayGene) {
                        child = child.mutate(generator);
                    }
                    clone.children.set(i, child);
                }
            }
        }
        return clone;
    }

    @Override
    public ArrayGene copy() {
        ArrayGene clone = new ArrayGene(getSchema());

        for (Gene child : getChildren()) {
            clone.addChild(child.copy());
        }

        return clone;
    }
}
