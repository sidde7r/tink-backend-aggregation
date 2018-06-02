package se.tink.backend.system.workers.processor.other.payment;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorCommandResult;
import se.tink.backend.utils.StringUtils;

/**
 * Detects payments using specialized payment solutions (Paypal, iZettle, etc.),
 * tags the transaction type and re-formats the description of the transaction.
 * 
 * Resets:
 *  TransactionPayloadTypes.PAYMENT_GATEWAY
 *
 * Sets:
 *  description
 *  TransactionPayloadTypes.PAYMENT_GATEWAY
 */
public class PaymentDetectionCommand implements TransactionProcessorCommand {
    // NB Must keep insert ordering and match "paypal *" before "paypal"
    private static ImmutableMap<String, String> PAYMENT_MAPPINGS = ImmutableMap.<String, String>builder()
            .put("paypal *", "Paypal")
            .put("paypal ", "Paypal")
            .put("swish betalning ", "Swish")
            .put("swish inbetalning ", "Swish")
            .put("izettle ", "iZettle")
            .put("izettle*", "iZettle")
            .put("iz *", "iZettle")
            .put("sq *", "Square")
            .put("google *", "Square")
            .put("ady*", "ADY")
            .put("wpy*", "WePay")
            .build();

    private static ImmutableList<String> NON_PAYMENT_PREFIXES = ImmutableList.of("izettle ab");

    @Override
    public TransactionProcessorCommandResult execute(Transaction transaction) {

        if (transaction.isUserModifiedDescription()) {
            return TransactionProcessorCommandResult.CONTINUE;
        }

        // reset if exists
        transaction.removePayload(TransactionPayloadTypes.PAYMENT_GATEWAY);
        
        // find payments

        String description = pickDescription(transaction);

        for (String nonPaymentTransactionPrefix : NON_PAYMENT_PREFIXES) {
            if (description.toLowerCase().startsWith(nonPaymentTransactionPrefix)) {
                return TransactionProcessorCommandResult.CONTINUE;
            }
        }

        for (String prefix : PAYMENT_MAPPINGS.keySet()) {
            if (!description.toLowerCase().startsWith(prefix)) {
                continue;
            }

            if (StringUtils.trim(description).length() != StringUtils.trim(prefix).length()) {
                transaction.setDescription(StringUtils.formatHuman(description.substring(prefix.length())));
            } else {
                transaction.setDescription(PAYMENT_MAPPINGS.get(prefix));
            }

            transaction.setPayload(TransactionPayloadTypes.PAYMENT_GATEWAY, PAYMENT_MAPPINGS.get(prefix));

            break;
        }

        return TransactionProcessorCommandResult.CONTINUE;
    }

    /**
     * Return the description that the command should work with. Commands before this command can have changed the
     * description so work with description if available and ot not original description.
     */
    private static String pickDescription(Transaction transaction) {
        if (!com.google.common.base.Strings.isNullOrEmpty(transaction.getDescription())) {
            return transaction.getDescription();
        }

        return transaction.getOriginalDescription();
    }

    @Override
    public TransactionProcessorCommandResult initialize() {
        return TransactionProcessorCommandResult.CONTINUE;
    }

    /**
     * Called for every command in command chain's reverse order at after processing all transactions.
     */
    @Override
    public void postProcess() {
        // Deliberately left empty.
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .toString();
    }
}
