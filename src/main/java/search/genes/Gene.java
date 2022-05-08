package search.genes;

import search.Generator;
import openRPC.SchemaSpecification;
import util.config.Configuration;

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

    public Gene getNewGene(Generator generator) {
        return generator.generateValueGene(getSchema(), Configuration.TYPE_CHANGES);
    }
}
