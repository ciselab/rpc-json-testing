package search.genes;

import search.Generator;
import search.openRPC.SchemaSpecification;

import static util.RandomSingleton.getRandom;

public class BooleanGene extends ValueGene<Boolean> {

    final private double BOOL_FLIP_PROB = 0.95;

    public BooleanGene(SchemaSpecification schema, Boolean value) {
        super(schema, value);
    }

    @Override
    public Gene mutate(Generator generator) {
        if (getRandom().nextDouble() < BOOL_FLIP_PROB) {
            return new BooleanGene(this.getSchema(), !this.getValue());
        } else {
            // change gene (e.g. no longer boolean but string, or random boolean)
            return getNewGene(generator);
        }
    }

    @Override
    public Gene<Boolean> copy() {
        return new BooleanGene(this.getSchema(), this.getValue());
    }
}

