package se.tink.backend.system.cli.helper.traversal;

import java.util.concurrent.TimeUnit;
import rx.Observable.Operator;
import rx.Subscriber;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.utils.ExecutorServiceUtils;

public class ExecutorOperator<T> implements Operator<T, T> {

    private final ListenableThreadPoolExecutor executor;
    private final long gracefulShutDownTimeout;
    private final TimeUnit gracefulShutDownTimeoutUnit;

    public ExecutorOperator(ListenableThreadPoolExecutor executor, long gracefulShutDownTimeout,
            TimeUnit gracefulShutDownTimeoutUnit) {
        this.executor = executor;
        this.gracefulShutDownTimeout = gracefulShutDownTimeout;
        this.gracefulShutDownTimeoutUnit = gracefulShutDownTimeoutUnit;
    }

    // NOTE that this class only delegates onNext to the executor. onCompleted and onError will not be delegated! This
    // means that we adhere to contract of onCompleted and onError being called after all the onNext calls.
    @Override
    public Subscriber<? super T> call(final Subscriber<? super T> s) {
        return new Subscriber<T>() {

            /**
             * @return if shutdown was succesful
             */
            private boolean shutdownExecutor() {
                // The shutdown time is essentially the time it can take to finish executing #queueLimit items.
                // Make the timeout configurable if needed.
                return ExecutorServiceUtils.shutdownExecutor("executor", executor,
                        gracefulShutDownTimeout, gracefulShutDownTimeoutUnit);
            }

            @Override
            public void onCompleted() {
                if (!shutdownExecutor()) {
                    if (!s.isUnsubscribed()) {
                        s.onError(new RuntimeException("Could not shutdown the executor."));
                    }
                } else {
                    if (!s.isUnsubscribed()) {
                        s.onCompleted();
                    }
                }

            }

            @Override
            public void onError(Throwable e) {
                if (!shutdownExecutor()) {
                    e = new RuntimeException("Could not shutdown the executor, after a previous error.", e);
                }

                if (!s.isUnsubscribed()) {
                    s.onError(e);
                }

            }

            @Override
            public void onNext(final T t) {
                if (s.isUnsubscribed()) {
                    return;
                }

                executor.execute(() -> {
                    if (!s.isUnsubscribed()) {
                        // Important to check this again, since it might take some time before this Runnable is
                        // executed.

                        s.onNext(t);
                    }
                });
            }
        };
    }

}
