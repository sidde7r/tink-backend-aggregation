package se.tink.backend.system.cli.extraction;

import com.clearspring.analytics.stream.Counter;
import com.clearspring.analytics.stream.StreamSummary;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Injector;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import io.dropwizard.setup.Bootstrap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import net.sourceforge.argparse4j.inf.Namespace;
import rx.Observable;
import rx.observables.ConnectableObservable;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.config.StringConverterFactory;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Transaction;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;
import se.tink.backend.system.cli.helper.traversal.ThreadPoolObserverTransformer;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.http.client.WebResourceFactory;

public class ExtractDescriptionsToCategor extends ServiceContextCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(ExtractDescriptionsToCategor.class);

    /**
     * Number of descriptions held in memory.
     */
    private static final int DEFAULT_TOPK_SIZE = 100000;
    public static final int DEFAULT_DESCRIPTION_THRESHOLD = 5;

    private UserRepository userRepository;
    private TransactionDao transactionDao;
    private CategoryConfiguration categoryConfig;
    private CategoryRepository categoryRepository;

    public ExtractDescriptionsToCategor() {
        super("extract-descriptions-to-categor",
                "Extract common transaction categories to categor.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        this.userRepository = serviceContext.getRepository(UserRepository.class);
        this.transactionDao = serviceContext.getDao(TransactionDao.class);
        this.categoryConfig = serviceContext.getCategoryConfiguration();
        this.categoryRepository = serviceContext.getRepository(CategoryRepository.class);

        final String marketKey = getRequiredSystemProperty("marketKey");
        final String apiToken = getRequiredSystemProperty("apiToken");
        final String url = getRequiredSystemProperty("url");
        final String categoryCodePrefix = System.getProperty("categoryCodePrefix", "");
        final int concurrency = Integer.getInteger("concurrency", 1);
        final Optional<String> market = Optional.ofNullable(System.getProperty("market"));
        final String preformatterName = System
                .getProperty("preformatter", "se.tink.backend.common.config.IdentityStringConverterFactory");
        final StringConverterFactory preformatterFactory = (StringConverterFactory) Class.forName(preformatterName)
                .newInstance();
        Function<String, String> preformatter = preformatterFactory.build();
        final boolean dryRun = Boolean.getBoolean("dryRun");
        int submissionConcurrency = Integer.getInteger("submissionConcurrency", 40);

        final int descriptionThreshold = Integer.getInteger("descriptionThreshold", DEFAULT_DESCRIPTION_THRESHOLD);

        final int memoryLimit = Integer.getInteger("memoryLimit", DEFAULT_TOPK_SIZE);
        // Helper class that will automatically prune the uncommon transaction descriptions so we don't run out of
        // memory.
        final StreamSummary<String> topDescriptionsSummary = new StreamSummary<>(memoryLimit);

        // Using a fixed capacity to not run out of memory. Not using capacity of 1 to avoid some contention.
        final BlockingQueue<String> transactionDescriptionQueue = new LinkedBlockingQueue<>(1000);

        // StreamSummary isn't thread-safe so I'm using a single thread here to populate it.
        final Thread streamSummaryUpdated = new Thread(() -> {
            try {
                log.info("Started the topDescriptionsSummary construction thread.");
                while (true) {
                    try {
                        String description = transactionDescriptionQueue.take();
                        if (log.isDebugEnabled()) {
                            log.debug(String.format("Adding '%s'.", description));
                        }
                        topDescriptionsSummary.offer(description);
                    } catch (InterruptedException e) {
                        break;
                    }
                }

                // Drain the queue.

                while (true) {
                    String description = transactionDescriptionQueue.poll();
                    if (description == null) {
                        return;
                    }
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Adding '%s'.", description));
                    }
                    topDescriptionsSummary.offer(description);
                }
            } catch (Exception e) {
                log.error("Could not populate topDescriptionsSummary.", e);
            }
        });
        streamSummaryUpdated.start();

        // Push all relevant transactions onto the queue.

        ConnectableObservable<List<String>> descriptionsPerUser = userRepository.streamAll()
                .compose(new CommandLineInterfaceUserTraverser(concurrency))
                .filter(u -> !Strings.isNullOrEmpty(u.getId()))
                .filter(u -> u.getProfile() != null)
                .filter(u -> !market.isPresent() || market.get().equals(u.getProfile().getMarket()))
                .map(user -> {
                    try {
                        return transactionDao.findAllByUserId(user.getId()).stream()
                                .filter(t -> !Strings.isNullOrEmpty(t.getOriginalDescription()))
                                .filter(t -> !Strings.isNullOrEmpty(t.getCategoryId()))
                                .filter(t -> categoryRepository.findById(t.getCategoryId()) != null)
                                .filter(t -> categoryRepository.findById(t.getCategoryId()).getCode()
                                        .startsWith(categoryCodePrefix))
                                .map(Transaction::getDescription)
                                .filter(desc -> desc != null)
                                .map(String::trim)
                                .map(desc -> preformatter.apply(desc))
                                .collect(Collectors.toList());
                    } catch (Exception e) {
                        log.error("Failed fetching transactions.", e);
                        return ImmutableList.<String>of();
                    }
                })
                .publish();

        descriptionsPerUser
                .flatMapIterable(t -> t)
                .forEach(description -> {
                    try {
                        if (log.isDebugEnabled()) {
                            log.debug(String.format("Pushing '%s' to queue.", description));
                        }
                        Uninterruptibles.putUninterruptibly(transactionDescriptionQueue, description);
                    } catch (Exception e) {
                        log.error("Could not push to queue.", e);
                    }
                });

        // If there was a way to subscribe to elements being purged from `uniqueUsersPerDescription`, the I wouldn't
        // need this to be a weak map. Using a weak map to automatically remove the desciptions that are no longer
        // relevant to avoid running out of memory.
        final Map<String, LongAdder> uniqueUsersPerDescription = Collections.synchronizedMap(new WeakHashMap<>());

        descriptionsPerUser
                .map(ImmutableSet::copyOf)
                .flatMapIterable(uniqueDescriptionsPerUser -> uniqueDescriptionsPerUser)
                .forEach(
                        description -> {
                            try {
                                uniqueUsersPerDescription.computeIfAbsent(description, _desc2 -> new LongAdder())
                                        .increment();
                            } catch (Exception e) {
                                log.error("Could not count uniqueness.", e);
                            }
                        });

        // Go through all the transactions:

        log.debug("Starting going through all users.");
        descriptionsPerUser.connect();

        Preconditions.checkState(streamSummaryUpdated.isAlive(), "Thread must have died.");
        streamSummaryUpdated.interrupt();
        streamSummaryUpdated.join();

        // We are now done building the stream summary and count of unique users per description. Submitting them to
        // Categor.

        Client client = Client.create();
        WebResource jerseyResource = client.resource(url);
        TrainingSetService categorService = WebResourceFactory.newResource(TrainingSetService.class, jerseyResource);

        List<Counter<String>> topDescriptions = topDescriptionsSummary.topK(memoryLimit);
        log.info(String.format("Found %d popular transaction descriptions.", topDescriptions.size()));
        List<Counter<String>> mostCommonDescriptions = topDescriptions.stream()
                .filter(c -> {
                    try {
                        final String description = c.getItem();

                        if (!uniqueUsersPerDescription.containsKey(description)) {
                            log.warn(String.format("Key '%s' could not be found.", description));
                            return false;
                        }

                        final boolean shouldSubmit =
                                uniqueUsersPerDescription.get(description).sum() >= descriptionThreshold;

                        if (log.isDebugEnabled()) {
                            if (shouldSubmit) {
                                log.debug(String.format(
                                        "Description '%s' is above or equal (%d) threshold (%d). Will be submitted.",
                                        description, uniqueUsersPerDescription.get(description).sum(),
                                        descriptionThreshold));
                            } else {
                                log.debug(String.format(
                                        "Description '%s' is below (%d) to threshold (%d). Not submitting.",
                                        description, uniqueUsersPerDescription.get(description).sum(),
                                        descriptionThreshold));
                            }
                        }

                        return shouldSubmit;
                    } catch (Exception e) {
                        log.error("Something went wrong with the filtering.", e);
                        return false;
                    }
                })
                .collect(Collectors.toList());
        log.info(String.format("Found %d transaction descriptions with relevant user threshold to submit.",
                mostCommonDescriptions.size()));

        Observable<Counter<String>> mostCommonDescriptionsStream = Observable.from(mostCommonDescriptions);

        if (submissionConcurrency > 1) {
            mostCommonDescriptionsStream = mostCommonDescriptionsStream.compose(
                    ThreadPoolObserverTransformer.buildFromSystemPropertiesWithConcurrency(submissionConcurrency)
                            .build());
        }

        mostCommonDescriptionsStream
                .forEach(c -> submitDescription(dryRun, apiToken, categorService, marketKey, c.getItem(), c.getCount()));

    }

    private String getRequiredSystemProperty(String property) {
        return Preconditions
                .checkNotNull(System.getProperty(property), String.format("'%s' must be specified", property));
    }

    private static class DescriptionPayload {
        public String marketkey;
        public String description;
        public long popularity;
    }

    @Path("/api/training-sets")
    @Consumes({
            MediaType.APPLICATION_JSON
    })
    @Produces({
            MediaType.APPLICATION_JSON
    })
    private interface TrainingSetService {
        @POST
        @Path("/{trainingsetkey}/training-samples")
        @Consumes({
                MediaType.APPLICATION_JSON
        })
        @Produces({
                MediaType.APPLICATION_JSON
        })
        void submitDescription(
                @HeaderParam("Authorization") String authorizationHeader,
                @PathParam("trainingsetkey") String trainingsetkey,
                DescriptionPayload payload
        );
    }

    private void submitDescription(boolean dryRun, String apiToken, TrainingSetService categorService, String marketKey,
            String description,
            long count) {
        try {
            DescriptionPayload payload = new DescriptionPayload();
            payload.description = description;
            payload.marketkey = marketKey;

            // XXX: Should this be proportional to unique users having the transactions instead? Likely proportional to
            // the total number of times the transaction showed up in total, though...
            payload.popularity = count;

            if (dryRun) {
                if (log.isDebugEnabled()) {
                    log.debug(
                            String.format(
                                    "Would have submitted description '%s' with popularity %d, but 'dryRun' is enabled.",
                                    description, count));
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Submitting description '%s' with popularity %d.", description, count));
                }
                categorService.submitDescription("Bearer " + apiToken, marketKey, payload);
            }
        } catch (Exception e) {
            log.error("Could not submit.", e);
        }
    }

}
