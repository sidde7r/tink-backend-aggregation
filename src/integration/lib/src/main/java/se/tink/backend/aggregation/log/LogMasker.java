package se.tink.backend.aggregation.log;

import com.google.common.collect.ImmutableSortedSet;
import java.util.Comparator;
import java.util.LinkedHashSet;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.utils.StringMaskerBuilder;

public class LogMasker {

    /**
     * This enumeration decides if logging should be done or not. NOTE: Only pass
     * LOGGING_MASKER_COVERS_SECRETS if you are 100% certain that the masker will handle your
     * secrets. If that is not the case, you pass the other one. Or use {@link #shouldLog(Provider)}
     * instead.
     */
    public enum LoggingMode {
        LOGGING_MASKER_COVERS_SECRETS,
        UNSURE_IF_MASKER_COVERS_SECRETS
    }

    public static final String MASK = "***MASKED***";

    private final ImmutableSortedSet<String> sensitiveValuesToMask;

    private LogMasker(Builder builder) {
        sensitiveValuesToMask = createUniqueStringMasker(builder.getStringMaskerBuilders());
    }

    private ImmutableSortedSet<String> createUniqueStringMasker(
            LinkedHashSet<StringMaskerBuilder> stringMaskerBuilders) {
        ImmutableSortedSet.Builder<String> builder =
                ImmutableSortedSet.orderedBy(Comparator.comparing(String::length).reversed());

        stringMaskerBuilders.forEach(
                stringMaskerBuilder -> builder.addAll(stringMaskerBuilder.getValuesToMask()));

        return builder.build();
    }

    public String mask(String dataToMask) {
        return sensitiveValuesToMask.stream()
                .reduce(dataToMask, (s1, value) -> s1.replace(value, MASK));
    }

    public static LoggingMode shouldLog(Provider provider) {
        // Disable logging for now.
        if (true) {
            return LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS;
        }
        // Temporary disable of http traffic logging for RE agents.
        // Leave until all RE agents logging has been evaluted and secrets moved to appropriate
        // format to be handled by logging masker.
        if (!provider.isOpenBanking()) {
            return LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS;
        }

        if (
        /*!"FI".equalsIgnoreCase(provider.getMarket())
        && !"NO".equalsIgnoreCase(provider.getMarket())
        && !"DE".equalsIgnoreCase(provider.getMarket())
        && !"FR".equalsIgnoreCase(provider.getMarket())
        && !"IT".equalsIgnoreCase(provider.getMarket())
        &&*/ !"SE".equalsIgnoreCase(provider.getMarket())
                && !"ES".equalsIgnoreCase(provider.getMarket())) {
            return LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS;
        }
        return LoggingMode.LOGGING_MASKER_COVERS_SECRETS;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private LinkedHashSet<StringMaskerBuilder> stringMaskerBuilders;

        private Builder() {}

        public Builder addStringMaskerBuilder(StringMaskerBuilder stringMaskerBuilder) {
            stringMaskerBuilders.add(stringMaskerBuilder);
            return this;
        }

        private LinkedHashSet<StringMaskerBuilder> getStringMaskerBuilders() {
            return stringMaskerBuilders;
        }

        public LogMasker build() {
            return new LogMasker(this);
        }
    }
}
