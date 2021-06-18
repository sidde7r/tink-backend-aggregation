package se.tink.backend.aggregation.nxgen.storage;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import org.apache.commons.lang3.tuple.Pair;

public interface InsertionObservable {
    /** Subscribe on storage insertions. The Pair contain the storage key and value. */
    Disposable subscribeOnInsertion(Consumer<Pair<String, Object>> onInsertion);
}
