package search.genes;

public abstract class LeaveGene<T> implements Gene<T> {

    private T value;

    public LeaveGene(T value) {
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

