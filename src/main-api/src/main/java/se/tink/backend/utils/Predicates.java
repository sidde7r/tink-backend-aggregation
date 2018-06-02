package se.tink.backend.utils;

import java.util.function.Function;
import java.util.function.Predicate;

public class Predicates {
    // Composition of a function with a predicate
    // Taken from https://stackoverflow.com/a/40992320.
    public static <T, S> Predicate<T> compose(Function<T, S> first, Predicate<S> second) {
        return input -> second.test(first.apply(input));
    }

    public static <T> Predicate<T> not(Predicate<T> p) {
        return input -> !p.test(input);
    }
}
