package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

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

    private PartyIdentificationEntity(
            String name,
            PostalAddressEntity postalAddress,
            GenericOrganisationIdentificationEntity organisationId,
            GenericPrivateIdentificationEntity privateId) {
        this.name = name;
        this.postalAddress = postalAddress;
        this.organisationId = organisationId;
        this.privateId = privateId;
    }

    public static PartyIdentificationEntityBuilder builder() {
        return new PartyIdentificationEntityBuilder();
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

    public static class PartyIdentificationEntityBuilder {

        private String name;
        private PostalAddressEntity postalAddress;
        private GenericOrganisationIdentificationEntity organisationId;
        private GenericPrivateIdentificationEntity privateId;

        PartyIdentificationEntityBuilder() {}

        public PartyIdentificationEntityBuilder name(String name) {
            this.name = name;
            return this;
        }

        public PartyIdentificationEntityBuilder postalAddress(PostalAddressEntity postalAddress) {
            this.postalAddress = postalAddress;
            return this;
        }

        public PartyIdentificationEntityBuilder organisationId(
                GenericOrganisationIdentificationEntity organisationId) {
            this.organisationId = organisationId;
            return this;
        }

        public PartyIdentificationEntityBuilder privateId(
                GenericPrivateIdentificationEntity privateId) {
            this.privateId = privateId;
            return this;
        }

        public PartyIdentificationEntity build() {
            return new PartyIdentificationEntity(name, postalAddress, organisationId, privateId);
        }

        public String toString() {
            return "PartyIdentificationEntity.PartyIdentificationEntityBuilder(name="
                    + this.name
                    + ", postalAddress="
                    + this.postalAddress
                    + ", organisationId="
                    + this.organisationId
                    + ", privateId="
                    + this.privateId
                    + ")";
        }
    }
}
