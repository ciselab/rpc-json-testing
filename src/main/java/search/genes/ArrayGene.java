package search.genes;

import org.json.JSONArray;
import search.Generator;
import search.openRPC.Specification;

import java.util.ArrayList;
import java.util.List;

import static util.RandomSingleton.getRandom;

/**
 * ArrayGene represents the genes in an individual (the parameters of a method).
 */
public class ArrayGene extends NestedGene<JSONArray> {

    private List<Gene> children;

    public ArrayGene() {
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


        if (clone.children.size() == 0) {
            // TODO add child by default maybe
        } else {
            int index = getRandom().nextInt(clone.children.size());

            // TODO this always mutate exactly ONE CHILD (but we might want to mutate more)

            Gene child = clone.children.get(index);
            clone.children.set(index, child.mutate(generator));
        }

        return clone;
    }

    @Override
    public ArrayGene copy() {
        ArrayGene clone = new ArrayGene();

        for (Gene child : getChildren()) {
            clone.addChild(child.copy());
        }

        return clone;
    }
}
