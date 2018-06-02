package se.tink.backend.insights.app;

import com.google.inject.name.Named;
import javax.inject.Inject;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.insights.core.valueobjects.UserId;

public class TriggerHandler {
    private final ListenableThreadPoolExecutor<Runnable> executorService;
    private final GeneratorsProvider generatorsProvider;

    @Inject
    public TriggerHandler(@Named("insightsExecutor") ListenableThreadPoolExecutor<Runnable> executorService,
            GeneratorsProvider generatorsProvider) {
        this.executorService = executorService;
        this.generatorsProvider = generatorsProvider;
    }

    public void handle(String userIdStr) {
        UserId userId = UserId.of(userIdStr);
        generatorsProvider.getGenerators(userId)
                .forEach(generator -> executorService.execute(() -> generator.generateIfShould(userId)));
    }
}
