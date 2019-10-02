package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MobileEntity {
    @JsonProperty("to_phone")
    private String toPhone;

    @JsonProperty("from_phone")
    private String fromPhone;

    private String reference;
    private String name;
}
