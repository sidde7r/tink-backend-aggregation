package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DocumentsItemEntity {

    @JsonProperty("documentKey")
    private String documentKey;

    @JsonProperty("countryCode")
    private String countryCode;

    @JsonProperty("documentName")
    private String documentName;

    @JsonProperty("value")
    private String value;
}
