package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.insurance;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InsuranceReferenceEntity {
    @JsonProperty("Number")
    private String number;

    // @JsonProperty("EncryptedNumber")
    // `EncryptedNumber` is null - cannot define it!
}
