package se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.fetcher.rpc.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankAccountEntity {

    @JsonProperty("clearing_number")
    private String clearingNumber;

    @JsonProperty("bank_account_number")
    private String bankAccountNumber;
}
