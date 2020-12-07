package search.genes;

import search.openRPC.Specification;

public abstract class Gene<T> {

    private String key;

    public Gene(String key) {
        this.key = key;
    }

    abstract T toJSON();

    abstract Gene mutate(Specification specification);

    abstract Gene<T> copy();

    public String getKey() {
        return key;
    }
}
