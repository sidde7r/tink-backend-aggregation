package se.tink.backend.common.utils;

import java.util.List;
import java.util.function.Function;

public class FunctionUtils<T> {
    public Function<T, T> compose(List<Function<T, T>> functions) {
        return functions.stream().reduce(Function.identity(), (x, y) -> x.andThen(y));
    }
}
