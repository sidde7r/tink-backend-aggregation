package se.tink.backend.system.cronjob.job;

import com.google.inject.Inject;
import se.tink.backend.common.client.FastTextServiceFactoryProvider;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.system.workers.training.FastTextModelTrainer;
import se.tink.libraries.log.LogUtils;

public class FastTextTrainerJob {
    private static final LogUtils log = new LogUtils(FastTextTrainerJob.class);
    private final FastTextModelTrainer fastTextModelTrainer;

    @Inject
    public FastTextTrainerJob(CategoryRepository categoryRepository, FastTextServiceFactoryProvider fastTextServiceFactoryProvider,
                              TransactionDao transactionDao, UserRepository userRepository) {
        fastTextModelTrainer = new FastTextModelTrainer(categoryRepository, fastTextServiceFactoryProvider, transactionDao, userRepository);
    }

    public void run() throws Exception {
        log.info("Starting fast text training job");
        fastTextModelTrainer.trainFastTextModel();
        log.info("Done with fast text training job");
    }
}
