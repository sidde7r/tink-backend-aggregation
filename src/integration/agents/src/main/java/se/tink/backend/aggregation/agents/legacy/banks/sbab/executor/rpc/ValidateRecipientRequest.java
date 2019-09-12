package se.tink.backend.aggregation.agents.banks.sbab.executor.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.banks.sbab.util.SBABDestinationAccountIdentifierFormatter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonObject
public class ValidateRecipientRequest {
    @JsonIgnore
    private static final SBABDestinationAccountIdentifierFormatter ACCOUNT_IDENTIFIER_FORMATTER =
            new SBABDestinationAccountIdentifierFormatter();

    private String accountNumber;
    private String description;

    @JsonIgnore
    private ValidateRecipientRequest(Transfer transfer) {
        this.accountNumber = transfer.getDestination().getIdentifier(ACCOUNT_IDENTIFIER_FORMATTER);
        this.description = "";
    }

    @JsonIgnore
    public static ValidateRecipientRequest create(Transfer transfer) {
        return new ValidateRecipientRequest(transfer);
    }
}
