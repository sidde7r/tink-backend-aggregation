package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ScaMethodsItemEntity {

    @JsonProperty("_links")
    private LinksEntity linksEntity;

    private String scaMethodType;

    public LinksEntity getLinksEntity() {
        return linksEntity;
    }

    public String getScaMethodType() {
        return scaMethodType;
    }
}
