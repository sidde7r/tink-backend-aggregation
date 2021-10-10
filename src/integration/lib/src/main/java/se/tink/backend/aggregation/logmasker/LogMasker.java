package se.tink.backend.aggregation.logmasker;

import com.google.common.collect.ImmutableSet;
import io.reactivex.rxjava3.core.Observable;
import java.util.Collection;
import se.tink.backend.agents.rpc.Provider;

public interface LogMasker {

    void addAgentWhitelistedValues(ImmutableSet<String> agentWhitelistedValues);

    String mask(String dataToMask);

    void addSensitiveValuesSetObservable(
            Observable<Collection<String>> newSensitiveValuesSetObservable);

    void disposeOfAllSubscriptions();

    void addNewSensitiveValuesToMasker(Collection<String> newSensitiveValues);

    void addNewSensitiveValueToMasker(String newSensitiveValue);

    LoggingMode shouldLog(Provider provider);

    /**
     * This enumeration decides if logging should be done or not. NOTE: Only pass
     * LOGGING_MASKER_COVERS_SECRETS if you are 100% certain that the masker will handle your
     * secrets. If that is not the case, you pass the other one. Or use {@link #shouldLog(Provider)}
     * instead.
     */
    enum LoggingMode {
        LOGGING_MASKER_COVERS_SECRETS,
        UNSURE_IF_MASKER_COVERS_SECRETS
    }
}
