package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CurrentAgreementEntity {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Selectable")
    private boolean selectable;

    @JsonProperty("Type")
    private String type;

    @JsonProperty("TypeId")
    private String typeId;
}
