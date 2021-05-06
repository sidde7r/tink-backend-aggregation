package se.tink.libraries.pair;

import java.util.Objects;

/** @deprecated use {@link org.apache.commons.lang3.tuple.Pair } */
@Deprecated
public class Pair<T, U> {
    public final T first;
    public final U second;

    public Pair(T t, U u) {
        this.first = t;
        this.second = u;
    }

    public static <T, U> Pair<T, U> of(T first, U second) {
        return new Pair<>(first, second);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) {
            return false;
        }
        final Pair<?, ?> other = (Pair<?, ?>) o;
        return Objects.equals(first, other.first) && Objects.equals(second, other.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
