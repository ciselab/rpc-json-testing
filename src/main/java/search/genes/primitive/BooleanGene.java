package search.genes.primitive;

import search.Generator;
import openRPC.SchemaSpecification;
import search.genes.Gene;
import util.config.Configuration;

public class BooleanGene extends ValueGene<Boolean> {

    public BooleanGene(SchemaSpecification chosenSchema, Boolean value) {
        super(chosenSchema, value);
    }

    @Override
    public Gene mutate(Generator generator) {
        if (util.RandomSingleton.getRandomBool(Configuration.MUTATION_INSTEAD_OF_GENERATION)) {
            return new BooleanGene(this.getSchema(), !this.getValue());
        } else {
            // Change gene into an entirely new value by generating new value
            Gene gene = getNewGene(generator);

//            // Enforce a change (i.e. not the exact same boolean)
//            if (gene.toJSON().toString().equals(this.toJSON().toString())) {
//                return new BooleanGene(this.getSchema(), !this.getValue());
//            }

            return gene;
        }
    }

    @Override
    public Gene<Boolean> copy() {
        return new BooleanGene(this.getSchema(), this.getValue());
    }
}

