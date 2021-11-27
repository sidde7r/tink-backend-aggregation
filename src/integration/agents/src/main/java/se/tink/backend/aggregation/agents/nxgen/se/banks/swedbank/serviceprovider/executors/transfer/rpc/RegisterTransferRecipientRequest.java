package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.transfer.rpc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@JsonObject
@AllArgsConstructor
@Getter
public class RegisterTransferRecipientRequest {
    private String name;
    private String type;
    private String recipientNumber;

    private static String getCleanRecipientNumber(SwedishIdentifier destination) {
        String cleanRecipientNumber =
                destination.getClearingNumber() + destination.getAccountNumber();
        return cleanRecipientNumber.replaceAll("[^0-9]", "");
    }

    public static RegisterTransferRecipientRequest create(
            SwedishIdentifier destination, String recipientName) {
        return new RegisterTransferRecipientRequest(
                recipientName,
                SwedbankBaseConstants.TransferRecipientType.BANKACCOUNT,
                getCleanRecipientNumber(destination));
    }
}
