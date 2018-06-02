package se.tink.backend.system.cli;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.StatisticMode;
import se.tink.backend.system.api.ProcessService;
import se.tink.backend.system.rpc.GenerateStatisticsAndActivitiesRequest;
import se.tink.backend.utils.LogUtils;

public class RecalculateStatisticsCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(RecalculateStatisticsCommand.class);
    
    public RecalculateStatisticsCommand() {
        super("recalculate-statistics", "Recalculate all the users' statistics and activities.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        
        final ProcessService processService = serviceContext.getSystemServiceFactory().getProcessService();
        UserRepository userRepository = serviceContext.getRepository(UserRepository.class);
        
        // Use a specific userId-list if available.

        List<String> users = null;
        
        try {
            File userIdFilterFile = new File("userid-filter.txt");

            if (userIdFilterFile.exists()) {
                users = Files.readLines(userIdFilterFile, Charsets.UTF_8);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
        if (users == null || users.isEmpty()) {
            users = userRepository.findAllUserIds();
        }

        ExecutorService executor = Executors.newFixedThreadPool(1);
        final AtomicInteger counter = new AtomicInteger(0);
        
        for (final String userId : users) {
            final int finalUsersCount = counter.incrementAndGet();

            executor.execute(() -> {
                try {
                    log.info("\tGenerating statistics for user #" + finalUsersCount + ", with id: " + userId);
                    GenerateStatisticsAndActivitiesRequest request = new GenerateStatisticsAndActivitiesRequest();

                    request.setMode(StatisticMode.FULL);
                    request.setUserId(userId);

                    processService.generateStatisticsAndActivityAsynchronously(request);
                    Thread.sleep(500);
                } catch (Exception e) {
                    log.error("Could not generate statistics for user: " + userId, e);
                }
            });
        }
        
        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Could generate statistics", e);
        }

        log.info("Done generating stattistics for " + counter.get() + " users");

    }
}
