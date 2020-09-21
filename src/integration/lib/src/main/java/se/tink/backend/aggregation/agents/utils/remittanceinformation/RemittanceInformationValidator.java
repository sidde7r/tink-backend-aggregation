package se.tink.backend.aggregation.agents.utils.remittanceinformation;

import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class RemittanceInformationValidator {
    private static final TransferExecutionException exception =
            TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setMessage("Unsupported RemittanceInformation")
                    .setEndUserMessage(EndUserMessage.INVALID_DESTINATION_MESSAGE)
                    .setInternalStatus(InternalStatus.INVALID_DESTINATION_MESSAGE_TYPE.toString())
                    .build();

    private RemittanceInformationValidator() {}

    public static void validateSupportedRemittanceInformationTypesOrThrow(
            RemittanceInformation remittanceInformation,
            RemittanceInformationType... supportedTypes) {
        for (RemittanceInformationType type : supportedTypes) {
            if (type == remittanceInformation.getType()) {
                return;
            }
        }
        throw exception;
    }
}
