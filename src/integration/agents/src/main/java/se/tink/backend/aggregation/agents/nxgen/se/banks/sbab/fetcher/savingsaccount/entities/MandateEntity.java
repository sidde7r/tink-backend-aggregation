package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.savingsaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum MandateEntity {
    @JsonProperty("owner")
    OWNER,
    @JsonProperty("co_account_holder")
    CO_ACCOUNT_HOLDER,
    @JsonProperty("guardian")
    GUARDIAN,
    @JsonProperty("custodian")
    CUSTODIAN,
}
