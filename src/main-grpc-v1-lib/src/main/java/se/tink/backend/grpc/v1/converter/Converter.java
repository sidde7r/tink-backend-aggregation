package se.tink.backend.grpc.v1.converter;

import java.util.List;
import java.util.stream.Collectors;

public interface Converter<T, R> {
    R convertFrom(T input);

    default List<R> convertFrom(List<T> list) {
        return list.stream()
                .map(this::convertFrom)
                .collect(Collectors.toList());
    }
}
