package se.tink.backend.system.cli.training;

import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.client.FastTextServiceFactoryProvider;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.workers.training.FastTextModelTrainer;
import se.tink.libraries.log.LogUtils;

public class TrainFastTextModelCommand extends ServiceContextCommand<ServiceConfiguration> {
    private static final LogUtils log = new LogUtils(TrainFastTextModelCommand.class);

    public TrainFastTextModelCommand() {
        super("train-fasttext", "a command that feeds labeled transactions to the fasttext service");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace, ServiceConfiguration configuration,
                       Injector injector, ServiceContext serviceContext) throws Exception {

        CategoryRepository categoryRepository = injector.getInstance(CategoryRepository.class);
        FastTextServiceFactoryProvider fastTextServiceFactoryProvider = injector.getInstance(FastTextServiceFactoryProvider.class);
        TransactionDao transactionDao = injector.getInstance(TransactionDao.class);
        UserRepository userRepository = injector.getInstance(UserRepository.class);
        FastTextModelTrainer fastTextModelTrainer = new FastTextModelTrainer(categoryRepository, fastTextServiceFactoryProvider
                , transactionDao, userRepository);
        fastTextModelTrainer.trainFastTextModel();
    }


}
