package search.genes;

import search.Generator;
import openRPC.SchemaSpecification;
import util.config.Configuration;

public class BooleanGene extends ValueGene<Boolean> {

    public BooleanGene(SchemaSpecification schema, Boolean value) {
        super(schema, value);
    }

    @Override
    public Gene mutate(Generator generator) {
        if (util.RandomSingleton.getRandomBool(Configuration.MUTATION_INSTEAD_OF_GENERATION)) {
            return new BooleanGene(this.getSchema(), !this.getValue());
        } else {
            // Change gene into an entirely new value by generating new value
            Gene gene = getNewGene(generator);

            // Enforce a change (i.e. not the exact same boolean)
            if (gene.toJSON().equals(this.toJSON())) {
                return new BooleanGene(this.getSchema(), !this.getValue());
            }

            return gene;
        }
    }

    @Override
    public Gene<Boolean> copy() {
        return new BooleanGene(this.getSchema(), this.getValue());
    }
}

