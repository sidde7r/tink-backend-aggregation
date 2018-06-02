package se.tink.backend.system.cli.helper.traversal;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.observables.ConnectableObservable;
import se.tink.backend.core.User;

public class ThreadPoolObserverTransformerTest {

    /**
     * Mostly making sure that I understand the RxJava Scheduler concept. See
     * https://dzone.com/articles/rx-java-subscribeon-and.
     * 
     * @throws Exception
     *             on issues.
     */
    @Test
    public void testConcurrency() throws Exception {
        Observable<User> allUsers = Observable.just(new User()).repeat(2);

        final CountDownLatch latch = new CountDownLatch(2);
        Observable<User> users = allUsers.compose(ThreadPoolObserverTransformer.buildWithConcurrency(2).<User> build());
        users.forEach(t -> latch.countDown());

        // This will fail if the filter callbacks aren't running concurrently.
        Assert.assertTrue(latch.await(1, TimeUnit.MINUTES));

    }
    
    @Test
    @Ignore
    // Slow test.
    public void testProcessLogging() throws Exception {
        Observable<User> allUsers = Observable.just(new User()).repeat(20);

        Observable<User> users = allUsers.compose(ThreadPoolObserverTransformer.buildWithConcurrency(1).<User> build());
        users.forEach(t -> {
            System.out.println(t);
            Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        });

    }

    @Test
    public void testConcurrencyOfFilter() throws Exception {
        Observable<User> allUsers = Observable.just(new User()).repeat(2);

        final AtomicInteger userCount = new AtomicInteger();

        final CountDownLatch latch = new CountDownLatch(2);
        Observable<User> users = allUsers.compose(ThreadPoolObserverTransformer.buildWithConcurrency(2).<User> build());
        users.filter(t -> {
            latch.countDown();
            return true;
        }).forEach(t -> userCount.incrementAndGet());

        // This will fail if the filter callbacks aren't running concurrently.
        Assert.assertTrue(latch.await(1, TimeUnit.MINUTES));

        Assert.assertEquals(2, userCount.get());
    }

    @Test
    public void testConnectableObservableAutoConnectUnderstanding() {

        // Given

        final AtomicInteger counter = new AtomicInteger(0);
        Action1<Integer> incrementer = t -> counter.incrementAndGet();
        ImmutableList<Integer> source = ImmutableList.of(1, 2, 3);

        ConnectableObservable<Integer> publishableObservable = Observable.from(source).publish();
        publishableObservable.forEach(incrementer);
        publishableObservable.forEach(incrementer);

        ConnectableObservable<Integer> publishableObservable2 = publishableObservable.autoConnect().publish();
        publishableObservable2.forEach(incrementer);
        publishableObservable2.forEach(incrementer);

        Assert.assertEquals(0, counter.get());

        // When

        publishableObservable2.connect();

        // Then

        Assert.assertEquals(12, counter.get());

    }

    @Test
    public void testObservableZipCountUnderstanding() {
        Observable<Integer> source = Observable.from(ImmutableList.of(1, 2, 3));
        Observable.zip(source.count(), source, (t1, t2) -> String.format("%d %d", t1, t2)).forEach(System.out::println);
    }

}
