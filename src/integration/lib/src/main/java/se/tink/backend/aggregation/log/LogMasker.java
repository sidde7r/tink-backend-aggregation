package se.tink.backend.aggregation.log;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.regex.Pattern;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.nxgen.controllers.configuration.AgentConfigurationController;
import se.tink.backend.aggregation.utils.StringMasker;
import se.tink.backend.aggregation.utils.StringMaskerBuilder;

public class LogMasker implements PropertyChangeListener {

    public static final Comparator<String> SENSITIVE_VALUES_SORTING_COMPARATOR =
            Comparator.comparing(String::length)
                    .reversed()
                    .thenComparing(Comparator.naturalOrder());
    private static final ImmutableSet<String> WHITELISTED_SENSITIVE_VALUES =
            ImmutableSet.<String>builder().add("true").add("false").build();
    private static final ImmutableSet<String> MARKETS_MASKER_COVERS_SECRETS_FOR =
            ImmutableSet.<String>builder()
                    .add("fi")
                    .add("no")
                    .add("de")
                    .add("at")
                    .add("se")
                    .add("es")
                    .add("pt")
                    .add("fr")
                    .add("it")
                    .add("nl")
                    .add("be")
                    .add("gb")
                    .add("dk")
                    .build();

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

    private final StringMasker masker;

    private LogMasker(Builder builder) {
        masker = new StringMasker(builder.getStringMaskerBuilders(), this::isWhiteListed);
    }

    private boolean isWhiteListed(Pattern sensitiveValue) {
        return sensitiveValue.toString().length() <= 3
                || WHITELISTED_SENSITIVE_VALUES.contains(sensitiveValue.toString());
    }

    public String mask(String dataToMask) {
        return masker.getMasked(dataToMask);
    }

    @Override
    public void propertyChange(PropertyChangeEvent newSecretValues) {
            switch (newSecretValues.getPropertyName()) {
                case AgentConfigurationController.SECRET_VALUES_PROPERTY_NAME:
                    break;

                default:
                    throw new IllegalStateException(
                        "Unrecognized property name received: "
                            + newSecretValues.getPropertyName());
            }
        }
    }

    public static LoggingMode shouldLog(Provider provider) {
        // Temporary disable of http traffic logging for RE agents.
        // Leave until all RE agents logging has been evaluted and secrets moved to appropriate
        // format to be handled by logging masker.
        if (!provider.isOpenBanking()) {
            return LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS;
        }

        if (!MARKETS_MASKER_COVERS_SECRETS_FOR.contains(provider.getMarket().toLowerCase())) {
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
