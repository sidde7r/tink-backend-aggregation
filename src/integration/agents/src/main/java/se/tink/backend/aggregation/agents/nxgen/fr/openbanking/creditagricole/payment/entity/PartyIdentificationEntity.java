package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class PartyIdentificationEntity {
    @JsonProperty("name")
    private String name = null;

    @JsonProperty("postalAddress")
    private PostalAddressEntity postalAddress = null;

    @JsonProperty("organisationId")
    private GenericOrganisationIdentificationEntity organisationId = null;

    @JsonProperty("privateId")
    private GenericPrivateIdentificationEntity privateId = null;

    public PartyIdentificationEntity() {}

    public PartyIdentificationEntity(
            String name,
            PostalAddressEntity postalAddress,
            GenericOrganisationIdentificationEntity organisationId,
            GenericPrivateIdentificationEntity privateId) {
        this.name = name;
        this.postalAddress = postalAddress;
        this.organisationId = organisationId;
        this.privateId = privateId;
    }

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
