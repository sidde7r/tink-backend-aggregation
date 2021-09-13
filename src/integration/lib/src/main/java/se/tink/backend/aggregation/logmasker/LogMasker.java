package se.tink.backend.aggregation.logmasker;

import com.google.common.collect.ImmutableSet;
import io.reactivex.rxjava3.core.Observable;
import java.util.Collection;

public interface LogMasker {

    void addAgentWhitelistedValues(ImmutableSet<String> agentWhitelistedValues);

    String mask(String dataToMask);

    void addSensitiveValuesSetObservable(
            Observable<Collection<String>> newSensitiveValuesSetObservable);

    void disposeOfAllSubscriptions();

    void addNewSensitiveValuesToMasker(Collection<String> newSensitiveValues);

    void addNewSensitiveValueToMasker(String newSensitiveValue);
}
