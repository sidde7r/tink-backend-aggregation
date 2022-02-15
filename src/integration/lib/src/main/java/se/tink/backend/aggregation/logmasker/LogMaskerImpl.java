package se.tink.backend.aggregation.logmasker;

import com.google.common.collect.ImmutableSet;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.utils.masker.Base64EncodedMaskerPatternsProvider;
import se.tink.backend.aggregation.utils.masker.SensitiveValuesCollectionMaskerPatternsProvider;
import se.tink.libraries.masker.StringMasker;

public class LogMaskerImpl implements LogMasker {

    private static final ImmutableSet<String> WHITELISTED_SENSITIVE_VALUES =
            ImmutableSet.<String>builder().add("true").add("false").add("null").build();
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
                    .add("ee")
                    .add("lt")
                    .add("lv")
                    .add("pl")
                    .build();
    private static final int MINIMUM_LENGTH_TO_BE_CONSIDERED_A_SECRET = 3;

    private final Set<String> whitelistedValues = new HashSet<>();
    private final CompositeDisposable composite = new CompositeDisposable();
    private final StringMasker masker = new StringMasker();

    @Override
    public void addAgentWhitelistedValues(ImmutableSet<String> agentWhitelistedValues) {
        this.whitelistedValues.addAll(agentWhitelistedValues);
        masker.removeValuesToMask(whitelistedValues);
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
                new SensitiveValuesCollectionMaskerPatternsProvider(newSensitiveValues),
                this::shouldMask);

        masker.addValuesToMask(
                new Base64EncodedMaskerPatternsProvider(newSensitiveValues), this::shouldMask);
        // This causes incident so I disabled it until we fix the issue
        /*masker.addValuesToMask(
        new Base64DecodedMaskerValuesProvider(newSensitiveValues), this::shouldMask);*/
    }

    @Override
    public LoggingMode shouldLog(Provider provider) {
        if (!MARKETS_MASKER_COVERS_SECRETS_FOR.contains(provider.getMarket().toLowerCase())) {
            return LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS;
        }
        return LoggingMode.LOGGING_MASKER_COVERS_SECRETS;
    }

    @Override
    public void addNewSensitiveValueToMasker(String newSensitiveValue) {
        this.addNewSensitiveValuesToMasker(Collections.singletonList(newSensitiveValue));
    }
}
