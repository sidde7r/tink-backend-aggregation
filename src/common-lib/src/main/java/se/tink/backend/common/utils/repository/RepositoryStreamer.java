package se.tink.backend.common.utils.repository;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import rx.Observable;
import se.tink.backend.utils.LogUtils;

/*package*/class RepositoryStreamer<T> {

    private static final Joiner NO_SEPARATOR_JOINER = Joiner.on("");
    private static final int MAX_ID_LENGTH = 32;

    private PrefixRepository<T> repository;
    private static final LogUtils log = new LogUtils(RepositoryStreamer.class);

    public RepositoryStreamer(PrefixRepository<T> repository) {
        this.repository = repository;
    }

    /**
     * Estimate the length of the prefix to be below a certain batch size. Assumes uniform distribution of prefixes.
     * 
     * @param maxBatchSize
     *            the maximum allowed batch size.
     * @return
     */
    private int estimatePrefixLength(int maxBatchSize) {
        int length = 0;

        int nUsers;
        do {
            nUsers = repository
                    .countByIdPrefix(NO_SEPARATOR_JOINER.join(Iterators.limit(Iterators.cycle('0'), ++length)));
        } while (nUsers > maxBatchSize && length < MAX_ID_LENGTH);

        return length;
    }

    public Observable<T> streamAll(int batchSize) {
        final int prefixLength = estimatePrefixLength(batchSize);
        Preconditions.checkState(prefixLength > 0);
        log.debug("Estimated prefix length: " + prefixLength);

        Observable<Observable<T>> a = Observable.create(t -> {
            try {

                // NOTE: Any database logic in this anonymous method must be refactored out to a separate method in
                // UserRepositoryImpl to make sure that each database operation uses a non-closed database session.

                for (String useridPrefix : RepositoryUtils.hexPrefixes(prefixLength)) {
                    if (t.isUnsubscribed()) {
                        return;
                    }
                    log.trace("Querying for prefix: " + useridPrefix);
                    t.onNext(Observable.from(repository.listByIdPrefix(useridPrefix)));
                }

                if (!t.isUnsubscribed()) {
                    t.onCompleted();
                }
            } catch (Throwable e) {
                if (!t.isUnsubscribed()) {
                    t.onError(e);
                }
            }
        });

        return Observable.concat(a);
    }

}
