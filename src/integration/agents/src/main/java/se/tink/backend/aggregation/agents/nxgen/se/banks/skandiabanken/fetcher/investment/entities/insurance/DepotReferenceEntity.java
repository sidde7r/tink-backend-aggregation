package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.insurance;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DepotReferenceEntity {
    @JsonProperty("Number")
    private String number;

    @JsonProperty("EncryptedNumber")
    private String encryptedNumber;
}
