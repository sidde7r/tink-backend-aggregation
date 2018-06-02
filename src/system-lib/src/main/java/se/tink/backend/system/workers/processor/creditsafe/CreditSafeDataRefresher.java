package se.tink.backend.system.workers.processor.creditsafe;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import se.tink.backend.aggregation.api.CreditSafeService;
import se.tink.backend.aggregation.client.AggregationServiceFactory;
import se.tink.backend.aggregation.rpc.AddMonitoredConsumerCreditSafeRequest;
import se.tink.backend.aggregation.rpc.ChangedConsumerCreditSafeRequest;
import se.tink.backend.aggregation.rpc.PageableConsumerCreditSafeRequest;
import se.tink.backend.aggregation.rpc.PageableConsumerCreditSafeResponse;
import se.tink.backend.aggregation.rpc.PortfolioListResponse;
import se.tink.backend.aggregation.rpc.RemoveMonitoredConsumerCreditSafeRequest;
import se.tink.backend.common.config.IDControlConfiguration;
import se.tink.backend.common.i18n.SocialSecurityNumber;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.resources.CredentialsRequestRunnableFactory;
import se.tink.backend.common.retry.RetryerBuilder;
import se.tink.backend.common.utils.RxJavaUtils;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.User;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.guavaimpl.Predicates;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

public class CreditSafeDataRefresher {

    private static final LogUtils log = new LogUtils(CreditSafeDataRefresher.class);
    private static final MetricId REFRESHED_CREDENTIALS = MetricId.newId("refreshed_credentials");
    private static final int MAX_NUM_RETRIES = 30;
    private static final String NO_CUSTOMERS_MONITORED_CODE = "16";
    private static final String NO_CUSTOMERS_CHANGED_CODE = "17";
    private static final Ordering<Integer> NATURAL = Ordering.natural();
    private static final int PAGE_SIZE = 10000;

    private final Counter refreshedCreditSafeCredentials;
    private final boolean isDryRun;
    private final int defaultChangedDays;

    private final CredentialsRequestRunnableFactory refreshFactory;
    private final AggregationServiceFactory aggregationServiceFactory;
    private final CredentialsRepository credentialsRepository;
    private final UserRepository userRepository;
    private final boolean retryUntilSuccessful;

    @Inject
    public CreditSafeDataRefresher(CredentialsRequestRunnableFactory credentialsRequestRunnableFactory,
            AggregationServiceFactory aggregationServiceFactory,
            CredentialsRepository credentialsRepository,
            UserRepository userRepository,
            MetricRegistry metricRegistry,
            IDControlConfiguration idControlConfiguration) {
        this.refreshFactory = credentialsRequestRunnableFactory;
        this.aggregationServiceFactory = aggregationServiceFactory;
        this.credentialsRepository = credentialsRepository;
        this.userRepository = userRepository;
        this.refreshedCreditSafeCredentials = metricRegistry.meter(REFRESHED_CREDENTIALS);
        this.isDryRun = idControlConfiguration.isDryRunDataRefresh();
        this.retryUntilSuccessful = idControlConfiguration.isRetryDataRefreshUntilSuccessful();
        this.defaultChangedDays = idControlConfiguration.getDaysToFetchChangesFor();
    }

    public void refreshCredentialsForIdControlUsers(Integer changedDays) {
        log.info("Refreshing credentials for ID Control users.");

        changedDays = changedDays != null ? changedDays : defaultChangedDays;

        List<String> portfolios = fetchPortfolios();

        // Find users with changed person data.
        MonitoredConsumersResponse changedResponse = findPersonNumbersWithChangedPersonData(portfolios, changedDays);

        Set<String> changedPnrs = changedResponse.consumers;

        log.info(String.format("CreditSafe delivered %s person numbers with changed data.", changedPnrs.size()));
        log.info(String.format("CreditSafe changed person numbers portfolio status: %s",
                changedResponse.getPortfolioStatusLogString()));

        Observable<User> userIdsWithIdControl = getAllUsersWithIdControl();

        // Find users with changed company data.

        Observable<User> changedFraudUser = userIdsWithIdControl.filter(
                RxJavaUtils.fromGuavaPredicate(new UsersFilter(changedPnrs, true)));

        // For all users with changed data, refresh credentials.

        changedFraudUser.forEach(user -> {
            // Refreshing all fraud credentials as some "bisnode" might actually be using CreditSafe
            List<Credentials> allCredentials = credentialsRepository.findAllByUserIdAndType(user.getId(),
                    CredentialsTypes.FRAUD);

            for (Credentials credentials : allCredentials) {
                log.info(user.getId(), credentials.getId(),
                        "Queued refresh of Fraud Credentials (" + credentials.getProviderName()
                                + ") with status: " + credentials.getStatus());

                if (!isDryRun) {

                    Runnable runnable = refreshFactory.createRefreshRunnable(user, credentials, false);
                    if (runnable != null) {
                        refreshedCreditSafeCredentials.inc();
                        runnable.run();
                    } else {
                        log.warn("Was not given a constructed Refresh Credentials Runnable");
                    }
                }
            }
        });

        log.info("refreshCredentialsForChangedUsers: All done");

        // For all uses that weren't changed, set new updated time stamp
        // to reflect data has been checked, that is updated.

        Observable<User> unchangedFraudUser = userIdsWithIdControl.filter(
                RxJavaUtils.fromGuavaPredicate(new UsersFilter(changedPnrs, false)));

        unchangedFraudUser.forEach(user -> {
            List<Credentials> allCredentials = credentialsRepository.findAllByUserIdAndType(user.getId(),
                    CredentialsTypes.FRAUD);

            Date now = new Date();

            for (Credentials credentials : allCredentials) {
                log.debug(user.getId(), credentials.getId(),
                        "Updating updated timestamp unchanged ID Control credentials");

                if (!isDryRun) {
                    credentials.setUpdated(now);
                    credentialsRepository.save(credentials);
                }
            }
        });

        log.info("refreshCredentialsForUnchangedUsers: All done");
    }

    public void cleanUpMonitoredConsumers() {
        log.info("Clean up monitoring for customers.");

        List<String> portfolios = fetchPortfolios();

        MonitoredConsumersResponse monitoredConsumersResponse = findAllMonitoredPersonNumbers(portfolios);

        if (monitoredConsumersResponse.consumers.size() == 0) {
            log.info("CreditSafe delivered 0 monitored person numbers.");
            return;
        }

        log.info("CreditSafe delivered " + monitoredConsumersResponse.consumers.size()
                + " total monitored person numbers.");
        log.info("CreditSafe portfolio status: " + monitoredConsumersResponse.getPortfolioStatusLogString());

        Integer min = NATURAL.min(monitoredConsumersResponse.portfolioSizes);
        int indexOfLeastMonitored = monitoredConsumersResponse.portfolioSizes.indexOf(min);
        String portfolioWithLeastMonitored = monitoredConsumersResponse.portfolioNames.get(indexOfLeastMonitored);

        log.info("Chose " + portfolioWithLeastMonitored + " to put new users in");

        Set<String> fraudPnrs = getAllIdControlPersonNumbers();
        log.info("Found " + fraudPnrs.size() + " person numbers in our database.");

        // AddMonitoring for unmonitored
        Iterable<String> allUnmonitoredFraudPnrs = Iterables.filter(fraudPnrs,
                new PersonNumberFilter(monitoredConsumersResponse.consumers, false));

        log.info("Adding CreditSafe consumer monitoring for " + Iterables.size(allUnmonitoredFraudPnrs) + " pnrs.");
        for (String fraudPnr : allUnmonitoredFraudPnrs) {

            if (addConsumerMonitoring(portfolioWithLeastMonitored, fraudPnr)) {
                log.debug("Added CreditSafe consumer monitoring for: " + fraudPnr);
            } else {
                log.warn("Was not able to add CreditSafe consumer monitoring for: " + fraudPnr);
            }
        }

        // Remove monitoring of deleted users and users that inactivated fraud
        Iterable<String> allNonExistingMonitoredPnrs = Iterables.filter(monitoredConsumersResponse.consumers,
                new PersonNumberFilter(fraudPnrs, false));

        log.info("Removing CreditSafe consumer monitoring for " + Iterables.size(allNonExistingMonitoredPnrs)
                + " pnrs.");
        for (String monitoredPnr : allNonExistingMonitoredPnrs) {

            if (removeConsumerMonitoring(portfolios, monitoredPnr)) {
                log.debug("Removed CreditSafe consumer monitoring for: " + monitoredPnr);
            } else {
                log.warn("Was not able to remove CreditSafe consumer monitoring for: " + monitoredPnr);
            }
        }
    }

    private List<String> fetchPortfolios() {
        Retryer<PortfolioListResponse> retryer = RetryerBuilder
                .<PortfolioListResponse>newBuilder(log, "fetching portfolios")
                .withStopStrategy(StopStrategies.stopAfterAttempt(MAX_NUM_RETRIES))
                .withWaitStrategy(WaitStrategies.fixedWait(500, TimeUnit.MILLISECONDS))
                .retryIfResult(
                        input -> input == null || input.getPortfolios() == null || input.getPortfolios().size() == 0)
                .build();


        try {
            CreditSafeService creditSafeService = aggregationServiceFactory.getCreditSafeService();
            PortfolioListResponse response = retryer.call(creditSafeService::listPortfolios);
            return response.getPortfolios();
        } catch (ExecutionException e) {
            throw new RuntimeException("Could not fetch portfolio list from credit safe.", e);
        } catch (RetryException e) {
            throw new RuntimeException("Could not fetch portfolio list from credit safe. Tried too many times.", e);
        }
    }

    private MonitoredConsumersResponse findPersonNumbersWithChangedPersonData(List<String> portfolios,
            int changedDays) {

        MonitoredConsumersResponse response = new MonitoredConsumersResponse();
        response.portfolioNames = Lists.newArrayListWithCapacity(portfolios.size());
        response.portfolioSizes = Lists.newArrayListWithCapacity(portfolios.size());
        response.consumers = Sets.newHashSet();

        for (String portfolio : portfolios) {
            ChangedConsumerCreditSafeRequest initial = new ChangedConsumerCreditSafeRequest(
                    portfolio, PAGE_SIZE, 1, changedDays);

            Set<String> set = fetchMonitoredConsumers(initial);

            response.portfolioNames.add(portfolio);
            response.portfolioSizes.add(set.size());
            response.consumers.addAll(set);
        }

        return response;
    }

    private MonitoredConsumersResponse findAllMonitoredPersonNumbers(List<String> portfolios) {

        MonitoredConsumersResponse response = new MonitoredConsumersResponse();
        response.portfolioNames = Lists.newArrayListWithCapacity(portfolios.size());
        response.portfolioSizes = Lists.newArrayListWithCapacity(portfolios.size());
        response.consumers = Sets.newHashSet();

        for (String portfolio : portfolios) {
            PageableConsumerCreditSafeRequest initial = new PageableConsumerCreditSafeRequest(
                    portfolio, PAGE_SIZE, 1);

            Set<String> set = fetchMonitoredConsumers(initial);

            response.portfolioNames.add(portfolio);
            response.portfolioSizes.add(set.size());
            response.consumers.addAll(set);
        }

        return response;
    }

    private Set<String> fetchMonitoredConsumers(final PageableConsumerCreditSafeRequest initialRequest) {
        Set<String> pnrs = Sets.newHashSet();

        int start = 1;
        int total = Integer.MAX_VALUE;

        while (start < total) {

            final PageableConsumerCreditSafeRequest pageRequest = initialRequest;
            pageRequest.setPageStart(start);

            final Callable<PageableConsumerCreditSafeResponse> fetchChangedConsumers = () -> {
                PageableConsumerCreditSafeResponse response;

                if (initialRequest instanceof ChangedConsumerCreditSafeRequest) {
                    ChangedConsumerCreditSafeRequest changedRequest = (ChangedConsumerCreditSafeRequest) pageRequest;

                    response = aggregationServiceFactory.getCreditSafeService().listChangedConsumers(changedRequest);
                } else {
                    response = aggregationServiceFactory.getCreditSafeService().listMonitoredConsumers(pageRequest);
                }

                if (response.getConsumers() == null) {
                    log.error("CreditSafe delivered a non-ok response for monitored consumers.");
                } else if (response.getConsumers().size() == 0) {
                    log.info("CreditSafe delivered 0 monitored person numbers.");
                }
                return response;
            };

            PageableConsumerCreditSafeResponse consumerResponse;
            try {
                int retries = retryUntilSuccessful ? MAX_NUM_RETRIES : 1;
                consumerResponse = createRetryer(retries).call(fetchChangedConsumers);
                List<String> consumers = consumerResponse.getConsumers();

                pnrs.addAll(consumers != null ? consumers : Lists.<String>newArrayList());
                start = consumerResponse.getPageEnd() + 1;
                total = consumerResponse.getTotalPortfolioSize();

            } catch (ExecutionException e) {
                throw new RuntimeException("Could not fetch from credit safe.", e);
            } catch (RetryException e) {
                throw new RuntimeException("Could not fetch from credit safe. Tried too many times.", e);
            }
        }
        return pnrs;
    }

    private boolean addConsumerMonitoring(String portfolio, String pnr) {
        SocialSecurityNumber.Sweden socialSecurityNumber = new SocialSecurityNumber.Sweden(pnr);
        if (!socialSecurityNumber.isValid()) {
            return false;
        }

        AddMonitoredConsumerCreditSafeRequest request = new AddMonitoredConsumerCreditSafeRequest();
        request.setPortfolio(portfolio);
        request.setPnr(socialSecurityNumber.asString());
        try {
            if (!isDryRun) {
                aggregationServiceFactory.getCreditSafeService().addConsumerMonitoring(request);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean removeConsumerMonitoring(List<String> portfolios, String pnr) {
        SocialSecurityNumber.Sweden socialSecurityNumber = new SocialSecurityNumber.Sweden(pnr);
        if (!socialSecurityNumber.isValid()) {
            return false;
        }

        RemoveMonitoredConsumerCreditSafeRequest request = new RemoveMonitoredConsumerCreditSafeRequest();
        request.setPortfolios(portfolios);
        request.setPnr(socialSecurityNumber.asString());
        try {
            if (!isDryRun) {
                aggregationServiceFactory.getCreditSafeService().removeConsumerMonitoring(request);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Set<String> getAllIdControlPersonNumbers() {

        Iterable<String> iterable = getAllUsersWithIdControl().map(user -> {
            String fraudPnr = user.getProfile().getFraudPersonNumber();
            if (fraudPnr == null) {
                throw new IllegalStateException("Caller should filter out users without Fraud first");
            }
            return fraudPnr;
        }).toBlocking().toIterable();
        return Sets.newHashSet(iterable);
    }

    private Observable<User> getAllUsersWithIdControl() {
        return userRepository.streamAll().filter(
                RxJavaUtils.fromGuavaPredicate(Predicates.USERS_WITH_FRAUD_PERSONNUMBER));
    }

    private static Retryer<PageableConsumerCreditSafeResponse> createRetryer(int maxRetries) {
        return RetryerBuilder
                .<PageableConsumerCreditSafeResponse>newBuilder(log, "fetching consumer response")
                .withStopStrategy(StopStrategies.stopAfterAttempt(maxRetries))
                .withWaitStrategy(WaitStrategies.fixedWait(500, TimeUnit.MILLISECONDS))
                .retryIfResult(
                        input -> input.getErrorCode() != null && !input.getErrorCode().equals(NO_CUSTOMERS_CHANGED_CODE)
                                && !input.getErrorCode().equals(NO_CUSTOMERS_MONITORED_CODE)).build();
    }

    private class PersonNumberFilter implements Predicate<String> {

        private Set<String> pnrs;
        private boolean include;

        PersonNumberFilter(Set<String> pnrs, boolean include) {
            this.pnrs = pnrs;
            this.include = include;
        }

        @Override
        public boolean apply(String pnr) {
            if (include) {
                return pnrs.contains(pnr);
            } else {
                return !pnrs.contains(pnr);
            }
        }
    }

    private class UsersFilter implements Predicate<User> {

        private Set<String> pnrs;
        private boolean include;

        UsersFilter(Set<String> pnrs, boolean include) {
            this.pnrs = pnrs;
            this.include = include;
        }

        @Override
        public boolean apply(User user) {
            String pnr = user.getProfile().getFraudPersonNumber();
            if (include) {
                return pnrs.contains(pnr);
            } else {
                return !pnrs.contains(pnr);
            }
        }
    }

    private class MonitoredConsumersResponse {
        private List<String> portfolioNames;
        private List<Integer> portfolioSizes;
        private Set<String> consumers;

        public String getPortfolioStatusLogString() {
            StringBuilder sb = new StringBuilder();
            sb.append('{');
            for (int i = 0; i < portfolioNames.size(); i++) {
                String name = portfolioNames.get(i);
                Integer size = portfolioSizes.get(i);

                sb.append(" {").append(name).append(": ").append(size).append('}');
                if (i != portfolioNames.size() - 1) {
                    sb.append(',');
                }
            }
            sb.append(" }");

            return sb.toString();
        }
    }

}
