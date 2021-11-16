package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
@JsonObject
public class TaxPartyEntity {
    @JsonProperty("taxIdentification")
    private String taxIdentification = null;

    @JsonProperty("registrationIdentification")
    private String registrationIdentification = null;

    @JsonProperty("taxType")
    private String taxType = null;

    @JsonProperty("authorisation")
    private TitleAndNameEntity authorisation = null;
}
