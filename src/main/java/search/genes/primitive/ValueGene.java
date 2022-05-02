package search.genes.primitive;

import org.json.JSONObject;
import search.Generator;
import openRPC.SchemaSpecification;
import search.genes.Gene;
import search.genes.MethodGene;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ValueGene represents one single gene (can be BooleanGene, LongGene or StringGene).
 * @param <T>
 */
public abstract class ValueGene<T> extends Gene<T> {

    private T value;

    public ValueGene(SchemaSpecification chosenSchema, T value) {
        super(chosenSchema);
        this.value = value;
    }


    public T getValue() {
        return value;
    }

    @Override
    public T toJSON(Map<MethodGene, JSONObject> previousResponse) {
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

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public List<Gene> getChildren() {
        return new ArrayList<>();
    }
}

