package se.tink.backend.aggregation.utils.masker;

import java.util.Comparator;

public final class MaskingConstants {

    private MaskingConstants() {
        throw new AssertionError();
    }

    static final Comparator<String> SENSITIVE_VALUES_SORTING_COMPARATOR =
            Comparator.comparing(String::length)
                    .reversed()
                    .thenComparing(Comparator.naturalOrder());
}
