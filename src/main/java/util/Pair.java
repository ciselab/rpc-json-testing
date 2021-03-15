package util;

public class Pair<T, S> {

    private T t;
    private S s;

    public Pair(T t, S s) {
        this.t = t;
        this.s = s;
    }

    public T getKey() {
        return t;
    }
    public S getValue() {
        return s;
    }
}