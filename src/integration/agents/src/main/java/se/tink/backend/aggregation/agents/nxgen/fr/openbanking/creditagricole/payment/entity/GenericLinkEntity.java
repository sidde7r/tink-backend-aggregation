package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GenericLinkEntity {
    @JsonProperty("href")
    private String href = null;

    @JsonProperty("templated")
    private Boolean templated = null;

    public String getHref() {
        return href;
    }

    public Boolean getTemplated() {
        return templated;
    }
}
