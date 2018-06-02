package se.tink.backend.common.utils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import rx.Observable;
import rx.functions.Func1;
import se.tink.backend.utils.LogUtils;

public class RxJavaUtils {

    private static class GuavaPredicateWrapper<T> implements Func1<T, Boolean> {

        private Predicate<T> predicate;

        public GuavaPredicateWrapper(Predicate<T> predicate) {
            this.predicate = predicate;
        }

        @Override
        public Boolean call(T t) {
            return predicate.apply(t);
        }

    }

    private static class GuavaFunctionWrapper<F, T> implements Func1<F, T> {

        private Function<F, T> function;

        public GuavaFunctionWrapper(Function<F, T> function) {
            this.function = function;
        }

        @Override
        public T call(F t) {
            return function.apply(t);
        }

    }

    public static <T> Func1<T, Boolean> fromGuavaPredicate(Predicate<T> predicate) {
        return new GuavaPredicateWrapper<>(predicate);
    }

    public static <F, T> Func1<F, T> fromGuavaFunction(Function<F, T> function) {
        return new GuavaFunctionWrapper<>(function);
    }

    public static <T> void logCountsPerKey(Observable<T> o, final LogUtils log) {

        o.groupBy(t -> t)
                .forEach(t -> t.count().forEach(count -> log.info(String.format("%s: %20s", t.getKey(), count))));

    }

}
