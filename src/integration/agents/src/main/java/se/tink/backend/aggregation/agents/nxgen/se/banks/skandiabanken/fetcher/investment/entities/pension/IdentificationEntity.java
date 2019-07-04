package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.pension;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IdentificationEntity {

    @JsonProperty("NationalIdentificationNumberOfEntitledToChangeFunds")
    private String nationalIdentificationNumberOfEntitledToChangeFunds;

    @JsonProperty("NationalIdentificationNumberOfFirstInsured")
    private String nationalIdentificationNumberOfFirstInsured;

    @JsonProperty("NationalIdentificationNumberOfOwner")
    private String nationalIdentificationNumberOfOwner;
}
