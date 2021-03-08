package search.genes;

import search.openRPC.SchemaSpecification;

import java.util.ArrayList;
import java.util.List;

/**
 * NestedGene represents multiple genes (can be ArrayGene or JSONObjectGene).
 * @param <T>
 */
public abstract class NestedGene<T> extends Gene<T> {

    public NestedGene(SchemaSpecification schema) {
        super(schema);
    }

    public abstract boolean hasChildren();

    public abstract List<Gene> getChildren();
}
