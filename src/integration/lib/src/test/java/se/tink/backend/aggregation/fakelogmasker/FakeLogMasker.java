package se.tink.backend.aggregation.fakelogmasker;

import com.google.common.collect.ImmutableSet;
import io.reactivex.rxjava3.core.Observable;
import java.util.Collection;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.logmasker.LogMasker;

/**
 * Implementation of LogMasker that does not mask anything. Useful for testing where you want to see
 * all log output.
 */
public final class FakeLogMasker implements LogMasker {

    @Override
    public void addAgentWhitelistedValues(ImmutableSet<String> agentWhitelistedValues) {}

    @Override
    public String mask(String dataToMask) {
        return dataToMask;
    }

    @Override
    public void addSensitiveValuesSetObservable(
            Observable<Collection<String>> newSensitiveValuesSetObservable) {}

    @Override
    public void disposeOfAllSubscriptions() {}

    @Override
    public void addNewSensitiveValuesToMasker(Collection<String> newSensitiveValues) {}

    @Override
    public void addNewSensitiveValueToMasker(String newSensitiveValue) {}

    @Override
    public LoggingMode shouldLog(Provider provider) {
        return LoggingMode.LOGGING_MASKER_COVERS_SECRETS;
    }
}
