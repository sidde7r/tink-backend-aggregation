package se.tink.backend.firehose.v1.queue;

import java.util.Date;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This class is needed since Protobuf v3 doesn't support null in setters and throws a NullpointerException if null
 * is passed.
 */
public class ConverterUtils {

    public static <T> void setIfPresent(final Supplier<T> getter, final Consumer<T> setter) {
        Optional.ofNullable(getter.get()).ifPresent(setter);
    }

    public static <T> void setValueIfPresent(T value, final Consumer<T> setter) {
        Optional.ofNullable(value).ifPresent(setter);
    }

    public static <T> void setIfPresent(final Date date, final Consumer<Long> timestampSetter) {
        if (date != null) {
            timestampSetter.accept(date.getTime());
        }
    }
}

