package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountOwnerEntity {
    @JsonProperty("FullName")
    private String fullName;

    @JsonProperty("NationalIdWithCentury")
    private String nationalIdWithCentury;
}
