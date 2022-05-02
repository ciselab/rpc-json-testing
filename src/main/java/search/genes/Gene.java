package search.genes;

import org.json.JSONObject;
import search.Generator;
import openRPC.SchemaSpecification;

import java.util.List;
import java.util.Map;

public abstract class Gene<T> {

    private SchemaSpecification schema;

    public Gene(SchemaSpecification schema) {
        this.schema = schema;
    }

    public abstract T toJSON(Map<MethodGene, JSONObject> previousResponse);

    public abstract Gene mutate(Generator generator);

    public abstract Gene<T> copy();

    public SchemaSpecification getSchema() {
        return schema;
    }

    public abstract boolean hasChildren();

    public abstract List<Gene> getChildren();
}
