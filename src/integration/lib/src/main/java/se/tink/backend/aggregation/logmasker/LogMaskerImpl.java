package se.tink.backend.aggregation.logmasker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.utils.masker.Base64Masker;
import se.tink.backend.aggregation.utils.masker.SensitiveValuesCollectionStringMaskerBuilder;
import se.tink.backend.aggregation.utils.masker.StringMasker;
import se.tink.backend.aggregation.utils.masker.StringMaskerBuilder;

public class LogMaskerImpl implements LogMasker {

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
                    .add("ie")
                    .add("fr")
                    .add("it")
                    .add("nl")
                    .add("be")
                    .add("gb")
                    .add("dk")
                    .build();
    private static final int MINIMUM_LENGTH_TO_BE_CONSIDERED_A_SECRET = 3;

    private Set<String> whitelistedValues = new HashSet<>();

    private CompositeDisposable composite = new CompositeDisposable();

    @Override
    public void addAgentWhitelistedValues(ImmutableSet<String> agentWhitelistedValues) {
        this.whitelistedValues.addAll(agentWhitelistedValues);
        masker.removeValuesToMask(whitelistedValues);
    }

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

    private LogMaskerImpl(Builder builder) {
        masker = new StringMasker(builder.getStringMaskerBuilders(), this::shouldMask);
    }

    private boolean shouldMask(Pattern sensitiveValue) {

        return sensitiveValue.toString().length() > MINIMUM_LENGTH_TO_BE_CONSIDERED_A_SECRET
                && !WHITELISTED_SENSITIVE_VALUES.contains(sensitiveValue.toString())
                && !this.whitelistedValues.contains(sensitiveValue.toString());
    }

    @Override
    public String mask(String dataToMask) {
        return masker.getMasked(dataToMask);
    }

    @Override
    public void addSensitiveValuesSetObservable(
            Observable<Collection<String>> newSensitiveValuesSetObservable) {
        composite.add(
                newSensitiveValuesSetObservable.subscribe(this::addNewSensitiveValuesToMasker));
    }

    @Override
    public void disposeOfAllSubscriptions() {
        composite.dispose();
    }

    @Override
    public void addNewSensitiveValuesToMasker(Collection<String> newSensitiveValues) {
        masker.addValuesToMask(
                new SensitiveValuesCollectionStringMaskerBuilder(newSensitiveValues),
                this::shouldMask);

        masker.addValuesToMask(new Base64Masker(newSensitiveValues), this::shouldMask);
    }

    public static LoggingMode shouldLog(Provider provider) {
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
            return new LogMaskerImpl(this);
        }
    }
}
