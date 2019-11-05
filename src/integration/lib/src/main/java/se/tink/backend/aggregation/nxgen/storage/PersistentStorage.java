package se.tink.backend.aggregation.nxgen.storage;

import com.google.common.collect.ImmutableSet;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.ReplaySubject;
import io.reactivex.rxjava3.subjects.Subject;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public class PersistentStorage extends Storage implements SensitiveValuesStorage {
    private static final String OLD_VALUE_PREFIX = "OLD_";

    private Subject<Collection<String>> secretValuesSubject =
            ReplaySubject.<Collection<String>>create().toSerialized();

    @Override
    public void clear() {
        // do not allow to clear the storage
    }

    @Override
    public String put(String key, String value) {
        Optional.ofNullable(value).ifPresent(v -> secretValuesSubject.onNext(ImmutableSet.of(v)));
        return super.put(key, value);
    }

    @Override
    public Observable<Collection<String>> getSensitiveValuesObservable() {
        return secretValuesSubject.subscribeOn(Schedulers.trampoline());
    }

    @Override
    public String put(String key, Object value) {
        final String valueToStore = super.put(key, value);
        Set<String> newSensitiveValues =
                SensitiveValuesStorage.extractSensitiveValues(valueToStore);
        secretValuesSubject.onNext(ImmutableSet.copyOf(newSensitiveValues));
        return valueToStore;
    }

    /**
     * Stores the previous value of the given key instead of removing it entirely. This is meant to
     * be used for sensitive values in persistent storage that we want to be able to mask out from
     * logging etc. These values update during the course of a refresh, making it necessary to keep
     * the old and new value to be able to mask both of them from the logs.
     *
     * @param key
     * @param newValue
     */
    public void rotateStorageValue(String key, Object newValue) {
        String oldValue = this.get(key);
        this.put(OLD_VALUE_PREFIX + key, oldValue);
        this.put(key, newValue);
    }
}
