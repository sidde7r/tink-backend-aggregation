package se.tink.backend.system.cli.analytics;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.utils.PaydayCalculator;
import se.tink.backend.core.Category;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;

public class EvaluatePaydateCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(EvaluatePaydateCommand.class);

    private TransactionDao transactionDao;

    private ImmutableMap<String, Category> categoriesByCode;
    private CategoryConfiguration categoryConfiguration;

    public EvaluatePaydateCommand() {
        super("evaluate-pay-date-logic", "Evaluate pay/salary date logic");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, final ServiceContext serviceContext) throws Exception {

        String users = System.getProperty("users");

        if (Strings.isNullOrEmpty(users)) {
            log.error(
                    "You need to specify which users to re-sync. Either `users=all` or `users=<comma separated list of user ids>`.");
            return;
        }

        transactionDao = serviceContext.getDao(TransactionDao.class);
        UserRepository userRepository = serviceContext.getRepository(UserRepository.class);
        CategoryRepository categoryRepository = serviceContext.getRepository(CategoryRepository.class);

        categoriesByCode = Maps.uniqueIndex(categoryRepository.findAll(), Category::getCode);

        categoryConfiguration = serviceContext.getCategoryConfiguration();

        final ExecutorService executor = Executors.newFixedThreadPool(10);

        if ("all".equalsIgnoreCase(users)) {
            userRepository.streamAll().forEach(user -> executor.execute(() -> process(user)));
        } else {
            Iterable<String> userIds = Splitter.on(',').split(users);
            for (String userId : userIds) {
                final User user = userRepository.findOne(userId);
                if (user == null) {
                    log.warn(String.format("User could not be found [userId=%s]. Skipping.", userId));
                    continue;
                }

                executor.execute(() -> process(user));
            }
        }

        executor.shutdown();

        executor.awaitTermination(24, TimeUnit.HOURS);
    }

    private void process(User user) {
        List<Transaction> transactions = transactionDao.findAllByUserId(user.getId());

        PaydayCalculator paydayCalculator = new PaydayCalculator(categoryConfiguration, categoriesByCode, transactions);

        Integer oldPayday = paydayCalculator.detectPayday(PaydayCalculator.CalculationMode.SIMPLE);
        Integer newPayday = paydayCalculator.detectPayday(PaydayCalculator.CalculationMode.ADVANCED);

        if (!Objects.equals(oldPayday, newPayday)) {
            log.info(user.getId(), String.format("Change, Old: %s New: %s", oldPayday, newPayday));
        } else {
            log.debug(user.getId(), String.format("No change, Old: %s New: %s", oldPayday, newPayday));
        }

    }

}
