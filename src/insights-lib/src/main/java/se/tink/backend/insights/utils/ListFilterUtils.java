package se.tink.backend.insights.utils;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ListFilterUtils {

    public static <T, R> ImmutableList<R> filterObjectList(List<R> objectList, Function<R, T> objectFunction,
            T requirement, BiFunction<T, T, Boolean> filterFunction) {
        return ImmutableList.copyOf(
                objectList.stream()
                        .filter(s -> evalFunction(objectFunction.apply(s), requirement, filterFunction))
                        .collect(Collectors.toList()));
    }

    private static <T> boolean evalFunction(T t1, T t2, BiFunction<T, T, Boolean> biFunction) {
        return biFunction.apply(t1, t2);
    }
}
