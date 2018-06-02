package se.tink.backend.grpc.v1.converter;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ConverterUtils {
    public static <T> void setIfPresent(final Supplier<T> getter, final Consumer<T> setter) {
        Optional.ofNullable(getter.get()).ifPresent(setter);
    }

    public static <T, R> void setIfPresent(final Supplier<T> getter, final Consumer<R> setter, Function<T, R> mapper) {
        Optional.ofNullable(getter.get()).map(mapper).ifPresent(setter);
    }

    public static <T, R> void setIfPresent(Supplier<Boolean> presence, final Supplier<T> getter,
            final Consumer<R> setter, Function<T, R> mapper) {
        if (presence.get()) {
            Optional.ofNullable(getter.get()).map(mapper).ifPresent(setter);
        }
    }

    public static <T, R> void mapList(final Supplier<List<T>> getter, final Consumer<List<R>> setter,
            Function<T, R> mapper) {
        Optional.ofNullable(getter.get()).map(list -> list.stream().map(mapper).filter(Objects::nonNull).collect(
                Collectors.toList())).ifPresent(setter);
    }
}
