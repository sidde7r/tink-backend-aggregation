package se.tink.backend.aggregation.nxgen.storage;

import io.reactivex.rxjava3.core.Observable;
import java.util.Collection;

public interface SensitiveValuesStorage {
    Observable<Collection<String>> getSensitiveValuesObservable();
}
