package se.tink.backend.system.cli.abnamro;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Market;
import se.tink.backend.core.Provider;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.core.User;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;
import se.tink.backend.system.workers.processor.formatting.MarketDescriptionExtractorFactory;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ReprocessTransactionDescriptionsCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final TypeReference<List<String>> STRING_LIST_TYPE_REFERENCE = new TypeReference<List<String>>() {
    };

    private static final LogUtils log = new LogUtils(ReprocessTransactionDescriptionsCommand.class);

    private TransactionDao transactionDao;
    private UserRepository userRepository;
    private CredentialsRepository credentialsRepository;
    private ProviderRepository providerRepository;
    private MarketDescriptionExtractorFactory extractor;
    private AggregationControllerCommonClient aggregationControllerClient;
    private boolean isProvidersOnAggregation;

    public ReprocessTransactionDescriptionsCommand() {
        super("reprocess-transaction-descriptions", "Reprocess transaction descriptions for ABN AMRO users.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, final ServiceContext serviceContext)
            throws Exception {

        log.info("Reprocess transaction descriptions for ABN AMRO users.");

        if (!Objects.equal(configuration.getCluster(), Cluster.ABNAMRO)) {
            log.error("This command is only enabled in the ABN AMRO cluster.");
            return;
        }

        final boolean dryRun = Boolean.getBoolean("dryRun");
        if (dryRun) {
            log.info("NB! This is just a dry run. No changes will be persisted.");
        }

        transactionDao = serviceContext.getDao(TransactionDao.class);
        credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        providerRepository = serviceContext.getRepository(ProviderRepository.class);
        aggregationControllerClient = serviceContext.getAggregationControllerCommonClient();
        userRepository = serviceContext.getRepository(UserRepository.class);
        isProvidersOnAggregation = serviceContext.isProvidersOnAggregation();

        extractor = MarketDescriptionExtractorFactory.byCluster(configuration.getCluster());

        final AtomicInteger changedCount = new AtomicInteger();

        userRepository.streamAll()
                .compose(new CommandLineInterfaceUserTraverser(10))
                .forEach(user -> {
                    try {
                        process(user, dryRun, changedCount);
                    } catch (Exception e) {
                        log.error(user.getId(), "Processing failed.", e);
                    }
                });

        log.info(String.format("Done! Changed %d descriptions.", changedCount.get()));
        if (dryRun) {
            log.info("NB! This was just a dry run. No changes were persisted.");
        }
    }

    private void process(User user, boolean dryRun, AtomicInteger changedCount) {
        List<Credentials> credentials = credentialsRepository.findAllByUserId(user.getId());
        List<Transaction> transactions = transactionDao.findAllByUserId(user.getId());

        Map<String, Provider> providerByCredentialsId = credentials.stream()
                .collect(Collectors.toMap(Credentials::getId, c -> findProviderByName(c.getProviderName())));

        process(user, transactions, dryRun, changedCount, providerByCredentialsId);
    }

    private Provider findProviderByName(String name) {
        if (isProvidersOnAggregation) {
            return aggregationControllerClient.getProviderByName(name);
        } else {
            return providerRepository.findByName(name);
        }
    }

    private void process(User user, List<Transaction> transactions, boolean dryRun, AtomicInteger changedCount,
            Map<String, Provider> providerByCredentialsId) {

        List<Transaction> transactionsToUpdate = Lists.newArrayList();

        for (Transaction transaction : transactions) {
            if (process(transaction, providerByCredentialsId.get(transaction.getCredentialsId()))) {
                changedCount.incrementAndGet();
                transactionsToUpdate.add(transaction);
            }
        }

        if (!dryRun && !transactionsToUpdate.isEmpty()) {
            transactionDao.saveAndIndex(user, transactionsToUpdate, false);
        }
    }

    private boolean process(Transaction transaction, Provider provider) {
        if (!transaction.hasInternalPayload()) {
            return false;
        }

        Map<String, String> internalPayload = transaction.getInternalPayload();

        // Remove the faulty external transaction ids.
        // Not really (well...at all) a description re-processing thing, but we're already at it... 
        internalPayload.remove("ID");

        String serializedDescriptionLines = internalPayload.get(AbnAmroUtils.InternalPayloadKeys.DESCRIPTION_LINES);

        if (Strings.isNullOrEmpty(serializedDescriptionLines)) {
            return false;
        }

        List<String> descriptionLines = SerializationUtils.deserializeFromString(
                serializedDescriptionLines,
                STRING_LIST_TYPE_REFERENCE
        );

        if (descriptionLines.isEmpty()) {
            return false;
        }

        Map<String, String> descriptionParts = AbnAmroUtils.getDescriptionParts(descriptionLines);

        // Set the transaction message.
        final String message = StringUtils.trimToNull(descriptionParts.get(AbnAmroUtils.DescriptionKeys.DESCRIPTION));
        if (message != null && !Objects.equal(message, transaction.getDescription())) {
            transaction.setPayload(TransactionPayloadTypes.MESSAGE, message);
        }

        // Regenerate description.
        String description = AbnAmroUtils.getDescription(descriptionParts);

        // Cleanup encoding F-UPs
        if (description.contains("Â¤")) {
            log.warn(transaction.getUserId(), transaction.getCredentialsId(),
                    String.format("The transaction [id=%s] contains \"Â¤\". Cleaning up.", transaction.getId())
            );
            description = description.replace("Â¤", "¤");
        }

        final String originalDescription = transaction.getOriginalDescription();

        // The description didn't change. Bail.
        if (Objects.equal(originalDescription, description)) {
            return false; // No changes made.
        }

        transaction.setOriginalDescription(description);

        // The formatted description have to be reset before the clean description is retrieved.
        transaction.setFormattedDescription(null);

        String cleanDescription = extractor.get(Market.Code.valueOf(provider.getMarket().toUpperCase()))
                .getCleanDescription(transaction);

        // Default to the new original description, if the cleaned one is empty.
        if (Strings.isNullOrEmpty(cleanDescription)) {
            cleanDescription = description;
        }

        final String formattedDescription = StringUtils.formatHuman(cleanDescription);

        transaction.setFormattedDescription(formattedDescription);

        if (!transaction.isUserModifiedDescription()) {
            transaction.setDescription(formattedDescription);
        }

        log.info(transaction.getUserId(), transaction.getCredentialsId(),
                String.format("Description changed from \"%s\" to \"%s\".", originalDescription, description)
        );

        return true; // Changes made.
    }
}
