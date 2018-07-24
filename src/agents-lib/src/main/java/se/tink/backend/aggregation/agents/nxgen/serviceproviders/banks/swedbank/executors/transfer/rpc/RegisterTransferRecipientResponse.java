package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.transfer.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterTransferRecipientResponse {
    private String name;
    private String accountNumber;
    private String clearingNumber;
    private String fullyFormattedNumber;
    private LinksEntity links;

    public String getName() {
        return name;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getClearingNumber() {
        return clearingNumber;
    }

    public String getFullyFormattedNumber() {
        return fullyFormattedNumber;
    }

    public LinksEntity getLinks() {
        return links;
    }
}
