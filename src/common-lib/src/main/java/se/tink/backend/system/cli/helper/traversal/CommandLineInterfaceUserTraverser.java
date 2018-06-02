package se.tink.backend.system.cli.helper.traversal;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.util.function.Function;
import rx.Observable;
import rx.Observable.Transformer;
import se.tink.backend.core.User;

public class CommandLineInterfaceUserTraverser implements Transformer<User, User> {

    private int concurrency;
    private CommandLineUserIdFilter commandLineUserIdFilter;
    private final CommandLineUserFilterFactory userIdFilterFactory = new CommandLineUserFilterFactory();
    private Function<String, Boolean> userFilter;
    public CommandLineInterfaceUserTraverser(int concurrency) throws IOException {
        Preconditions.checkArgument(concurrency >= 1, "Concurrency must be strictly positive.");
        this.concurrency = concurrency;
        this.userFilter = this.userIdFilterFactory.createUserFilter();
    }

    @Override
    public Observable<User> call(Observable<User> t) {
        Observable<User> result = t.filter(u ->
                this.userFilter.apply(u.getUsername()) || this.userFilter.apply(u.getId()))
                .compose(new ProgressLogger(new ProgressLogger.UserLogger()))
                .map(new CommandLineRateLimitter());
        if (concurrency > 1) {
            result = result.compose(
                    ThreadPoolObserverTransformer.buildFromSystemPropertiesWithConcurrency(concurrency)
                            .<User>build());
        }
        return result;
    }

    public boolean checkSubsetSizeRatio() {
        return this.userIdFilterFactory.checkSubsetSizeRatio();
    }
}
