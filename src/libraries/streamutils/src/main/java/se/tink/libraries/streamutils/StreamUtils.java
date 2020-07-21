package se.tink.libraries.streamutils;

import java.util.stream.Collector;
import java.util.stream.Collectors;

public class StreamUtils {

    /**
     * Will return an element if it is the only element in the stream.
     *
     * @param <T>
     * @return the single element in the stream or null.
     */
    public static <T> Collector<T, ?, T> toSingleton() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.size() != 1) {
                        return null;
                    }
                    return list.get(0);
                });
    }
}
