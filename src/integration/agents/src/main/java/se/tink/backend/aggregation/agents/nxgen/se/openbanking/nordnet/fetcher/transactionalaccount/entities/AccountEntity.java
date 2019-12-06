package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountEntity {
    @JsonProperty("accno")
    private long accountNumber;

    private String type;

    @JsonProperty("default")
    private boolean isDefault;

    private String alias;

    public long getAccountNumber() {
        return accountNumber;
    }
}
