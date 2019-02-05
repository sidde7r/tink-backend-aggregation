package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.transfer.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@JsonObject
public class RegisterTransferRecipientRequest {
    private String name;
    private String type;
    private String recipientNumber;

    private RegisterTransferRecipientRequest(String name, String type, String recipientNumber) {
        this.name = name;
        this.type = type;
        this.recipientNumber = recipientNumber;
    }

    private static String getCleanRecipientNumber(SwedishIdentifier destination) {
        String cleanRecipientNumber = destination.getClearingNumber() + destination.getAccountNumber();
        return cleanRecipientNumber.replaceAll("[^0-9]", "");
    }

    public static RegisterTransferRecipientRequest create(SwedishIdentifier destination, String recipientName) {
        return new RegisterTransferRecipientRequest(recipientName,
                SwedbankBaseConstants.TransferRecipientType.BANKACCOUNT,
                getCleanRecipientNumber(destination));
    }

    public String getRecipientNumber() {
        return recipientNumber;
    }
}
