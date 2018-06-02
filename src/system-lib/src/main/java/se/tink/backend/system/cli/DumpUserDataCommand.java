package se.tink.backend.system.cli;

import com.google.common.base.Strings;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.ActivityDao;
import se.tink.backend.common.dao.StatisticDao;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.User;

public class DumpUserDataCommand extends ServiceContextCommand<ServiceConfiguration> {
    public DumpUserDataCommand() {
        super("dump-user-data",
                "Dumps all the users' statistics and activities (can specify a single username with -Dusername=xxx@xxx.com)");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        UserRepository userRepository = serviceContext.getRepository(UserRepository.class);

        String username = System.getProperty("username");

        if (Strings.isNullOrEmpty(username)) {
            for (User user : userRepository.findAll()) {
                dump(user, serviceContext);
            }
        } else {
            dump(userRepository.findOneByUsername(username), serviceContext);
        }
    }

    private static void dump(User user, ServiceContext serviceContext) {
        StatisticDao statisticDao = serviceContext.getDao(StatisticDao.class);
        ActivityDao activityDao = serviceContext.getDao(ActivityDao.class);

        System.out.println("User: " + user.toString());

        List<Statistic> ss = statisticDao.findByUserId(user.getId());

        System.out.println("\tStatistics");

        for (Statistic s : ss) {
            System.out.println("\t\t" + s);
        }

        List<Activity> as = activityDao.findByUserId(user.getId());

        System.out.println("\tActivities");

        for (Activity a : as) {
            System.out.println("\t\t" + a);
        }
    }
}
