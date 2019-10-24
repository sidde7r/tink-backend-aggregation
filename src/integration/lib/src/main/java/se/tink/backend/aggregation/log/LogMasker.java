package se.tink.backend.aggregation.log;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.LinkedHashSet;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.utils.StringMaskerBuilder;

public class LogMasker {

    public static final Comparator<String> SENSITIVE_VALUES_SORTING_COMPARATOR =
            Comparator.comparing(String::length)
                    .reversed()
                    .thenComparing(Comparator.naturalOrder());
    private static final ImmutableSet<String> WHITELISTED_SENSITIVE_VALUES =
            ImmutableSet.<String>builder().add("true").add("false").build();

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

    private final ImmutableList<String> sensitiveValuesToMask;

    private LogMasker(Builder builder) {
        sensitiveValuesToMask = mergeSensitiveValuesToMask(builder.getStringMaskerBuilders());
    }

    private ImmutableList<String> mergeSensitiveValuesToMask(
            ImmutableList<StringMaskerBuilder> stringMaskerBuilders) {
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();

        stringMaskerBuilders.forEach(
                stringMaskerBuilder -> builder.addAll(stringMaskerBuilder.getValuesToMask()));

        ImmutableSet<String> sensitiveValuesWithoutDuplicates = builder.build();

        ImmutableList<String> sensitiveValuesToMaskWithoutDuplicates =
                sensitiveValuesWithoutDuplicates.stream()
                        .filter(this::shouldSensitiveValueBeMasked)
                        .sorted(SENSITIVE_VALUES_SORTING_COMPARATOR)
                        .collect(ImmutableList.toImmutableList());

        return sensitiveValuesToMaskWithoutDuplicates;
    }

    private boolean shouldSensitiveValueBeMasked(String sensitiveValue) {
        if (sensitiveValue.length() <= 3 || WHITELISTED_SENSITIVE_VALUES.contains(sensitiveValue)) {
            return false;
        }
        return true;
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
        private LinkedHashSet<StringMaskerBuilder> stringMaskerBuilders = new LinkedHashSet<>();

        private Builder() {}

        public Builder addStringMaskerBuilder(StringMaskerBuilder stringMaskerBuilder) {
            stringMaskerBuilders.add(stringMaskerBuilder);
            return this;
        }

        private ImmutableList<StringMaskerBuilder> getStringMaskerBuilders() {
            return ImmutableList.copyOf(stringMaskerBuilders);
        }

        public LogMasker build() {
            return new LogMasker(this);
        }
    }
}
