package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PartyIdentificationEntity {
    @JsonProperty("name")
    private String name = null;

    @JsonProperty("postalAddress")
    private PostalAddressEntity postalAddress = null;

    @JsonProperty("organisationId")
    private GenericOrganisationIdentificationEntity organisationId = null;

    @JsonProperty("privateId")
    private GenericPrivateIdentificationEntity privateId = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PostalAddressEntity getPostalAddress() {
        return postalAddress;
    }

    public void setPostalAddress(PostalAddressEntity postalAddress) {
        this.postalAddress = postalAddress;
    }

    public GenericOrganisationIdentificationEntity getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(GenericOrganisationIdentificationEntity organisationId) {
        this.organisationId = organisationId;
    }

    public GenericPrivateIdentificationEntity getPrivateId() {
        return privateId;
    }

    public void setPrivateId(GenericPrivateIdentificationEntity privateId) {
        this.privateId = privateId;
    }
}
