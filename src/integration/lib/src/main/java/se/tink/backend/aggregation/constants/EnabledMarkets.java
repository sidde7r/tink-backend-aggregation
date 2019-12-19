package se.tink.backend.aggregation.constants;

import com.google.common.collect.ImmutableSet;

public final class EnabledMarkets {
    private EnabledMarkets() {
        throw new AssertionError();
    }

    public static final ImmutableSet<String> ENABLED_MARKETS =
            ImmutableSet.<String>builder().add("SE", "GB", "ES", "DK", "NO", "BE", "NL").build();
}
