package se.tink.backend.system.cli.extraction;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.common.io.Files;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.BufferedWriter;
import java.io.File;
import java.util.List;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.core.User;
import se.tink.backend.core.UserState;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.date.Period;
import se.tink.libraries.date.ThreadSafeDateFormat;

/**
 * Used to get all CategoryChangeRecords from MongoDB, for analysis
 */
public class ExtractPeriodBreaksCommand extends ServiceContextCommand<ServiceConfiguration> {
    public ExtractPeriodBreaksCommand() {
        super("extract-period-breaks", "Extracts periods and period break dates for evaluation");
    }

    private static final LogUtils log = new LogUtils(ExtractPeriodBreaksCommand.class);

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        UserStateRepository userStateRepository = serviceContext.getRepository(UserStateRepository.class);
        UserRepository userRepository = serviceContext.getRepository(UserRepository.class);

        String username = System.getProperty("username");

        // only get for one user

        if (username != null) {
            BufferedWriter fileWrite = Files.newWriter(new File("data/test/periods.txt"), Charsets.UTF_8);

            User user = userRepository.findOneByUsername(username);

            UserState userState = userStateRepository.findOneByUserId(user.getId());

            fileWrite.write("Period\tStartDate\tEndDate\n");
            for (Period period : userState.getPeriods()) {
                fileWrite.append(period.getName() + "\t"
                        + ThreadSafeDateFormat.FORMATTER_DAILY.format(period.getStartDate()) + "\t"
                        + ThreadSafeDateFormat.FORMATTER_DAILY.format(period.getEndDate()));
                fileWrite.newLine();
            }
            fileWrite.flush();
            fileWrite.close();
            log.info("Done writing periods for " + username + " to data/test/periods.txt");
        }

        // summarize for all users

        else {
            List<UserState> userStates = userStateRepository.findAll();
            List<Period> allPeriods = Lists.newArrayList();

            log.info("Found " + userStates.size() + " user states");

            for (UserState us : userStates) {
                if (us.getPeriods() != null) {
                    allPeriods.addAll(us.getPeriods());
                }
            }

            log.info("Extracted " + allPeriods.size() + " periods from users states");

            ImmutableListMultimap<String, Period> periodsByPeriodName = Multimaps.index(allPeriods,
                    Period::getName);

            log.info("Writing " + periodsByPeriodName.keySet().size() + " periods names to file");

            for (String periodName : periodsByPeriodName.keySet()) {

                String filename = "data/test/" + periodName + ".txt";
                BufferedWriter fileWrite = Files.newWriter(new File(filename), Charsets.UTF_8);

                log.info("Writing " + periodName + " to file");

                fileWrite.write(periodName);
                for (Period period : periodsByPeriodName.get(periodName)) {
                    fileWrite.newLine();
                    fileWrite.append(ThreadSafeDateFormat.FORMATTER_DAILY.format(period.getStartDate()));
                }
                fileWrite.flush();
                fileWrite.close();
            }
            log.info("Done writing " + allPeriods.size() + " periods");
        }
    }

}
