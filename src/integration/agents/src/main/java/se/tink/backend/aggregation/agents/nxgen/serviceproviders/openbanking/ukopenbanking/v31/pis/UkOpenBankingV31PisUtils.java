package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentExceptionImpl;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Constants.EndUserMessage;
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

    static String convertToEndUserMessage(String errorMessageFromBank) {

        if (Strings.isNullOrEmpty(errorMessageFromBank)) {
            return EndUserMessage.PAYMENT_NOT_AUTHORISED_BY_USER;
        }

        if (StringUtils.containsIgnoreCase(errorMessageFromBank, "cancelled")) {
            return EndUserMessage.PIS_AUTHORISATION_CANCELLED;
        }

        if (StringUtils.containsIgnoreCase(
                errorMessageFromBank, "not completed in the allotted time")) {
            return EndUserMessage.PIS_AUTHORISATION_TIMEOUT;
        }

        if (StringUtils.containsIgnoreCase(errorMessageFromBank, "User failed to authenticate")) {
            return EndUserMessage.PIS_AUTHORISATION_FAILED_USER_ERROR;
        }

        // Log unknown error message and return the generic end user message for when payment
        // wasn't authorised.
        log.warn(
                "Unknown error message from bank during payment authorisation: {}",
                errorMessageFromBank);
        return EndUserMessage.PAYMENT_NOT_AUTHORISED_BY_USER;
    }

    static TransferExecutionException createCancelledTransferException(
            AgentExceptionImpl e, String endUserMessage) {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage("Payment cancelled - user did not authorise payment.")
                .setEndUserMessage(endUserMessage)
                .setException(e)
                .build();
    }

    static TransferExecutionException createFailedTransferException(AgentExceptionImpl e) {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(
                        "Payment failed - authorisation of payment failed, needs further investigation.")
                .setEndUserMessage("Authorisation of payment failed.")
                .setException(e)
                .build();
    }
}
