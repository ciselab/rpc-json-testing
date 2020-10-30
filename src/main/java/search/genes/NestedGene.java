package search.genes;

import java.util.ArrayList;
import java.util.List;

public abstract class NestedGene<T> implements Gene<T> {

    public NestedGene() {
    }


    public abstract boolean hasChildren();

    public abstract List<Gene> getChildren();
}
