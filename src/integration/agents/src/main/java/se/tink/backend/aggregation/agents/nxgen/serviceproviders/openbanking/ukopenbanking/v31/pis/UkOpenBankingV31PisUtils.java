package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.libraries.payment.rpc.Reference;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;

public class UkOpenBankingV31PisUtils {
    private static Logger log = LoggerFactory.getLogger(UkOpenBankingV31PisUtils.class);

    /**
     * Reference type should be type of reference (OCR, free text etc.) but is instead the transfer
     * type according to how the payment controller was designed. The type will always be
     * BANK_TRANSFER for UKOB. It's hardcoded here to construct the Reference correctly according to
     * the initial PaymentRequest that is sent to the agent.
     */
    public static Reference createTinkReference(String reference) {
        return new Reference("BANK_TRANSFER", reference);
    }

    static TransferExecutionException createCancelledTransferException(String endUserMessage) {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(endUserMessage)
                .setEndUserMessage(endUserMessage)
                .build();
    }

    static TransferExecutionException createFailedTransferException() {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(
                        "Payment failed - authorisation of payment failed, needs further investigation.")
                .setEndUserMessage("Authorisation of payment failed.")
                .build();
    }
}
