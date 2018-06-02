package se.tink.backend.system.cli.debug;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.StatisticDao;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.User;
import se.tink.backend.system.cli.CliPrintUtils;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.libraries.uuid.UUIDUtils;

public class DebugUserStatisticsCommand extends ServiceContextCommand<ServiceConfiguration> {
    public DebugUserStatisticsCommand() {
        super("debug-user-statistics", "Dump user statistics in readable form.");
    }

    private void printUserInfo(User user) {
        List<Map<String, String>> output = Lists.newArrayList();
        output.add(CliPrintUtils.keyValueEntry("id", String.format("%s (%s)", user.getId(),
                UUIDUtils.fromTinkUUID(user.getId()).toString())));
        output.add(CliPrintUtils.keyValueEntry("username", user.getUsername()));
        output.add(CliPrintUtils.keyValueEntry("profile_market", user.getProfile().getMarket()));
        output.add(CliPrintUtils.keyValueEntry("profile_locale", user.getProfile().getLocale()));
        output.add(CliPrintUtils.keyValueEntry("created", String.valueOf(user.getCreated())));
        output.add(CliPrintUtils.keyValueEntry("profile_periodadjustedday",
                Integer.toString(user.getProfile().getPeriodAdjustedDay())));
        output.add(CliPrintUtils.keyValueEntry("blocked", Boolean.toString(user.isBlocked())));
        output.add(CliPrintUtils.keyValueEntry("flags", String.valueOf(user.getFlags())));
        output.add(CliPrintUtils.keyValueEntry("debugFlag", String.valueOf(user.isDebug())));
        output.add(CliPrintUtils.keyValueEntry("nationalId", user.getNationalId()));

        CliPrintUtils.printTable(output);
    }

    private void printStatistics(StatisticDao statisticDao, String userId, String statisticType, List<String> periods) {
        List<Statistic> statistics = statisticDao.findByUserId(userId);
        for (Statistic statistic : statistics) {
            if (statisticType != null) {
                if (!statistic.getType().equals(statisticType)) {
                    continue;
                }
            }

            if (!periods.isEmpty()) {
                if (!periods.contains(statistic.getPeriod())) {
                    continue;
                }
            }

            System.out.println(statistic.getType() + ": " + statistic.getPeriod());
            List<Map<String, String>> output = Lists.newArrayList();
            output.add(CliPrintUtils.keyValueEntry("resolution", String.valueOf(statistic.getResolution())));
            output.add(CliPrintUtils.keyValueEntry("description", statistic.getDescription()));
            output.add(CliPrintUtils.keyValueEntry("value", String.valueOf(statistic.getValue())));
            output.add(CliPrintUtils.keyValueEntry("payload", statistic.getPayload()));

            CliPrintUtils.printTable(output);
        }
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        // Input validation
        final String username = System.getProperty("username");
        final String userId = System.getProperty("userId");
        System.out.println("Username to search for is: " + username);
        System.out.println("UserId to search for is: " + userId);

        final String statisticType = System.getProperty("type");
        final String periods = System.getProperty("periods");
        List<String> periodsList = (periods != null) ? Arrays.asList(periods.split(",")) : Collections.emptyList();

        Preconditions.checkArgument(
                Strings.nullToEmpty(username).trim().length() > 0 || Strings.nullToEmpty(userId).trim().length() > 0);

        UserRepository userRepository = serviceContext.getRepository(UserRepository.class);
        StatisticDao statisticDao = serviceContext.getDao(StatisticDao.class);

        User user = Strings.isNullOrEmpty(userId) ? userRepository.findOneByUsername(username) : userRepository.findOne(
                userId);

        // Output presented to the end user.
        System.out.println("<!-- START -->");

        try {

            if (user == null) {
                System.out.println("Could not find user.");
            } else {
                System.out.println("User");
                printUserInfo(user);

                System.out.println();
                System.out.println("Statistics");
                printStatistics(statisticDao, user.getId(), statisticType, periodsList);
            }

        } finally {
            // Without this finally clause, the Salt runner will not be able to match output correctly.
            System.out.println("<!-- END   -->");
        }
    }
}
