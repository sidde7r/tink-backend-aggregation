package se.tink.backend.aggregation.nxgen.storage;

import com.google.common.collect.ImmutableSet;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.ReplaySubject;
import io.reactivex.rxjava3.subjects.Subject;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public class SessionStorage extends Storage implements SensitiveValuesStorage {
    private Subject<Collection<String>> secretValuesSubject =
            ReplaySubject.<Collection<String>>create().toSerialized();

    @Override
    public String put(String key, String value) {
        return put(key, value, true);
    }

    public String put(String key, String value, boolean mask) {
        if (mask) {
            Optional.ofNullable(value)
                    .ifPresent(v -> secretValuesSubject.onNext(ImmutableSet.of(v)));
        }
        return super.put(key, value);
    }

    @Override
    public String put(String key, Object value) {
        return put(key, value, true);
    }

    public String put(String key, Object value, boolean mask) {
        final String valueToStore = super.put(key, value);
        if (mask) {
            Set<String> newSensitiveValues = StorageUtils.extractSensitiveValues(valueToStore);
            secretValuesSubject.onNext(ImmutableSet.copyOf(newSensitiveValues));
        }
        return valueToStore;
    }

    @Override
    public Observable<Collection<String>> getSensitiveValuesObservable() {
        return secretValuesSubject.subscribeOn(Schedulers.trampoline());
    }
}
