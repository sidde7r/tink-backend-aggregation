package se.tink.backend.system.cli.statistics.market;

import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.assertj.core.util.Maps;
import org.assertj.core.util.Strings;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Provider;
import se.tink.backend.core.ProviderStatuses;
import se.tink.backend.core.User;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;
import se.tink.libraries.date.DateUtils;
import static java.util.stream.Collectors.toCollection;

public class FetchMarketStatisticsCommand extends ServiceContextCommand<ServiceConfiguration> {
    private static final Pattern AGENT_NAME_PATTERN = Pattern.compile("\\.(\\w*$)");
    private static final String MARKET_FIELD = "market";
    private static final String DATE_FIELD = "date";

    private Map<String, String> agentNameByProviderName;
    private ConcurrentMap<String, Integer> totalCountByProviderName;
    private ConcurrentMap<String, Map<String, Integer>> credStatusCountByProviderName;


    public FetchMarketStatisticsCommand() {
        super("fetch-market-statistics",
                "Fetch market statistics about number of users, credentials and credentials statuses.");
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("-m", "--market")
                .dest(MARKET_FIELD)
                .type(String.class)
                .required(true)
                .help("Market to list statistics for");

        subparser.addArgument("-d", "--date")
                .dest(DATE_FIELD)
                .type(String.class)
                .required(false)
                .help("List statistics for users created on and after given date");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        // Input
        final String market = namespace.get(MARKET_FIELD);
        final Date inputDate = getDateFromInput(namespace);

        // Instantiate data structures and get repositories
        agentNameByProviderName = Maps.newHashMap();
        totalCountByProviderName = Maps.newConcurrentHashMap();
        credStatusCountByProviderName = Maps.newConcurrentHashMap();
        final AtomicInteger userCounter = new AtomicInteger();
        final UserRepository userRepository = injector.getInstance(UserRepository.class);
        final CredentialsRepository credentialsRepository = injector.getInstance(CredentialsRepository.class);

        // Get enabled provider names to compare to when going through credentials
        final Set<String> enabledProviderNames = getEnabledProviderNames(injector, serviceContext, market);

        userRepository.streamAll()
                .compose(new CommandLineInterfaceUserTraverser(4))
                .filter(user -> isRelevantUserByMarketAndDate(user, market, inputDate))
                .forEach(user -> {
                    userCounter.incrementAndGet();
                    credentialsRepository.findAllByUserId(user.getId()).stream()
                            .filter(credentials -> enabledProviderNames.contains(credentials.getProviderName()))
                            .forEach(this::addCredentialsToStatistics);
                });

        StatisticsPrinter.printMarketUserNumbers(userRepository, market, namespace.get(DATE_FIELD), userCounter.get());
        StatisticsPrinter.printProviderUserCountsTable(
                totalCountByProviderName, credStatusCountByProviderName, agentNameByProviderName);
    }

    private void addCredentialsToStatistics(Credentials credentials) {
        String providerName = credentials.getProviderName();
        String credentialStatus = credentials.getStatus().name();

        totalCountByProviderName.put(providerName, totalCountByProviderName.getOrDefault(providerName, 0) + 1);

        Map<String, Integer> countByStatus = credStatusCountByProviderName
                .getOrDefault(providerName, new LinkedHashMap<>());
        countByStatus.put(credentialStatus, countByStatus.getOrDefault(credentialStatus, 0) + 1);
        credStatusCountByProviderName.put(providerName, countByStatus);
    }

    private Date getDateFromInput(Namespace namespace) {
        String dateInputString = namespace.get(DATE_FIELD);

        if (!Strings.isNullOrEmpty(dateInputString)) {
            return DateUtils.parseDate(dateInputString);
        }

        return null;
    }

    private Set<String> getEnabledProviderNames(Injector injector, ServiceContext serviceContext, String market) {
        final ProviderRepository providerRepository = injector.getInstance(ProviderRepository.class);
        final AggregationControllerCommonClient aggregationControllerClient = injector.getInstance(
                AggregationControllerCommonClient.class);

        List<Provider> providers;

        if (serviceContext.isProvidersOnAggregation()) {
            providers = aggregationControllerClient.listProvidersByMarket(market);
        } else {
            providers = providerRepository.findProvidersByMarket(market);
        }

        return providers.stream()
                .filter(provider -> ProviderStatuses.ENABLED.equals(provider.getStatus()))
                .map(p -> {
                    agentNameByProviderName.put(p.getName(), getAgentNameFromProvider(p));
                    return p.getName();
                })
                .collect(toCollection(HashSet::new));
    }

    private String getAgentNameFromProvider(Provider provider) {
        Matcher matcher = AGENT_NAME_PATTERN.matcher(provider.getClassName());
        Preconditions.checkState(matcher.find(), "Agent name could not be parsed from provider class name");
        return matcher.group(1);
    }

    private boolean isRelevantUserByMarketAndDate(User user, String market, Date inputDate) {
        if (inputDate == null) {
            return isInMarket(user, market);
        }

        return isInMarket(user, market) && user.getCreated().after(inputDate);
    }

    private boolean isInMarket(User user, String market) {
        return market.equalsIgnoreCase(user.getProfile().getMarket());
    }
}
