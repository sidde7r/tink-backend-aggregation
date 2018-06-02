package se.tink.backend.common.concurrency;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Ordering;
import java.util.concurrent.BlockingQueue;

public class PriorityExecutorQueueFactory {

    public static class CancellingEvictionNotifier<T extends Runnable>
            implements CappedBlockingQueue.EvictionCallback<WrappedRunnableListenableFutureTask<T, ?>> {

        @Override
        public void evicted(WrappedRunnableListenableFutureTask<T, ?> t) {
            t.cancel(false);
        }
    }

    public static <T extends Runnable, C extends Comparable<X>, X> BlockingQueue<WrappedRunnableListenableFutureTask<T,
            ?>> cappedPriorityQueue(
            Function<WrappedRunnableListenableFutureTask<T, ?>, C> keyExtractor, int maxSize, Predicate<T> evictable) {
        WrappedRunnableListenableFutureTask.DelegateExtractor<T> extractor = new WrappedRunnableListenableFutureTask.DelegateExtractor<>();

        SortedDelegate<C, WrappedRunnableListenableFutureTask<T, ?>> delegate = new SortedDelegate<>(Ordering.natural(),
                keyExtractor);

        Predicate<WrappedRunnableListenableFutureTask<T, ?>> evictableChecker = Predicates
                .compose(evictable, extractor);

        return CappedBlockingQueue.<WrappedRunnableListenableFutureTask<T, ?>>builder()
                .setEvictionCallback(new CancellingEvictionNotifier())
                .setIsEvictable(evictableChecker)
                .build(maxSize, delegate);
    }

}
