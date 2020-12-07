package search.genes;

import search.openRPC.Specification;

import static util.RandomSingleton.getRandom;

public class StringGene extends LeaveGene<String> {

    public StringGene(String key, String value) {
        super(key, value);
    }

    @Override
    public Gene mutate(Specification specification) {
        if (getKey().length() == 0 || getRandom().nextDouble() < 0.9) {
            // TODO change value, currently it just copies
            return new StringGene(this.getKey(), this.getValue());
        } else {
            // change gene
            return specification.getRandomOption();
        }
    }

    @Override
    public StringGene copy() {
        return new StringGene(this.getKey(), getValue());
    }
}
