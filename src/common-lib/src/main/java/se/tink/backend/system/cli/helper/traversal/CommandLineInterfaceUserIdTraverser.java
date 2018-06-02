package se.tink.backend.system.cli.helper.traversal;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.util.function.Function;
import rx.Observable;

public class CommandLineInterfaceUserIdTraverser implements Observable.Transformer<String, String> {
    private final int concurrency;
    private final Function<String, Boolean> userFilter;

    public CommandLineInterfaceUserIdTraverser(int concurrency) throws IOException {
        Preconditions.checkArgument(concurrency >= 1, "Concurrency must be strictly positive.");
        this.concurrency = concurrency;
        this.userFilter = new CommandLineUserFilterFactory().createUserFilter();
    }

    @Override
    public Observable<String> call(Observable<String> t) {
        Observable<String> result = t.filter(id -> this.userFilter.apply(id))
                .compose(new ProgressLogger(new ProgressLogger.UserLogger()))
                .map(new CommandLineRateLimitter());
        if (concurrency > 1) {
            result = result.compose(
                    ThreadPoolObserverTransformer.buildFromSystemPropertiesWithConcurrency(concurrency)
                            .<String>build());
        }
        return result;
    }
}
