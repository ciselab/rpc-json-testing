package search.genes;

import search.Generator;
import openRPC.SchemaSpecification;

public abstract class Gene<T> {

    private SchemaSpecification schema;

    public Gene(SchemaSpecification schema) {
        this.schema = schema;
    }

    abstract T toJSON();

    abstract Gene mutate(Generator generator);

    abstract Gene<T> copy();

    public SchemaSpecification getSchema() {
        return schema;
    }
}
