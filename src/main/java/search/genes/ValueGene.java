package search.genes;

import search.Generator;
import openRPC.SchemaSpecification;

/**
 * ValueGene represents one single gene (can be BooleanGene, LongGene or StringGene).
 * @param <T>
 */
public abstract class ValueGene<T> extends Gene<T> {

    private T value;

    public ValueGene(SchemaSpecification schema, T value) {
        super(schema);
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
        return generator.generateValueGene(getSchema());
    }

    @Override
    public String toString() {
        return "ValueGene{" +
            "value=" + value +
            '}';
    }
}

