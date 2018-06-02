package se.tink.backend.system.cli.cleanup;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Market;
import se.tink.backend.core.Provider;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionTypes;
import se.tink.backend.core.User;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;
import se.tink.backend.system.workers.processor.formatting.DescriptionExtractor;
import se.tink.backend.system.workers.processor.formatting.DescriptionFormatter;
import se.tink.backend.system.workers.processor.formatting.MarketDescriptionExtractorFactory;
import se.tink.backend.system.workers.processor.formatting.MarketDescriptionFormatterFactory;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.i18n.Catalog;

public class FixSettledTransactionData extends ServiceContextCommand<ServiceConfiguration> {
    private static final Logger log = LoggerFactory.getLogger(FixSettledTransactionData.class);
    private static ImmutableMap<String, Pattern> pendingDescriptionPatterns = ImmutableMap.<String, Pattern>builder()
            .put("swedbank och sparbankerna", Pattern.compile("^skyddat belopp$|^övf via internet$",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE))
            .put("handelsbanken", Pattern.compile("^prel(\\.(\\s.+)?)?$", Pattern.CASE_INSENSITIVE))
            .put("länsförsäkringar bank", Pattern.compile("^prel(\\s.+)?$", Pattern.CASE_INSENSITIVE))
            .build();

    private TransactionDao transactionDao;
    private final static ImmutableSet<TransactionTypes> VALID_TRANSACTION_TYPES_TO_EXTRAPOLATE = ImmutableSet.of(
            TransactionTypes.CREDIT_CARD,
            TransactionTypes.DEFAULT,
            TransactionTypes.PAYMENT
    );

    private static final double MAX = 1;
    private static final double MIN = 0;

    public FixSettledTransactionData() {
        super("fix-settled-transactions",
                "Restore transaction for affected user");
    }

    private boolean isPredefinedPendingDescription(Provider provider, String description) {
        if (!Strings.isNullOrEmpty(description)) {
            String providerName = Optional.ofNullable(provider.getGroupDisplayName()).orElse(provider.getDisplayName());
            Pattern pendingDescriptionPattern = pendingDescriptionPatterns.getOrDefault(providerName.toLowerCase()
                    ,Pattern.compile("^skyddat belopp$|^övf via internet$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
            return pendingDescriptionPattern != null && pendingDescriptionPattern.matcher(description).matches();
        } else {
            return false;
        }
    }

    private boolean isSignDifferent(double existingAmount, double incomingAmount) {
        if (existingAmount == 0d || incomingAmount == 0d) {
            log.warn(String.format("Transaction amount is zero {\"existingAmount\": %s, \"incomingAmount\": %s}",
                    existingAmount, incomingAmount));
            return false;
        }

        return existingAmount > 0 ^ incomingAmount > 0;
    }

    private double compareDescription(String description, String originalDescription) {
        // Null description should be interpreted as empty description
        return StringUtils.getJaroWinklerDistance(
                Strings.nullToEmpty(description),
                Strings.nullToEmpty(originalDescription));
    }

    private double compareAmount(double amount, double originalAmount) {
        double maxDiffQuotient = 0.40;

        double absAmount = Math.abs(amount);
        double absoriginalAmount = Math.abs(originalAmount);

        if (isSignDifferent(amount, originalAmount)) {
            return MIN;
        }

        double diff = Math.abs(absAmount - absoriginalAmount);

        if (diff == 0) {
            return MAX;
        }

        double diffQuotient = diff / amount;
        if (diffQuotient > maxDiffQuotient) {
            return MIN;
        }

        // Don't change. This is used to scale ( weight * diffQuotient ) to a number between 0 and 0.5.
        double weight = 0.5 / maxDiffQuotient;

        return Math.cos(weight * diffQuotient * Math.PI);
    }

    private double compareDate(Date date, Date originalDate) {
        int maxDaysBetween = 20;

        int daysBetween = DateUtils.daysBetween(
                date,
                originalDate);

        if (daysBetween == 0) {
            return MAX;
        }

        if (daysBetween > maxDaysBetween || daysBetween < 0) {
            return MIN;
        }

        // Don't change. This is used to scale ( weight * daysBetween ) to a number between 0 and 0.5.
        double weight = 0.5 / maxDaysBetween;

        return Math.cos(weight * daysBetween * Math.PI);
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
                       ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        boolean dryRun = Boolean.parseBoolean(Optional.ofNullable(System.getProperty("dryRun")).orElse("true"));
        double dateThreshold = Double.parseDouble(Optional.ofNullable(System.getProperty("dateThreshold")).orElse("0.0"));
        double descriptionThreshold = Double.parseDouble(Optional.ofNullable(System.getProperty("descriptionThreshold")).orElse("0.0"));
        double amountThreshold = Double.parseDouble(Optional.ofNullable(System.getProperty("amountThreshold")).orElse("0.0"));

        Preconditions.checkArgument(dateThreshold > 0, "dateThreshold must be set to a value > 0.0." );
        Preconditions.checkArgument(descriptionThreshold > 0, "descriptionThreshold must be set to a value > 0.0." );
        Preconditions.checkArgument(amountThreshold > 0, "amountThreshold must be set to a value > 0.0." );
        ProviderDao providerDao = serviceContext.getDao(ProviderDao.class);
        CredentialsRepository credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        transactionDao = serviceContext.getDao(TransactionDao.class);
        UserRepository userRepository = serviceContext.getRepository(UserRepository.class);
        Cluster cluster = serviceContext.getConfiguration().getCluster();
        userRepository.streamAll()
                .compose(new CommandLineInterfaceUserTraverser(20))
                .forEach(user -> fixTransactions(user, dryRun, dateThreshold, descriptionThreshold, amountThreshold, credentialsRepository, providerDao, cluster));
    }

    public void fixTransactions(User user, boolean dryRun, double dateThreshold, double descriptionThreshold, double amountThreshold
    , CredentialsRepository credentialsRepository, ProviderDao providerDao, Cluster cluster) {
        ImmutableMap<String, Provider> providersByName = providerDao.getProvidersByName();
        final List<Credentials> credentials = credentialsRepository.findAllByUserId(user.getId());
        final Map<String, String> providerNameById = credentials.stream()
                .collect(Collectors.toMap(c -> c.getId(), c -> c.getProviderName()));

        List<Transaction> transactions = transactionDao.findAllByUserId(user.getId());
        transactions.stream()
                .filter(transaction -> {
                    String providerName = providerNameById.get(transaction.getCredentialsId());
                    Optional<Provider> providerOption = Optional.ofNullable(providersByName.get(providerName));
                    if(providerOption.isPresent()) {
                        Provider provider = providerOption.get();
                        return !transaction.isPending() && !isPredefinedPendingDescription(provider, transaction.getOriginalDescription())
                                && (isPredefinedPendingDescription(provider, transaction.getDescription()) ||
                                isPredefinedPendingDescription(provider, transaction.getFormattedDescription()));
                    } else {
                        log.error(String.format("provider with name %s couldn't be found", providerName));
                        return false;
                    }
                })
                .filter((transaction -> transaction.getOriginalDate().after(DateTime.parse("2017-12-06").toDate())))
                .forEach(transaction -> {
                    boolean changed = false;
                    String oldValues = String.format("date = %s, description= %s, amount=%s", transaction.getDate(), transaction.getDescription(), transaction.getAmount());
                    double amountScore = compareAmount(transaction.getAmount(), transaction.getOriginalAmount());
                    double dateScore = compareDate(transaction.getDate(), transaction.getOriginalDate());
                    double descriptionScore = compareDescription(transaction.getFormattedDescription(), transaction.getOriginalDescription());

                    if (dateScore < dateThreshold) {
                        transaction.setDate(transaction.getOriginalDate());
                        changed = true;
                    }

                    if (descriptionScore < descriptionThreshold) {
                        transaction.setDescription(transaction.getOriginalDescription());
                        String providerName = providerNameById.get(transaction.getCredentialsId());
                        Optional<Provider> providerOption = Optional.ofNullable(providersByName.get(providerName));

                        if(providerOption.isPresent()) {
                            setformattedDescription(user, transaction, providerOption.get(),
                                    MarketDescriptionFormatterFactory.byCluster(cluster), MarketDescriptionExtractorFactory.byCluster(cluster));
                        }

                        changed = true;
                    }

                    if (amountScore < amountThreshold) {
                        transaction.setAmount(transaction.getOriginalAmount());
                        changed = true;
                    }
                    
                    if (changed) {
                        String newValues = String.format("date = %s, description= %s, amount=%s", transaction.getDate(), transaction.getDescription(), transaction.getAmount());
                        log.info(String.format("user %s have settled transaction edited:\nfrom %s\nto %s", transaction.getUserId(), oldValues, newValues));

                        if(!dryRun) {
                            transactionDao.saveAndIndex(user, transactions, true);
                            log.info(String.format("transaction with id %s has been saved.", transaction.getId()));
                        }
                    }
                });
    }

    private void setformattedDescription(User user, Transaction transaction, Provider provider, MarketDescriptionFormatterFactory descriptionFormatterFactory,
                                     MarketDescriptionExtractorFactory descriptionExtractorFactory) {
        Market.Code marketCode = null;

        try {
            marketCode = Market.Code.valueOf(provider.getMarket());
        } catch (IllegalArgumentException e) {
            log.error(user.getId(), String.format(
                    "Provider %s has an unrecognized market (%s). Not formatting transaction description.",
                    provider.getName(), provider.getMarket()
            ));

        }

        Optional<Market.Code> marketCodeOptional = Optional.ofNullable(marketCode);
        final DescriptionFormatter formatter = descriptionFormatterFactory.get(marketCodeOptional.orElse(Market.Code.SE));
        DescriptionExtractor extractor = descriptionExtractorFactory.get(marketCode);

        // Extrapolate description (if applicable).
        if (eligibleForExtrapolation(user, transaction)) {
            String formattedDescription = formatter.extrapolate(transaction.getOriginalDescription());
            transaction.setFormattedDescription(formattedDescription);
        }

        // Clean up description.
        transaction.setFormattedDescription(StringUtils.formatHuman(extractor.getCleanDescription(transaction)));

        // Add placeholder description if the description is empty.
        if (Strings.isNullOrEmpty(transaction.getFormattedDescription())) {
            Catalog catalog = Catalog.getCatalog(user.getProfile().getLocale());
            transaction.setFormattedDescription(catalog.getString("(missing description)"));
        }
    }

    private boolean eligibleForExtrapolation(User user, Transaction transaction) {
        if (user.getFlags().contains(FeatureFlags.NO_EXTRAPOLATION)) {
            return false;
        }

        // Don't extrapolate if a formatted description already exists.
        if (!Strings.isNullOrEmpty(transaction.getFormattedDescription())) {
            return false;
        }

        // All transaction types are not eligible for extrapolation.
        if (!VALID_TRANSACTION_TYPES_TO_EXTRAPOLATE.contains(transaction.getType())) {
            return false;
        }

        // Don't extrapolate incomes.
        if (transaction.getAmount() > 0) {
            return false;
        }

        return true;
    }
}
