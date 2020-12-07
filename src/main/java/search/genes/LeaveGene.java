package search.genes;

/**
 * LeaveGene represents one single gene (can be BooleanGene, LongGene or StringGene).
 * @param <T>
 */
public abstract class LeaveGene<T> extends Gene<T> {

    private T value;

    public LeaveGene(String key, T value) {
        super(key);
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @Override
    public T toJSON() {
        return value;
    }
}

