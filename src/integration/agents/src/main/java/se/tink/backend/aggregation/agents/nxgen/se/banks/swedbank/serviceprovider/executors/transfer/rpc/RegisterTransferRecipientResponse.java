package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.transfer.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class RegisterTransferRecipientResponse {
    private String name;
    private String accountNumber;
    private String clearingNumber;
    private String fullyFormattedNumber;
    private LinksEntity links;
}
