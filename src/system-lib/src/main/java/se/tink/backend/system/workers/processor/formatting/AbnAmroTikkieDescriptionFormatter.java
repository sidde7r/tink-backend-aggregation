package se.tink.backend.system.workers.processor.formatting;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import java.util.Optional;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;
import se.tink.libraries.abnamro.utils.tikkie.TikkieDetails;
import se.tink.libraries.abnamro.utils.tikkie.TikkieUtils;
import se.tink.backend.core.Provider;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorCommandResult;
import se.tink.backend.utils.LogUtils;

/**
 * "Tikkie" is ABN AMRO apps where you can request money from other people. We format descriptions for Tikkie
 * transactions in a nicer way buy extracting the name and the sender / receiver from message details.
 */
public class AbnAmroTikkieDescriptionFormatter implements TransactionProcessorCommand {

    private static final LogUtils log = new LogUtils(AbnAmroTikkieDescriptionFormatter.class);

    private AbnAmroTikkieDescriptionFormatter() {
    }

    @Override
    public TransactionProcessorCommandResult execute(Transaction transaction) {

        if (TikkieUtils.isTikkieTransaction(transaction) && !transaction.isUserModifiedDescription()) {
            updateDescriptions(transaction);
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

    private void updateDescriptions(Transaction transaction) {

        TikkieDetails details = TikkieUtils.parseTransactionDetails(transaction);

        if (details == null) {
            log.info(transaction.getUserId(), transaction.getCredentialsId(),
                    "Could not parse Tikkie details. Format change?");
            return;
        }

        transaction.setDescription(String.format("Tikkie - %s", details.getName()));
        transaction.setOriginalDescription(transaction.getDescription());

        if (Strings.isNullOrEmpty(details.getMessage())) {
            return;
        }

        transaction.setPayload(TransactionPayloadTypes.MESSAGE, details.getMessage());
    }

    public static Optional<AbnAmroTikkieDescriptionFormatter> build(Provider provider) {
        if (!AbnAmroUtils.isAbnAmroProvider(provider.getName())) {
            // Execute for ABN AMRO only.
            return Optional.empty();
        }
        return Optional.of(new AbnAmroTikkieDescriptionFormatter());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).toString();
    }
}
