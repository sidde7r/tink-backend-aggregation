package se.tink.backend.aggregation.log;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.Subject;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.regex.Pattern;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.utils.Base64Masker;
import se.tink.backend.aggregation.utils.SensitiveValuesCollectionStringMaskerBuilder;
import se.tink.backend.aggregation.utils.StringMasker;
import se.tink.backend.aggregation.utils.StringMaskerBuilder;

public class LogMasker {

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
    private static final int MINIMUM_LENGTH_TO_BE_CONSIDERED_A_SECRET = 3;

    private CompositeDisposable composite = new CompositeDisposable();

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
        masker = new StringMasker(builder.getStringMaskerBuilders(), this::shouldMask);
    }

    private boolean shouldMask(Pattern sensitiveValue) {
        return sensitiveValue.toString().length() > MINIMUM_LENGTH_TO_BE_CONSIDERED_A_SECRET
                && !WHITELISTED_SENSITIVE_VALUES.contains(sensitiveValue.toString());
    }

    public String mask(String dataToMask) {
        return masker.getMasked(dataToMask);
    }

    public void addSensitiveValuesSetSubject(
            Subject<Collection<String>> newSensitiveValuesSetSubject) {
        composite.add(
                newSensitiveValuesSetSubject
                        .subscribeOn(Schedulers.trampoline())
                        .subscribe(this::addNewSensitiveValuesToMasker));
    }

    public void disposeOfAllSubscriptions() {
        composite.dispose();
    }

    private void addNewSensitiveValuesToMasker(Collection<String> newSensitiveValues) {
        masker.addValuesToMask(
                new SensitiveValuesCollectionStringMaskerBuilder(newSensitiveValues),
                this::shouldMask);

        masker.addValuesToMask(new Base64Masker(newSensitiveValues), this::shouldMask);
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
