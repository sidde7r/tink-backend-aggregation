package se.tink.backend.system.cli.analytics;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AtomicLongMap;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.libraries.abnamro.utils.AbnAmroPaymentProviderUtils;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;
import se.tink.libraries.abnamro.utils.paymentproviders.PaymentProvider;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.serialization.TypeReferences;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

/**
 * Command will go through users and check which transactions that we can extract payment provider information
 * from. Use flag `printMatched=true` if the output should be what is matched or `printMatched=false` if the output
 * should we what is unmatched.
 */
public class EvaluatePaymentProviderCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(EvaluatePaymentProviderCommand.class);

    private TransactionDao transactionDao;

    private ImmutableList<PaymentProvider> paymentProviders;

    public EvaluatePaymentProviderCommand() {
        super("evaluate-payment-provider", "Evaluate payment provider logic");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, final ServiceContext serviceContext)
            throws Exception {

        if (!com.google.common.base.Objects.equal(configuration.getCluster(), Cluster.ABNAMRO)) {
            log.error("This command is only enabled in the ABN AMRO cluster.");
            return;
        }

        transactionDao = serviceContext.getDao(TransactionDao.class);
        paymentProviders = AbnAmroPaymentProviderUtils.getPaymentProviders();

        UserRepository userRepository = serviceContext.getRepository(UserRepository.class);

        final AtomicLongMap<String> statistics = AtomicLongMap.create();

        String matchProperty = System.getProperty("printMatched");

        userRepository.streamAll()
                .compose(new CommandLineInterfaceUserTraverser(1))
                .forEach(user -> {
                    try {
                        processUser(user, statistics);
                    } catch (Exception e) {
                        log.error("Failed to execute for userId: " + user.getId(), e);
                    }
                });

        printStatistics(statistics);
    }

    private void processUser(User user, AtomicLongMap<String> statistics) {
        List<Transaction> transactions = transactionDao.findAllByUserId(user.getId());

        for (Transaction t : transactions) {
            processTransaction(t, statistics);
        }
    }

    private void processTransaction(Transaction transaction, AtomicLongMap<String> statistics) {

        Map<String, String> descriptionParts = getDescriptionParts(transaction);

        String descriptionOld = getDescriptionWithoutProvider(descriptionParts);
        String descriptionNew = getDescriptionWithProvider(descriptionParts);

        // Check which payment provider this description is matched by (if any)
        Optional<PaymentProvider> provider = getPaymentProviderByName(descriptionParts);

        if (!provider.isPresent()) {
            statistics.getAndIncrement("No-provider-match");
            return;
        }

        String providerName = provider.get().getClass().getSimpleName();
        String descriptionFull = descriptionParts.get(AbnAmroUtils.DescriptionKeys.DESCRIPTION);
        String format = "Provider: %-30s Old: %-40s New: %-60s Full: %-200s";

        boolean isChanged = !Objects.equals(descriptionOld, descriptionNew);

        if (isChanged) {
            statistics.getAndIncrement(providerName);
            log.info(String.format(format, providerName, descriptionOld, descriptionNew, descriptionFull));
        } else {
            statistics.getAndIncrement(providerName + "-no-description-match");
            log.info(String.format(format, providerName, descriptionOld, descriptionNew, descriptionFull));
        }
    }

    private Map<String, String> getDescriptionParts(Transaction transaction) {
        String payload = transaction.getInternalPayload((AbnAmroUtils.InternalPayloadKeys.DESCRIPTION_LINES));

        List<String> lines = SerializationUtils.deserializeFromString(payload, TypeReferences.LIST_OF_STRINGS);

        return AbnAmroUtils.getDescriptionParts(lines);
    }

    private String getDescriptionWithoutProvider(Map<String, String> descriptionParts) {
        return StringUtils.formatHuman(AbnAmroUtils.getDescription(descriptionParts));
    }

    private String getDescriptionWithProvider(Map<String, String> descriptionParts) {
        return StringUtils.formatHuman(AbnAmroUtils.getDescription(descriptionParts, true));
    }

    /**
     * Returns the first provider that matches the name (if any)
     */
    private Optional<PaymentProvider> getPaymentProviderByName(Map<String, String> descriptionParts) {
        final String providerName = descriptionParts.get(AbnAmroUtils.DescriptionKeys.NAME);

        return paymentProviders.stream().filter(p -> p.matches(providerName)).findFirst();
    }

    private void printStatistics(AtomicLongMap<String> statistics) {

        for (Map.Entry<String, Long> entry : statistics.asMap().entrySet()) {
            log.info(String.format("%-20s %d", entry.getKey(), entry.getValue()));
        }
    }
}

