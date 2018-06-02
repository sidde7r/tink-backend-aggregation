package se.tink.backend.system.cli.analytics;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.sourceforge.argparse4j.inf.Namespace;
import org.joda.time.LocalDate;
import rx.schedulers.Schedulers;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.concurrency.BlockingRejectedExecutionHandler;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.mail.MailSender;
import se.tink.backend.common.repository.cassandra.CredentialsEventRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.CredentialsEvent;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.Market;
import se.tink.backend.core.enums.RefreshType;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;

public class CalculateWeeklyRefreshStatistics extends ServiceContextCommand<ServiceConfiguration> {
    private static final int MAX_CREDENTIALS_IN_QUEUE = 300;
    private static final LocalDate YESTERDAY = new LocalDate().minusDays(1);
    private static final LogUtils log = new LogUtils(CalculateWeeklyRefreshStatistics.class);
    private static ConcurrentMap<String, RefreshStatistics> refreshStatistics = Maps.newConcurrentMap();

    public CalculateWeeklyRefreshStatistics() {
        super("weekly-refresh-statistics", "calculate weekly refresh statistics for swedish market");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        final Integer threadPoolSize = 30;
        final CredentialsEventRepository credentialsEventRepository = serviceContext.getRepository(
                CredentialsEventRepository.class);
        final CredentialsRepository credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        final UserRepository userRepository = serviceContext.getRepository(UserRepository.class);

        final ThreadPoolExecutor executor = new ThreadPoolExecutor(threadPoolSize, threadPoolSize, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(MAX_CREDENTIALS_IN_QUEUE));
        // Without this, we will queue up to many credentials in memory.
        executor.setRejectedExecutionHandler(new BlockingRejectedExecutionHandler());

        final AtomicInteger counter = new AtomicInteger();

        userRepository.streamAll().filter(user -> {
            int handledUsers = counter.getAndIncrement();
            if (handledUsers % 10000 == 0) {
                log.info(String.format("Processed %s users", handledUsers));
            }
            return Objects.equal(user.getProfile().getMarket(), Market.Code.SE.name());
        }).flatMapIterable(user -> credentialsRepository.findAllByUserId(user.getId()))
                .observeOn(Schedulers.from(executor)).forEach(credentials -> {
            String providerName = credentials.getProviderName();
            refreshStatistics.putIfAbsent(providerName, new RefreshStatistics(providerName));

            String credentialsId = credentials.getId();
            String userId = credentials.getUserId();
            int eventsToCollect = 10;

            List<CredentialsEvent> mostRecentEvents = credentialsEventRepository
                    .findMostRecentByUserIdAndCredentialsId(userId, credentialsId, eventsToCollect);

            ImmutableList<CredentialsEvent> filteredEvents = filterCredentialsEvents(mostRecentEvents);

            // If we have less than four events we should try to fetch more from the database
            if (filteredEvents.size() < 4) {
                filteredEvents = ensureMinimalDataAmount(
                        4, credentialsEventRepository, userId, credentialsId, eventsToCollect);
            }

            ImmutableList<CredentialsEvent> finalListOfEvents = filterCredentialsEvents(filteredEvents);

            if (finalListOfEvents.isEmpty()) {
                return;
            }

            calculateStatistics(finalListOfEvents);
        });

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Got interrupted waiting for threads to finish.", e);
        }

        MailBuilder mailBuilder = new MailBuilder(refreshStatistics.values());
        String email = mailBuilder.buildMail();

        MailSender mailSender = injector.getInstance(MailSender.class);
        mailSender.sendMessage("integration@tink.se", "Weekly refresh statistics from " + YESTERDAY.toString(),
                "integration@tink.se", "Statistics @ Tink", email);
    }

    private ImmutableList<CredentialsEvent> ensureMinimalDataAmount(int minimalAmount,
            CredentialsEventRepository credentialsEventRepository, String userId, String credentialsId,
            int eventsToCollect) {
        ImmutableList<CredentialsEvent> filteredEvents;

        do {
            eventsToCollect += 10;
            List<CredentialsEvent> mostRecentEvents = credentialsEventRepository
                    .findMostRecentByUserIdAndCredentialsId(userId, credentialsId, eventsToCollect);

            filteredEvents = filterCredentialsEvents(mostRecentEvents);

        } while (filteredEvents.size() < minimalAmount && eventsToCollect < 80);

        return filteredEvents;
    }

    private ImmutableList<CredentialsEvent> filterCredentialsEvents(List<CredentialsEvent> mostRecentEvents) {
        return FluentIterable.from(mostRecentEvents)
                .filter(RefreshStatisticsPredicates.CREDENTIALS_EVENTS_FROM_LAST_WEEK)
                .filter(RefreshStatisticsPredicates.CREDENTIALS_EVENTS_WITH_PERSISTENT_STATUS)
                .toList();
    }

    private void calculateStatistics(ImmutableList<CredentialsEvent> persistentEvents) {
        RefreshType refreshType = persistentEvents.get(0).getRefreshType();

        int counter = 0;
        String providerName = persistentEvents.get(0).getProviderName();

        statisticsLoop : for (CredentialsEvent credentialsEvent : persistentEvents) {
            if (Objects.equal(credentialsEvent.getStatus(), CredentialsStatus.UPDATED) ||
                    counter == 3) {
                switch (counter) {
                case 0:
                    incrementRefresh(providerName, refreshType);
                    break statisticsLoop;
                case 1:
                    incrementOneTimeFailure(providerName, refreshType);
                    break statisticsLoop;
                case 2:
                    incrementTwoTimesFailure(providerName, refreshType);
                    break statisticsLoop;
                case 3:
                    incrementThreeTimesFailure(providerName, refreshType);
                    break statisticsLoop;
                }
            }

            counter++;

            // Some special cases that need to be handled
            // If there is only one persistent event and it is a fail
            if (persistentEvents.size() == 1) {
                incrementOneTimeFailure(providerName, refreshType);
                break;
            }

            // If there are only two persistent events and both are fails
            if (persistentEvents.size() == 2 && counter == 2) {
                incrementTwoTimesFailure(providerName, refreshType);
                break;
            }

            // If there are only three persistent event and all three are fails
            if (persistentEvents.size() == 3 && counter == 3) {
                incrementThreeTimesFailure(providerName, refreshType);
                break;
            }
        }

        // Since the credentials events are in order the very first event should be last in the list.
        // Only increment this number if the very last event has status CREATED
        if (Iterables.getLast(persistentEvents).getStatus() == CredentialsStatus.CREATED) {
            incrementCreatedStatistics(persistentEvents);
        }
    }

    private void incrementCreatedStatistics(ImmutableList<CredentialsEvent> persistentEvents) {
        String providerName = persistentEvents.get(0).getProviderName();
        int listSize = persistentEvents.size();

        if (listSize >= 2) {
            CredentialsEvent secondToLastEvent = persistentEvents.get(listSize - 2);

            refreshStatistics.get(providerName).totalCreatedEvents.getAndIncrement();
            switch (secondToLastEvent.getStatus()) {
            case AUTHENTICATION_ERROR:
            case TEMPORARY_ERROR:
                refreshStatistics.get(providerName).firstTimeFailure.getAndIncrement();
                break;
            default:
                // nothing
            }
        }
    }

    private void incrementRefresh(String providerName, RefreshType refreshType) {
        switch (refreshType) {
        case AUTOMATIC:
            refreshStatistics.get(providerName).automaticRefreshes.getAndIncrement();
            break;
        case MANUAL:
            refreshStatistics.get(providerName).manualRefreshes.getAndIncrement();
            break;
        default:
            // nothing
        }
    }

    private void incrementOneTimeFailure(String providerName, RefreshType refreshType) {
        incrementRefresh(providerName, refreshType);
        switch (refreshType) {
        case AUTOMATIC:
            refreshStatistics.get(providerName).automaticOneTimeFailure.getAndIncrement();
            break;
        case MANUAL:
            refreshStatistics.get(providerName).manualOneTimeFailure.getAndIncrement();
            break;
        default:
            // nothing
        }
    }

    private void incrementTwoTimesFailure(String providerName, RefreshType refreshType) {
        incrementOneTimeFailure(providerName, refreshType);
        switch (refreshType) {
        case AUTOMATIC:
            refreshStatistics.get(providerName).automaticTwoTimesFailure.getAndIncrement();
            break;
        case MANUAL:
            refreshStatistics.get(providerName).manualTwoTimesFailure.getAndIncrement();
            break;
        default:
            // nothing
        }
    }

    private void incrementThreeTimesFailure(String providerName, RefreshType refreshType) {
        incrementTwoTimesFailure(providerName, refreshType);
        switch (refreshType) {
        case AUTOMATIC:
            refreshStatistics.get(providerName).automaticThreeTimesFailure.getAndIncrement();
            break;
        case MANUAL:
            refreshStatistics.get(providerName).manualThreeTimesFailure.getAndIncrement();
            break;
        default:
            // nothing
        }
    }

    private static class RefreshStatistics {
        final String providerName;
        AtomicInteger automaticRefreshes = new AtomicInteger();
        AtomicInteger automaticOneTimeFailure = new AtomicInteger();
        AtomicInteger automaticTwoTimesFailure = new AtomicInteger();
        AtomicInteger automaticThreeTimesFailure = new AtomicInteger();
        AtomicInteger manualRefreshes = new AtomicInteger();
        AtomicInteger manualOneTimeFailure = new AtomicInteger();
        AtomicInteger manualTwoTimesFailure = new AtomicInteger();
        AtomicInteger manualThreeTimesFailure = new AtomicInteger();
        AtomicInteger totalCreatedEvents = new AtomicInteger();
        AtomicInteger firstTimeFailure = new AtomicInteger();

        public RefreshStatistics(String providerName) {
            this.providerName = providerName;
        }

    }

    private static class RefreshStatisticsPredicates {
        private static final Predicate<CredentialsEvent> CREDENTIALS_EVENTS_WITH_PERSISTENT_STATUS =
                input -> {
                    if (input.getStatus() == null) {
                        return false;
                    }
                    switch (input.getStatus()) {
                    case AUTHENTICATION_ERROR:
                    case CREATED:
                    case TEMPORARY_ERROR:
                    case UPDATED:
                        return true;
                    default:
                        return false;
                    }
                };

        private static final Predicate<CredentialsEvent> CREDENTIALS_EVENTS_FROM_LAST_WEEK =
                input -> {
                    if (input.getTimestamp() == null) {
                        return false;
                    }

                    LocalDate inputDate = new LocalDate(input.getTimestamp());
                    LocalDate today = new LocalDate();
                    LocalDate oneWeekAgo = new LocalDate().minusWeeks(1);
                    return inputDate.isBefore(today) &&
                            (inputDate.isAfter(oneWeekAgo) || inputDate.isEqual(oneWeekAgo));
                };
    }

    private static class RefreshStatisticsOrderings {
        private static final Ordering<RefreshStatistics> ORDER_ON_TOTAL_AUTOMATIC_REFRESHES =
                new Ordering<RefreshStatistics>() {
                    @Override
                    public int compare(RefreshStatistics t0, RefreshStatistics t1) {
                        return ComparisonChain.start()
                                .compare(t0.automaticRefreshes.get(), t1.automaticRefreshes.get())
                                .result();
                    }
                };

        private static final Ordering<RefreshStatistics> ORDER_ON_TOTAL_MANUAL_REFRESHES =
                new Ordering<RefreshStatistics>() {
                    @Override
                    public int compare(RefreshStatistics t0, RefreshStatistics t1) {
                        return ComparisonChain.start()
                                .compare(t0.manualRefreshes.get(), t1.manualRefreshes.get())
                                .result();
                    }
                };
    }

    private class MailBuilder {
        final Collection<RefreshStatistics> refreshStatistics;

        private MailBuilder(Collection<RefreshStatistics> refreshStatistics) {
            this.refreshStatistics = refreshStatistics;
        }

        private String buildMail() {
            StringBuilder email = new StringBuilder();

            email.append("<html><body>");
            email.append(buildHeader("Weekly refresh statistics", 2));
            email.append(String.format("Statistics are based on credentials events from week %s",
                    YESTERDAY.weekOfWeekyear().get()));
            email.append(buildHeader("Manual refreshes", 3));
            email.append(buildManualTable());
            email.append(buildHeader("Automatic refreshes", 3));
            email.append(buildAutomaticTable());
            email.append("</body></html>");

            return email.toString();
        }

        private String buildRefreshTableHead() {
            StringBuilder head = new StringBuilder();

            head.append(buildTableCell("Provider"));
            head.append(buildTableCell("Total refreshes"));
            head.append(buildTableCell("One failed"));
            head.append(buildTableCell("Two failed"));
            head.append(buildTableCell("Three failed"));

            return buildTableHead(head.toString());
        }

        private String buildManualRefreshTableHead() {
            StringBuilder head = new StringBuilder();

            head.append(buildTableCell("Provider"));
            head.append(buildTableCell("Total refreshes"));
            head.append(buildTableCell("One failed"));
            head.append(buildTableCell("Two failed"));
            head.append(buildTableCell("Three failed"));
            head.append(buildTableCell("Total created"));
            head.append(buildTableCell("First time failed"));

            return buildTableHead(head.toString());
        }

        private String buildAutomaticTable() {
            StringBuilder body = new StringBuilder();

            body.append(buildRefreshTableHead());
            for (RefreshStatistics refresh : RefreshStatisticsOrderings.ORDER_ON_TOTAL_AUTOMATIC_REFRESHES.reverse()
                    .sortedCopy(refreshStatistics)) {
                AtomicInteger automaticRefresh = refresh.automaticRefreshes;

                body.append("<tr>");
                body.append(buildTableCell(refresh.providerName));
                body.append(buildTableCell(automaticRefresh));
                body.append(buildTableCell(refresh.automaticOneTimeFailure.intValue()));
                body.append(buildTableCell(refresh.automaticTwoTimesFailure.intValue()));
                body.append(buildTableCell(refresh.automaticThreeTimesFailure.intValue()));
                body.append("</tr>");
            }

            return buildTable(body.toString());
        }

        private String buildManualTable() {
            StringBuilder body = new StringBuilder();

            body.append(buildManualRefreshTableHead());
            for (RefreshStatistics refresh : RefreshStatisticsOrderings.ORDER_ON_TOTAL_MANUAL_REFRESHES.reverse()
                    .sortedCopy(refreshStatistics)) {
                AtomicInteger manualRefresh = refresh.manualRefreshes;

                body.append("<tr>");
                body.append(buildTableCell(refresh.providerName));
                body.append(buildTableCell(manualRefresh));
                body.append(buildTableCell(refresh.manualOneTimeFailure.intValue()));
                body.append(buildTableCell(refresh.manualTwoTimesFailure.intValue()));
                body.append(buildTableCell(refresh.manualThreeTimesFailure.intValue()));
                body.append(buildTableCell(refresh.totalCreatedEvents));
                body.append(buildTableCell(refresh.firstTimeFailure));
                body.append("</tr>");
            }

            return buildTable(body.toString());
        }

        private String buildHeader(String header, int size) {
            return "<h" + size + ">" + header + "</h" + size + ">";
        }

        private String buildTableHead(String body) {
            StringBuilder head = new StringBuilder();

            head.append("<tr bgcolor=\"#33CC99\">");
            head.append(body);
            head.append("</tr>");

            return head.toString();
        }

        private String buildTableCell(Object cellInput) {
            return "<td>" + cellInput + "</td>";
        }

        private String buildTable(String body) {
            StringBuilder table = new StringBuilder();

            table.append("<table style='border:2px solid black'>");
            table.append(body);
            table.append("</table>");

            return table.toString();
        }
    }
}
