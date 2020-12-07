package search.genes;

import search.openRPC.Specification;

import static util.RandomSingleton.getRandom;

public class BooleanGene extends LeaveGene<Boolean> {

    public BooleanGene(String key, Boolean value) {
        super(key, value);
    }

    @Override
    public Gene mutate(Specification specification) {
        if (getRandom().nextDouble() < 0.9) {
            return new BooleanGene(this.getKey(), !this.getValue());
        } else {
            // change gene (e.g. no longer boolean but string)
            return specification.getRandomOption();
        }
    }

    @Override
    public Gene<Boolean> copy() {
        return new BooleanGene(this.getKey(), this.getValue());
    }
}

