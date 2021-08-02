package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class PartyIdentificationEntity {
    @JsonProperty("name")
    private String name;

    @JsonProperty("postalAddress")
    private PostalAddressEntity postalAddress;

    @JsonProperty("organisationId")
    private GenericOrganisationIdentificationEntity organisationId;

    @JsonProperty("privateId")
    private GenericPrivateIdentificationEntity privateId;

    @JsonCreator
    public PartyIdentificationEntity(
            @JsonProperty("name") String name,
            @JsonProperty("postalAddress") PostalAddressEntity postalAddress,
            @JsonProperty("organisationId") GenericOrganisationIdentificationEntity organisationId,
            @JsonProperty("privateId") GenericPrivateIdentificationEntity privateId) {
        this.name = name;
        this.postalAddress = postalAddress;
        this.organisationId = organisationId;
        this.privateId = privateId;
    }
}
