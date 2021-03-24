package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonObject
public class ValidateRecipientRequest {

    private String toAccountNumber;
    private String toClearingNumber;

    private ValidateRecipientRequest() {}

    public static ValidateRecipientRequest create(Transfer transfer) {
        AccountIdentifier destinationIdentifier = transfer.getDestination();
        ValidateRecipientRequest request = new ValidateRecipientRequest();
        if (destinationIdentifier.is(AccountIdentifierType.SE)) {
            SwedishIdentifier swedishIdentifier = destinationIdentifier.to(SwedishIdentifier.class);
            request.toClearingNumber = swedishIdentifier.getClearingNumber();
            request.toAccountNumber = swedishIdentifier.getAccountNumber();
        } else if (destinationIdentifier.is(AccountIdentifierType.SE_SHB_INTERNAL)) {
            request.toAccountNumber =
                    destinationIdentifier.getIdentifier(new DefaultAccountIdentifierFormatter());
        }
        return request;
    }
}
