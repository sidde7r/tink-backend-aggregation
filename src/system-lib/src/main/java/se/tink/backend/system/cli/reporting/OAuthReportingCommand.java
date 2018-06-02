package se.tink.backend.system.cli.reporting;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.cassandra.CredentialsEventRepository;
import se.tink.backend.common.repository.cassandra.OAuth2ClientEventRepository;
import se.tink.backend.common.repository.cassandra.TransferEventRepository;
import se.tink.backend.common.repository.mysql.main.OAuth2ClientRepository;
import se.tink.backend.common.repository.mysql.main.UserOriginRepository;
import se.tink.backend.core.CredentialsEvent;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.OAuth2ClientEvent;
import se.tink.backend.core.UserOrigin;
import se.tink.backend.core.oauth2.OAuth2Client;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.TransferEvent;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.uuid.UUIDUtils;

public class OAuthReportingCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(OAuthReportingCommand.class);

    private static final String METRIC_REFRESHES = "refreshes";
    private static final String METRIC_TRANSFERS = "transfers";

    private static final String KEY_CREATED_USER = "created-users";
    private static final String KEY_CREATED_CREDENTIALS = "created-credentials";
    private static final String KEY_SUCCESSFULLY_UPDATES = "successfully-updates";
    private static final String KEY_FAILED_UPDATES = "failed-updates";

    private static final String KEY_CREATED_TRANSFER = "created-transfer";
    private static final String KEY_SUCCESSFUL_TRANSFER_COUNT = "successful-transfer-count";
    private static final String KEY_SUCCESSFUL_TRANSFER_AMOUNT = "successful-transfer-amount";
    private static final String KEY_FAILED_TRANSFER_COUNT = "failed-transfer-count";
    private static final String KEY_FAILED_TRANSFER_AMOUNT = "failed-transfer-amount";

    private static final ImmutableSet<CredentialsStatus> UPDATED_STATUSES = ImmutableSet.of(CredentialsStatus.UPDATED);
    private static final ImmutableSet<CredentialsStatus> ERROR_STATUSES = ImmutableSet.of(
            CredentialsStatus.AUTHENTICATION_ERROR, CredentialsStatus.TEMPORARY_ERROR);

    private static final ImmutableSet<SignableOperationStatuses> EXECUTED_STATUSES = ImmutableSet
            .of(SignableOperationStatuses.EXECUTED);
    private static final ImmutableSet<SignableOperationStatuses> FAILED_STATUSES = ImmutableSet.of(
            SignableOperationStatuses.FAILED, SignableOperationStatuses.CANCELLED);

    private static final Function<CredentialsEvent, UUID> CREDENTIALS_EVENT_TO_CREDENTIALS_ID = CredentialsEvent::getCredentialsId;

    private static final Function<TransferEvent, UUID> TRANSFER_EVENT_TO_CREDENTIALS_ID = TransferEvent::getTransferId;

    private static final Ordering<Map.Entry<String, AtomicLong>> MESSAGES_BY_OCCURENCE = Ordering.from(
            (e1, e2) -> (int) (e1.getValue().get() - e2.getValue().get()));

    private boolean debug;
    private int topDebugMessages;

    private final Map<String, Map<String, AtomicLong>> counters;
    private final MultiKeyMap<String, AtomicLong> debugCounters;
    private final MultiKeyMap<String, Map<String, AtomicLong>> debugMessageCounters;

    private CredentialsEventRepository credentialsEventRepository;
    private TransferEventRepository transferEventRepository;

    public OAuthReportingCommand() {
        super("oauth-reporting", "");

        counters = Maps.newHashMap();
        debugCounters = new MultiKeyMap<>();
        debugMessageCounters = new MultiKeyMap<>();
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        final OAuth2ClientRepository oAuth2ClientRepository = serviceContext.getRepository(OAuth2ClientRepository.class);
        final OAuth2ClientEventRepository oAuth2ClientEventRepository = serviceContext.getRepository(OAuth2ClientEventRepository.class);
        final UserOriginRepository userOriginRepository = serviceContext.getRepository(UserOriginRepository.class);

        credentialsEventRepository = serviceContext.getRepository(CredentialsEventRepository.class);
        transferEventRepository = serviceContext.getRepository(TransferEventRepository.class);

        String metric = System.getProperty("metric", METRIC_REFRESHES);
        String clientId = System.getProperty("clientId");
        String start = System.getProperty("startDate");
        String end = System.getProperty("endDate");
        debug = Boolean.getBoolean("debug");
        topDebugMessages = Integer.getInteger("topDebugMessages", 3);

        if (Strings.isNullOrEmpty(clientId) || Strings.isNullOrEmpty(start) || Strings.isNullOrEmpty(end)) {
            log.warn("Need 'clientId', 'startDate' and 'endDate' input. Exiting.");
            return;
        }

        if (!UUIDUtils.isValidTinkUUID(clientId)) {
            clientId = UUIDUtils.toTinkUUID(UUID.fromString(clientId));
        }

        OAuth2Client oAuth2Client = oAuth2ClientRepository.findOne(clientId);

        if (oAuth2Client == null) {
            log.warn("OAuth2Client not found. Exiting.");
            return;
        }

        Date startDate = DateUtils.setInclusiveStartTime(ThreadSafeDateFormat.FORMATTER_DAILY.parse(start));
        Date endDate = DateUtils.setInclusiveEndTime(ThreadSafeDateFormat.FORMATTER_DAILY.parse(end));

        List<OAuth2ClientEvent> events = oAuth2ClientEventRepository.findAllByClientIdAndDateBetween(
                UUIDUtils.fromTinkUUID(oAuth2Client.getId()), startDate, endDate);

        for (OAuth2ClientEvent event : events) {

            if (Objects.equals(event.getType(), OAuth2ClientEvent.Type.USER_REGISTERED)) {

                Optional<String> userId = event.getPayloadValue(OAuth2ClientEvent.PayloadKey.USERID);

                if (userId.isPresent()) {

                    UserOrigin userOrigin = userOriginRepository.findOneByUserId(userId.get());

                    switch (metric) {
                    case METRIC_REFRESHES:
                        countRefreshes(userId.get(), userOrigin);
                        break;
                    case METRIC_TRANSFERS:
                        countTransfers(userId.get(), userOrigin);
                        break;
                    }
                }
            }
        }

        String startSecond = ThreadSafeDateFormat.FORMATTER_SECONDS.format(startDate);
        String endSecond = ThreadSafeDateFormat.FORMATTER_SECONDS.format(endDate);

        System.out.println("<!-- START -->");
        System.out.println("==============> OAUTH REPORTING <==============");
        System.out.println(String.format("OAuth2Client: %s (%s)", oAuth2Client.getName(), oAuth2Client.getId()));
        System.out.println(String.format("Reporting period: %s   -   %s", startSecond, endSecond));
        System.out.println(String.format("Reporting metric: %s", metric));
        System.out.println("===============================================");
        System.out.println("");
        for (String originKey : counters.keySet()) {
            System.out.println(String.format("For origin: %s", originKey));

            switch (metric) {
            case METRIC_REFRESHES:
                System.out.println(String.format("\tRegistered users:\t%s", getCounter(originKey, KEY_CREATED_USER).get()));
                System.out.println(String.format("\tCreated credentials:\t%s", getCounter(originKey, KEY_CREATED_CREDENTIALS).get()));
                System.out.println(String.format("\tSuccessfully refreshed:\t%s", getCounter(originKey, KEY_SUCCESSFULLY_UPDATES).get()));
                System.out.println(String.format("\tFailed refreshed:\t%s", getCounter(originKey, KEY_FAILED_UPDATES).get()));
                break;
            case METRIC_TRANSFERS:
                System.out.println(String.format("\tRegistered users:\t%s", getCounter(originKey, KEY_CREATED_USER).get()));
                System.out.println(String.format("\tCreated credentials:\t%s", getCounter(originKey, KEY_CREATED_CREDENTIALS).get()));
                System.out.println(String.format("\tCreated transfer:\t%s", getCounter(originKey, KEY_CREATED_TRANSFER).get()));
                System.out.println(String.format("\tSuccessful transfer (count):\t%s", getCounter(originKey, KEY_SUCCESSFUL_TRANSFER_COUNT).get()));
                System.out.println(String.format("\tSuccessful transfer (amount):\t%s", getCounter(originKey, KEY_SUCCESSFUL_TRANSFER_AMOUNT).get()));
                System.out.println(String.format("\tFailed transfer (count):\t%s", getCounter(originKey, KEY_FAILED_TRANSFER_COUNT).get()));
                System.out.println(String.format("\tFailed transfer (amount):\t%s", getCounter(originKey, KEY_FAILED_TRANSFER_AMOUNT).get()));
                break;
            }
        }

        if (debug) {
            System.out.println("");
            System.out.println("===============================================");
            System.out.println(String.format("Debugging failed %s", metric));
            System.out.println("===============================================");
            System.out.println("");

            printDebugInfo();
        }

        System.out.println("<!-- END   -->");
    }

    private void printDebugInfo() {

        Set<String> origins = getOriginSet(debugCounters);
        Set<String> providers = getProviderSet(debugCounters);
        Set<String> statuses = getStatusSet(debugCounters);

        for (String origin : origins) {
            System.out.println(String.format("For origin: %s", origin));

            for (String provider : providers) {
                System.out.println(String.format("\t%s", provider));

                for (String status : statuses) {
                    MultiKey<String> key = new MultiKey<>(origin, provider, status);

                    if (debugCounters.containsKey(key)) {
                        System.out.println(String.format("\t\t%s: %d", status, debugCounters.get(key).get()));

                        if (debugMessageCounters.containsKey(key)) {
                            Map<String, AtomicLong> messageMap = debugMessageCounters.get(key);
                            List<Map.Entry<String, AtomicLong>> topMessages = MESSAGES_BY_OCCURENCE.greatestOf(
                                    messageMap.entrySet(), topDebugMessages);

                            for (Map.Entry<String, AtomicLong> entry : topMessages) {
                                System.out.println(String.format("\t\t\t%d %s", entry.getValue().get(), entry.getKey()));
                            }
                        }
                    }
                }
            }
        }
    }

    private String getMediaSource(UserOrigin userOrigin) {

        if (userOrigin == null || Strings.isNullOrEmpty(userOrigin.getMediaSource())) {
            return "undefined";
        }

        return userOrigin.getMediaSource().toLowerCase();
    }

    private void countRefreshes(String userId, UserOrigin userOrigin) {
        String mediaSource = getMediaSource(userOrigin);

        incrementCounter(mediaSource, KEY_CREATED_USER);

        ImmutableListMultimap<UUID, CredentialsEvent> eventByCredentialsId = FluentIterable
                .from(credentialsEventRepository.findByUserId(userId))
                .index(CREDENTIALS_EVENT_TO_CREDENTIALS_ID);

        for (UUID credentialsId : eventByCredentialsId.keySet()) {
            incrementCounter(mediaSource, KEY_CREATED_CREDENTIALS);

            List<CredentialsEvent> credentialsEvents = eventByCredentialsId.get(credentialsId);

            if (credentialsEvents.stream().anyMatch(getCredentialsEventByStatus(UPDATED_STATUSES))) {
                incrementCounter(mediaSource, KEY_SUCCESSFULLY_UPDATES);
            } else if (credentialsEvents.stream().anyMatch(getCredentialsEventByStatus(ERROR_STATUSES))) {
                incrementCounter(mediaSource, KEY_FAILED_UPDATES);

                if (debug) {
                    CredentialsEvent event = credentialsEvents.stream()
                            .filter(getCredentialsEventByStatus(ERROR_STATUSES))
                            .findFirst().get();

                    incrementDebugCounter(mediaSource, event);
                }
            }
        }
    }

    private void countTransfers(String userId, UserOrigin userOrigin) {
        String mediaSource = getMediaSource(userOrigin);

        incrementCounter(mediaSource, KEY_CREATED_USER);

        ImmutableListMultimap<UUID, CredentialsEvent> eventByCredentialsId = FluentIterable
                .from(credentialsEventRepository.findByUserId(userId))
                .index(CREDENTIALS_EVENT_TO_CREDENTIALS_ID);

        ImmutableListMultimap<UUID, TransferEvent> eventByTransferId = FluentIterable
                .from(transferEventRepository.findAllByUserId(UUIDUtils.fromTinkUUID(userId)))
                .index(TRANSFER_EVENT_TO_CREDENTIALS_ID);

        for (UUID credentialsId : eventByCredentialsId.keySet()) {
            incrementCounter(mediaSource, KEY_CREATED_CREDENTIALS);
        }

        for (UUID transferId : eventByTransferId.keySet()) {
            incrementCounter(mediaSource, KEY_CREATED_TRANSFER);

            List<TransferEvent> transferEvents = eventByTransferId.get(transferId);

            Optional<TransferEvent> successEvent = transferEvents.stream().filter(getTransferEventByStatus
                    (EXECUTED_STATUSES)).findFirst();
            Optional<TransferEvent> failedEvent = transferEvents.stream()
                    .filter(getTransferEventByStatus(FAILED_STATUSES)).findFirst();

            if (successEvent.isPresent()) {
                incrementCounter(mediaSource, KEY_SUCCESSFUL_TRANSFER_COUNT);
                incrementCounter(mediaSource, KEY_SUCCESSFUL_TRANSFER_AMOUNT, successEvent.get().getAmount().longValue());
            } else if (failedEvent.isPresent()) {
                incrementCounter(mediaSource, KEY_FAILED_TRANSFER_COUNT);
                incrementCounter(mediaSource, KEY_FAILED_TRANSFER_AMOUNT, failedEvent.get().getAmount().longValue());

                if (debug) {
                    ImmutableList<CredentialsEvent> events = eventByCredentialsId.get(
                            failedEvent.get().getCredentialsId());

                    CredentialsEvent first = Iterables.getFirst(events, null);
                    String providerName = getOrDefault(first != null ? first.getProviderName() : null);

                    incrementDebugCounter(mediaSource, providerName, failedEvent.get());
                }
            }
        }
    }

    private void incrementCounter(String origin, String counter) {
        AtomicLong atomicLong = getCounter(origin, counter);
        atomicLong.incrementAndGet();
    }

    private void incrementCounter(String origin, String counter, long delta) {
        AtomicLong atomicLong = getCounter(origin, counter);
        atomicLong.addAndGet(delta);
    }

    private void incrementDebugCounter(String origin, CredentialsEvent event) {
        AtomicLong debugCounter = getDebugCounter(origin, event);
        AtomicLong debugMessageCounter = getDebugMessageCounter(origin, event);
        debugCounter.incrementAndGet();
        debugMessageCounter.incrementAndGet();
    }

    private void incrementDebugCounter(String origin, String providerName, TransferEvent event) {
        AtomicLong debugCounter = getDebugCounter(origin, providerName, event);
        AtomicLong debugMessageCounter = getDebugMessageCounter(origin, providerName, event);
        debugCounter.incrementAndGet();
        debugMessageCounter.incrementAndGet();
    }

    private AtomicLong getCounter(String origin, String counter) {
        if (!counters.containsKey(origin)) {
            Map<String, AtomicLong> map = Maps.newHashMap();

            map.put(KEY_CREATED_USER, new AtomicLong(0));
            map.put(KEY_CREATED_CREDENTIALS, new AtomicLong(0));
            map.put(KEY_SUCCESSFULLY_UPDATES, new AtomicLong(0));
            map.put(KEY_FAILED_UPDATES, new AtomicLong(0));
            map.put(KEY_CREATED_TRANSFER, new AtomicLong(0));
            map.put(KEY_SUCCESSFUL_TRANSFER_COUNT, new AtomicLong(0));
            map.put(KEY_SUCCESSFUL_TRANSFER_AMOUNT, new AtomicLong(0));
            map.put(KEY_FAILED_TRANSFER_COUNT, new AtomicLong(0));
            map.put(KEY_FAILED_TRANSFER_AMOUNT, new AtomicLong(0));

            counters.put(origin, map);
        }

        return counters.get(origin).get(counter);
    }

    private AtomicLong getDebugCounter(String origin, CredentialsEvent event) {
        MultiKey<String> key = new MultiKey<>(
                origin, getOrDefault(event.getProviderName()), event.getStatus().toString());

        if (!debugCounters.containsKey(key)) {
            debugCounters.put(key, new AtomicLong(0));
        }

        return debugCounters.get(key);
    }

    private AtomicLong getDebugCounter(String origin, String providerName, TransferEvent event) {
        MultiKey<String> key = new MultiKey<>(
                origin, getOrDefault(providerName), event.getStatus().toString());

        if (!debugCounters.containsKey(key)) {
            debugCounters.put(key, new AtomicLong(0));
        }

        return debugCounters.get(key);
    }

    private AtomicLong getDebugMessageCounter(String origin, CredentialsEvent event) {
        MultiKey<String> key = new MultiKey<>(
                origin, getOrDefault(event.getProviderName()), event.getStatus().toString());
        String messageKey = getOrDefault(event.getMessage());

        if (!debugMessageCounters.containsKey(key)) {
            Map<String, AtomicLong> map = Maps.newHashMap();
            debugMessageCounters.put(key, map);
        }

        Map<String, AtomicLong> messageMap = debugMessageCounters.get(key);

        if (!messageMap.containsKey(messageKey)) {
            messageMap.put(messageKey, new AtomicLong(0));
        }

        return messageMap.get(messageKey);
    }

    private AtomicLong getDebugMessageCounter(String origin, String providerName, TransferEvent event) {
        MultiKey<String> key = new MultiKey<>(
                origin, getOrDefault(providerName), event.getStatus().toString());
        String messageKey = getOrDefault(event.getStatusMessage());

        if (!debugMessageCounters.containsKey(key)) {
            Map<String, AtomicLong> map = Maps.newHashMap();
            debugMessageCounters.put(key, map);
        }

        Map<String, AtomicLong> messageMap = debugMessageCounters.get(key);

        if (!messageMap.containsKey(messageKey)) {
            messageMap.put(messageKey, new AtomicLong(0));
        }

        return messageMap.get(messageKey);
    }

    private Predicate<TransferEvent> getTransferEventByStatus(final Set<SignableOperationStatuses> status) {
        return ev -> status.contains(ev.getStatus());
    }

    private Predicate<CredentialsEvent> getCredentialsEventByStatus(final Set<CredentialsStatus> status) {
        return ev -> status.contains(ev.getStatus());
    }

    private static String getOrDefault(String str) {
        if (Strings.isNullOrEmpty(str)) {
            return "undefined";
        }
        return str;
    }

    private Set<String> getOriginSet(MultiKeyMap<String, ? extends Object> map) {
        Set<String> set = Sets.newHashSet();
        for (MultiKey<? extends String> key : map.keySet()) {
            set.add(key.getKey(0));
        }
        return set;
    }

    private Set<String> getProviderSet(MultiKeyMap<String, ? extends Object> map) {
        Set<String> set = Sets.newHashSet();
        for (MultiKey<? extends String> key : map.keySet()) {
            set.add(key.getKey(1));
        }
        return set;
    }

    private static Set<String> getStatusSet(MultiKeyMap<String, ? extends Object> map) {
        Set<String> set = Sets.newHashSet();
        for (MultiKey<? extends String> key : map.keySet()) {
            set.add(key.getKey(2));
        }
        return set;
    }
}
