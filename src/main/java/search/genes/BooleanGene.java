package search.genes;

import search.Generator;

import static util.RandomSingleton.getRandom;

public class BooleanGene extends ValueGene<Boolean> {

    public BooleanGene(String key, Boolean value) {
        super(key, value);
    }

    @Override
    public Gene mutate(Generator generator) {
        if (getRandom().nextDouble() < 0.95) {
            System.out.println("booleanGene: boolean changed from " + this.getValue() + " to " + !this.getValue());
            return new BooleanGene(this.getSpecPath(), !this.getValue());
        } else {
//            System.out.println("booleanGene: gene type changed");
            // change gene (e.g. no longer boolean but string, or random boolean)
            return getNewGene(generator);
        }
    }

    @Override
    public Gene<Boolean> copy() {
        return new BooleanGene(this.getSpecPath(), this.getValue());
    }
}

