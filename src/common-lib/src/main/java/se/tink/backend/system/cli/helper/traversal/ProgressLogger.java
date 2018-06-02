package se.tink.backend.system.cli.helper.traversal;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import rx.Observable;
import rx.Observable.Transformer;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;
import se.tink.backend.core.User;
import se.tink.backend.utils.LogUtils;

public class ProgressLogger<T> implements Transformer<T, T> {

    private static final LogUtils log = new LogUtils(ProgressLogger.class);

    private final Printer<T> printer;

    public interface Printer<T> {
        void print(UserCountStats<T> t);
    }

    public ProgressLogger(Printer<T> printer) {
        this.printer = printer;
    }

    private static class UserCountStats<T> {
        private final int count;
        public final T item;

        public UserCountStats(int count, T item) {
            this.item = item;
            this.count = count;
        }
    }

    public static class UserLogger implements Printer<User> {

        private static final LogUtils log = new LogUtils(UserLogger.class);

        @Override
        public void print(UserCountStats<User> stats) {
            log.info(stats.item.getId(), String.format("Approximately reached item #%d.", stats.count));
        }
    }

    public static class StringLogger implements Printer<String> {

        private static final LogUtils log = new LogUtils(StringLogger.class);

        @Override
        public void print(UserCountStats<String> stats) {
            log.info(String.format("Approximately reached item #%d described as '%s'.", stats.count, stats.item));
        }
    }

    @Override
    public Observable<T> call(Observable<T> t) {
        ConnectableObservable<T> observable = t.publish();

        observable.map(new Func1<T, UserCountStats<T>>() {

            private AtomicInteger count = new AtomicInteger();

            @Override
            public UserCountStats call(T t2) {
                return new UserCountStats<T>(count.incrementAndGet(), t2);
            }

        }).sample(10, TimeUnit.SECONDS).forEach(stats -> printer.print(stats));

        observable.count().forEach(totalUsers -> log.info("Processed " + totalUsers + " items."));

        // Don't subscribe unless something else is subscribing to it.
        return observable.autoConnect();
    }

}
