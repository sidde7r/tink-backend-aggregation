package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.exceptions.payment.ReferenceValidationException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.PaymentValue;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class RemittanceInfoUtil {

    public static void validateRemittanceInfoForGiros(RemittanceInformation remittanceInformation)
            throws ReferenceValidationException {

        if (remittanceInformation == null) {
            throw new ReferenceValidationException(ErrorMessages.REMITTANCE_INFO_NOT_SET_FOR_GIROS);
        }

        switch (remittanceInformation.getType()) {
            case UNSTRUCTURED:
                validateMessageLength(
                        remittanceInformation.getValue(),
                        PaymentValue.MAX_DEST_MSG_LEN_GIROS_UNSTRUCTURED,
                        ErrorMessages.INVALID_INFO_UNSTRUCTURED);
                break;
            case OCR:
                validateMessageLength(
                        remittanceInformation.getValue(),
                        PaymentValue.MAX_DEST_MSG_LEN_GIROS_STRUCTURED,
                        ErrorMessages.INVALID_INFO_STRUCTURED);
                break;
            default:
                throw new ReferenceValidationException(
                        ErrorMessages.REMITTANCE_INFO_NOT_SET_FOR_GIROS);
        }
    }

    public static String validateAndReturnRemittanceInfo(
            RemittanceInformation remittanceInformation) throws ReferenceValidationException {

        if (remittanceInformation != null) {
            if (remittanceInformation.getType() == RemittanceInformationType.UNSTRUCTURED) {
                validateMessageLength(
                        remittanceInformation.getValue(),
                        PaymentValue.MAX_DEST_MSG_LEN_UNSTRUCTURED,
                        ErrorMessages.INVALID_INFO_UNSTRUCTURED);
                return remittanceInformation.getValue();
            } else {
                throw new ReferenceValidationException(ErrorMessages.INVALID_INFO_UNSTRUCTURED);
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
