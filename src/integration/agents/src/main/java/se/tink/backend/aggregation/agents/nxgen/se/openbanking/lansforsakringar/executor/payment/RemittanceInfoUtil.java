package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.exceptions.payment.ReferenceValidationException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.PaymentValue;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class RemittanceInfoUtil {

    public static void validateRemittanceInfoForGiros(RemittanceInformation remittanceInformation)
            throws ReferenceValidationException {

        if (remittanceInformation == null || remittanceInformation.getType() == null) {
            throw new ReferenceValidationException(EndUserMessage.INVALID_MESSAGE.getKey().get());
        }

        switch (remittanceInformation.getType()) {
            case UNSTRUCTURED:
                validateMessageLength(
                        remittanceInformation.getValue(),
                        PaymentValue.MAX_DEST_MSG_LEN_GIROS_UNSTRUCTURED,
                        EndUserMessage.INVALID_MESSAGE.getKey().get());
                break;
            case OCR:
                validateMessageLength(
                        remittanceInformation.getValue(),
                        PaymentValue.MAX_DEST_MSG_LEN_GIROS_STRUCTURED,
                        EndUserMessage.INVALID_OCR.getKey().get());
                break;
            default:
                throw new ReferenceValidationException(
                        EndUserMessage.INVALID_MESSAGE.getKey().get());
        }
    }

    public static String validateAndReturnRemittanceInfo(
            RemittanceInformation remittanceInformation) throws ReferenceValidationException {

        if (remittanceInformation != null) {
            if (remittanceInformation.getType() == RemittanceInformationType.UNSTRUCTURED) {
                validateMessageLength(
                        remittanceInformation.getValue(),
                        PaymentValue.MAX_DEST_MSG_LEN_UNSTRUCTURED,
                        EndUserMessage.INVALID_MESSAGE.getKey().get());
                return remittanceInformation.getValue();
            } else {
                throw new ReferenceValidationException(
                        EndUserMessage.INVALID_MESSAGE.getKey().get());
            }
        }

        return null;
    }

    private static void validateMessageLength(String message, int length, String errorMessage)
            throws ReferenceValidationException {
        if (!Strings.isNullOrEmpty(message) && message.length() > length) {
            throw new ReferenceValidationException(errorMessage);
        }
    }
}
