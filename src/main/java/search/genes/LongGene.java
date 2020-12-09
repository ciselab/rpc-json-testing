package search.genes;

import search.openRPC.Specification;

public class LongGene extends LeaveGene<Long> {
    public LongGene(String key, Long value) {
        super(key, value);
    }

    @Override
    public Gene mutate(Specification specification) {
        throw new IllegalStateException("UNINPLEMENTED");
//        return null;
    }

    @Override
    public Gene<Long> copy() {
        return new LongGene(getKey(), this.getValue());
    }
}
