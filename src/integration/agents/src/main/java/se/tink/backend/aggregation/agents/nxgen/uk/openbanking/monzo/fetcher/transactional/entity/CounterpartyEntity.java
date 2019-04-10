package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CounterpartyEntity {

    @JsonProperty("account_number")
    private String accountNumber;

    private String name;

    @JsonProperty("sort_code")
    private String sortCode;
}
