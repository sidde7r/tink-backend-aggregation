package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;

@JsonObject
public class ValidateRecipientRequest {

    private String toAccountNumber;
    private String toClearingNumber;

    private ValidateRecipientRequest() {
    }

    public static ValidateRecipientRequest create(Transfer transfer) {
        AccountIdentifier destinationIdentifier = transfer.getDestination();
        ValidateRecipientRequest request = new ValidateRecipientRequest();
        if (destinationIdentifier.is(AccountIdentifier.Type.SE)) {
            SwedishIdentifier swedishIdentifier = destinationIdentifier.to(SwedishIdentifier.class);
            request.toClearingNumber = swedishIdentifier.getClearingNumber();
            request.toAccountNumber = swedishIdentifier.getAccountNumber();
        } else if (destinationIdentifier.is(AccountIdentifier.Type.SE_SHB_INTERNAL)) {
            request.toAccountNumber = destinationIdentifier.getIdentifier(new DefaultAccountIdentifierFormatter());
        }
        return request;
    }
}
