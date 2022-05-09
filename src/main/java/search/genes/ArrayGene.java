package search.genes;

import org.json.JSONArray;
import search.Generator;
import openRPC.SchemaSpecification;
import util.RandomSingleton;
import util.config.Configuration;

import java.util.ArrayList;
import java.util.List;

import static util.RandomSingleton.getRandom;
import static util.RandomSingleton.getRandomBool;

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
    public Gene mutate(Generator generator) {
        ArrayGene clone = this.copy();

        // If there is no schema it means this is the main parameters array
        if (getSchema() == null) {
            int index = RandomSingleton.getRandomIndex(clone.children);

            Gene child = clone.children.get(index);
            child = child.mutate(generator);

            // Make sure child is not another array (stripValues does not support arrays in arrays)
            while (child instanceof ArrayGene) {
                child = child.mutate(generator);
            }
            clone.children.set(index, child);
            return clone;
        }

        if (!getRandomBool(Configuration.MUTATION_INSTEAD_OF_GENERATION)) {
            return this.getNewGene(generator);
        }


        // If the array is always empty according to specification, it stays empty.
        // TODO or maybe it should be filled with something random
        if (getSchema().getArrayItemSchemaSpecification().isEmpty()) {
            return clone;
        }

        // TODO we only take the first specification for now but this could be an anyof/oneof so we should take into account that we have to change the type of all child genes
        List<SchemaSpecification> children = getSchema().getArrayItemSchemaSpecification();

        // Mutate elements of the array

//        if (clone.children.size() == 0 && clone.children.size() < this.getSchema().getLength()) {
//            return clone;
//        }
        int index = clone.children.size() == 0 ? -1 : RandomSingleton.getRandomIndex(clone.children);

        double choice = getRandom().nextDouble();

        if (clone.children.size() == 0 || choice <= Configuration.ADD_ELEMENT_PROB) {
            int schemaIndex = RandomSingleton.getRandomIndex(children);
            SchemaSpecification schema = children.get(schemaIndex);

            // Add a child (change gene into a different type or generate new value)
            Gene child = generator.generateValueGene(schema);
            while (child instanceof ArrayGene) {
                child = generator.generateValueGene(schema);
            }
            if (index == -1) {
                clone.children.add(child);
            } else {
                clone.children.add(index, child);
            }
        } else if (clone.children.size() > 1 && choice <= (Configuration.REMOVE_ELEMENT_PROB + Configuration.ADD_ELEMENT_PROB)) {
            // Remove a child
            clone.children.remove(index);
        } else {
            // Mutate a child (or more)
            Gene child = clone.children.get(index);
            child = child.mutate(generator);
            // Make sure child is not another array (stripValues does not support arrays in arrays)
            while (child instanceof ArrayGene) {
                child = child.mutate(generator);
            }
            clone.children.set(index, child);
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
