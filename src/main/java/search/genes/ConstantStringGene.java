package search.genes;

import openRPC.SchemaSpecification;
import search.Generator;
import util.config.Configuration;

import static util.RandomSingleton.getRandom;
import static util.RandomSingleton.getRandomBool;

public class ConstantStringGene extends ValueGene<String> {

    private String constant;

    public ConstantStringGene(SchemaSpecification schema, String constant, int value) {
        super(schema, "" + value);
        this.constant = constant;
    }

    @Override
    public Gene mutate(Generator generator) {
        if (getRandomBool(Configuration.MUTATION_INSTEAD_OF_GENERATION)) {
            int value = Integer.parseInt(getValue());

            Integer mutatedValue = value + (getRandom().nextBoolean() ? 1 : -1);
            mutatedValue = Math.min(mutatedValue, Configuration.NUMBER_OF_ACCOUNTS - 1);
            mutatedValue = Math.max(mutatedValue, 0);

            return new ConstantStringGene(this.getSchema(), this.constant, mutatedValue);
        } else {
            // Change gene into an entirely new value by generating new value
            return getNewGene(generator);
        }
    }


    @Override
    public String toJSON() {
        return this.constant + this.getValue();
    }

    @Override
    public ConstantStringGene copy() {
        int value = Integer.parseInt(getValue());

        return new ConstantStringGene(this.getSchema(), this.constant, value);
    }

}
