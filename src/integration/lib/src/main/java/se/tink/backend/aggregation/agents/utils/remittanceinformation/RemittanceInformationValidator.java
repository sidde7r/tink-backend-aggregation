package se.tink.backend.aggregation.agents.utils.remittanceinformation;

import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class RemittanceInformationValidator {
    private static final TransferExecutionException exception =
            TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setMessage("Unsupported RemittanceInformation")
                    .build();

    private RemittanceInformationValidator() {}

    public static void validateSupportedRemittanceInformationTypesOrThrow(
            RemittanceInformation remittanceInformation,
            RemittanceInformationType... supportedTypes) {
        Stream.of(supportedTypes)
                .filter(
                        remittanceInformationType ->
                                remittanceInformationType == remittanceInformation.getType())
                .findFirst()
                .orElseThrow(() -> exception);
    }
}
