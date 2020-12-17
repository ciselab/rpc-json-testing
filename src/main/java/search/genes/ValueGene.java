package search.genes;

import search.Generator;

/**
 * ValueGene represents one single gene (can be BooleanGene, LongGene or StringGene).
 * @param <T>
 */
public abstract class ValueGene<T> extends Gene<T> {

    private String specPath;
    private T value;

    public ValueGene(String specPath, T value) {
        this.specPath = specPath;
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @Override
    public T toJSON() {
        return value;
    }

    public Gene getNewGene(Generator generator) {
        return generator.generateValueGene(getSpecPath());
    }

    public String getSpecPath() {
        return specPath;
    }
}

