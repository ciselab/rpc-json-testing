package search.genes;

import org.json.JSONArray;
import search.Generator;
import search.openRPC.SchemaSpecification;

import java.util.ArrayList;
import java.util.List;

import static util.RandomSingleton.getRandom;

/**
 * ArrayGene represents the genes in an individual (the parameters of a method).
 */
public class ArrayGene extends NestedGene<JSONArray> {

    private List<Gene> children;
    final private double REMOVE_ELEMENT_PROB = 0.1;
    final private double ADD_ELEMENT_PROB = 0.1;

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
            clone.children.set(index, child.mutate(generator));
            return clone;
        }

        // if the array is always empty according to specification, it stays empty.
        if (getSchema().getArrayItemSchemaSpecification().isEmpty()) {
            return clone;
        }

        List<SchemaSpecification> children = getSchema().getArrayItemSchemaSpecification();
        // TODO we only take the first specification for now but this could be an anyof/oneof so we should take into account that we have to change the type of all child genes

        double choice = getRandom().nextDouble();

        if (clone.children.size() < this.getSchema().getLength() && (clone.children.size() == 0 || choice <= ADD_ELEMENT_PROB)) {
            // add a child
            clone.children.add(generator.generateValueGene(children.get(0)));
        } else if (clone.children.size() > 1 && choice <= (REMOVE_ELEMENT_PROB + ADD_ELEMENT_PROB)) {
            // remove a child
            int index = getRandom().nextInt(clone.children.size());
            clone.children.remove(index);

        } else {
            // TODO this always mutate exactly ONE CHILD (but we might want to mutate more)
            int index = getRandom().nextInt(clone.children.size());
            Gene child = clone.children.get(index);
            clone.children.set(index, child.mutate(generator));
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
