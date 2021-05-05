package util;

public class Triple<T, S, R> {

    private T t;
    private S s;
    private R r;

    public Triple(T t, S s, R r) {
        this.t = t;
        this.s = s;
        this.r = r;
    }

    public T getKey() {
        return t;
    }
    public S getValue() {
        return s;
    }
    public R getValue2() { return r; }
}