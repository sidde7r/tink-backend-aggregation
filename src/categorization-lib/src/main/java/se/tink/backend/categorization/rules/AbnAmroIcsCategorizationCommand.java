package se.tink.backend.categorization.rules;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import java.io.IOException;
import java.util.Optional;
import se.tink.backend.abnamro.utils.AbnAmroIcsCredentials;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;
import se.tink.backend.categorization.CategorizationVector;
import se.tink.backend.categorization.MerchantCategoryMatcher;
import se.tink.backend.categorization.interfaces.Classifier;
import se.tink.backend.core.CategorizationCommand;
import se.tink.backend.core.Provider;
import se.tink.backend.core.Transaction;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.utils.LogUtils;

/**
 * Command that adds a categorization vector for transactions based on the MCC (Merchant Category Code) Descriptions.
 * <p>
 * The command is only used by ABN AMRO ICS Credentials since these are the only transactions that have this this
 * information.
 */
public class AbnAmroIcsCategorizationCommand implements Classifier {

    private final static LogUtils log = new LogUtils(AbnAmroIcsCategorizationCommand.class);

    private static MerchantCategoryMatcher MERCHANT_CATEGORY_MATCHER;

    static {
        try {
            MERCHANT_CATEGORY_MATCHER = MerchantCategoryMatcher.builder(Cluster.ABNAMRO).build();
        } catch (IOException e) {
            log.error("Could not initialize the merchant category matcher");
        }
    }

    @VisibleForTesting
    /*package*/ AbnAmroIcsCategorizationCommand() {
    }

    public static Optional<AbnAmroIcsCategorizationCommand> build(Provider provider) {
        if (!AbnAmroIcsCredentials.isAbnAmroIcsProvider(provider)) {
            return Optional.empty();
        }
        return Optional.of(new AbnAmroIcsCategorizationCommand());
    }

    public static void warmUp() {
        // Will initialize the static MERCHANT_CATEGORY_MATCHER;
    }

    @Override
    public Optional<Outcome> categorize(Transaction transaction) {

        if (transaction.getAmount() > 0) {
            // It's an income.
            return Optional.empty();
        }

        return getMerchantCategoryVector(transaction)
                .map(v -> new Outcome(CategorizationCommand.MCC, v));
    }

    /**
     * ABN AMRO Credit Cards (ICS) provide a merchant category description. Try to assign a category based on this
     * description. Package private so it's possible to unit tests without creating a whole TransactionProcessorContext.
     */
    public static Optional<CategorizationVector> getMerchantCategoryVector(Transaction transaction) {

        if (MERCHANT_CATEGORY_MATCHER == null) {
            log.error("Merchant category matcher is null");
            return Optional.empty();
        }

        String description = transaction.getInternalPayload(AbnAmroUtils.InternalPayloadKeys.MERCHANT_DESCRIPTION);

        if (Strings.isNullOrEmpty(description)) {
            return Optional.empty();
        }

        return Optional.ofNullable(MERCHANT_CATEGORY_MATCHER.findByDescription(description));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .toString();
    }
}
