package search.genes;

import org.json.JSONArray;
import search.Generator;
import search.openRPC.SchemaSpecification;
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

        // if there is no schema it means this is the main parameters array
        if (getSchema() == null) {
            // TODO this always mutate exactly ONE CHILD (but we might want to mutate more)
            int index = getRandom().nextInt(clone.children.size());
            Gene child = clone.children.get(index);
            child = child.mutate(generator);

            while (child instanceof ArrayGene) {
                child = child.mutate(generator);
            }

            clone.children.set(index, child);
            return clone;
        }

        // if the array is always empty according to specification, it stays empty.
        if (getSchema().getArrayItemSchemaSpecification().isEmpty()) {
            return clone;
        }

        List<SchemaSpecification> children = getSchema().getArrayItemSchemaSpecification();
        // TODO we only take the first specification for now but this could be an anyof/oneof so we should take into account that we have to change the type of all child genes

        double choice = getRandom().nextDouble();

        if (clone.children.size() < this.getSchema().getLength() && (clone.children.size() == 0 || choice <= Configuration.ADD_ELEMENT_PROB)) {
            // add a child
            Gene child = generator.generateValueGene(children.get(0));

            while (child instanceof ArrayGene) {
                child = generator.generateValueGene(children.get(0));
            }

            clone.children.add(child);
        } else if (clone.children.size() > 1 && choice <= (Configuration.REMOVE_ELEMENT_PROB + Configuration.ADD_ELEMENT_PROB)) {
            // remove a child
            int index = getRandom().nextInt(clone.children.size());
            clone.children.remove(index);

        } else {
            // TODO this always mutate exactly ONE CHILD (but we might want to mutate more)
            int index = getRandom().nextInt(clone.children.size());

            // Choose another gene if the chosen gene is the ACCOUNT tag
            if (clone.children.get(index) instanceof StringGene) {
                if (((StringGene) clone.children.get(index)).getValue().equals("__ACCOUNT__")) {
                    int newIndex = getRandom().nextInt(clone.children.size());
                    while (newIndex == index && clone.children.size() > 1) {
                        newIndex = getRandom().nextInt(clone.children.size());
                    }
                    index = newIndex;
                }
            }

            Gene child = clone.children.get(index);
            child = child.mutate(generator);

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
