package se.tink.backend.common.utils;

public final class Triplet<T, U, V> {
    public T first;
    public U second;
    public V third;

    public Triplet(T t, U u, V v) {
        this.first = t;
        this.second = u;
        this.third = v;
    }
}
