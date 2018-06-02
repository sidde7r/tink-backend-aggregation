package se.tink.backend.system.workers.processor.formatting;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.backend.core.Market;
import se.tink.backend.core.Provider;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionTypes;
import se.tink.backend.core.User;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorCommandResult;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;

/**
 * Sets a human readable format. Tries to extrapolate the description from other similar transactions.
 * <p>
 * Sets: description formattedDescription
 */
public class FormatDescriptionCommand implements TransactionProcessorCommand {
    private static final MetricId MODIFIED_DESCRIPTION_METRIC_NAME = MetricId.newId("modified_description");
    private static final MetricId UNMODIFIED_DESCRIPTION_METRIC_NAME = MetricId.newId("unmodified_description");

    private static final LogUtils log = new LogUtils(FormatDescriptionCommand.class);

    private final MarketDescriptionFormatterFactory descriptionFormatterFactory;
    private final MarketDescriptionExtractorFactory descriptionExtractorFactory;
    private final TransactionProcessorContext context;

    private final Counter modifiedDescriptionMeter;
    private final Counter unmodifiedDescriptionMeter;

    private final static ImmutableSet<TransactionTypes> VALID_TRANSACTION_TYPES_TO_EXTRAPOLATE = ImmutableSet.of(
            TransactionTypes.CREDIT_CARD,
            TransactionTypes.DEFAULT,
            TransactionTypes.PAYMENT
    );
    private final Provider provider;

    public FormatDescriptionCommand(
            TransactionProcessorContext context,
            MarketDescriptionFormatterFactory descriptionFormatterFactory,
            MarketDescriptionExtractorFactory descriptionExtractorFactory,
            MetricRegistry metricRegistry,
            Provider provider
    ) {
        this.provider = provider;
        this.context = context;
        this.descriptionFormatterFactory = descriptionFormatterFactory;
        this.descriptionExtractorFactory = descriptionExtractorFactory;
        this.modifiedDescriptionMeter = metricRegistry.meter(MODIFIED_DESCRIPTION_METRIC_NAME);
        this.unmodifiedDescriptionMeter = metricRegistry.meter(UNMODIFIED_DESCRIPTION_METRIC_NAME);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("descriptionFormatterFactory", descriptionFormatterFactory)
                .toString();
    }

    @Override
    public TransactionProcessorCommandResult execute(Transaction transaction) {

        final Market.Code marketCode;
        try {
            marketCode = Market.Code.valueOf(provider.getMarket());
        } catch (IllegalArgumentException e) {
            log.error(context.getUser().getId(), String.format(
                    "Provider %s has an unrecognized market (%s). Not formatting transaction description.",
                    provider.getName(), provider.getMarket()
            ));
            return TransactionProcessorCommandResult.CONTINUE;
        }
        final DescriptionFormatter formatter = descriptionFormatterFactory.get(marketCode);
        DescriptionExtractor extractor = descriptionExtractorFactory.get(marketCode);

        // Extrapolate description (if applicable).
        if (eligibleForExtrapolation(context.getUser(), transaction)) {
            String formattedDescription = formatter.extrapolate(transaction.getOriginalDescription());
            transaction.setFormattedDescription(formattedDescription);

            if (formattedDescription == null || formattedDescription.equals(transaction.getOriginalDescription())) {
                unmodifiedDescriptionMeter.inc();
            } else {
                modifiedDescriptionMeter.inc();
            }
        }

        // Clean up description. 
        transaction.setFormattedDescription(StringUtils.formatHuman(extractor.getCleanDescription(transaction)));

        // Add placeholder description if the description is empty.
        if (Strings.isNullOrEmpty(transaction.getFormattedDescription())) {
            Catalog catalog = Catalog.getCatalog(context.getUser().getProfile().getLocale());
            transaction.setFormattedDescription(catalog.getString("(missing description)"));
        }

        // Set the description only if it hasn't been modified by the user.
        if (!transaction.isUserModifiedDescription()) {
            transaction.setDescription(transaction.getFormattedDescription());
        }

        return TransactionProcessorCommandResult.CONTINUE;
    }

    @Override
    public TransactionProcessorCommandResult initialize() {
        return TransactionProcessorCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() {
        // Deliberately left empty.
    }

    private static boolean eligibleForExtrapolation(User user, Transaction transaction) {

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
