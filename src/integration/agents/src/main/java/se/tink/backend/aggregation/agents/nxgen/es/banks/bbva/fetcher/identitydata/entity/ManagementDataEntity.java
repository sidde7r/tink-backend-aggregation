package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.identitydata.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ManagementDataEntity {
    private boolean hasFatcaCrsOperativeBlockage;
    private boolean hasRbaOperativeBlockage;

    @JsonProperty("IsIdentified")
    private boolean isIdentified;
}
