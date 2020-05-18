package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.GenericIdentificationEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.PostalAddressEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PartyIdentificationEntity {
    private String name;
    private PostalAddressEntity postalAddress;
    private GenericIdentificationEntity organisationId;

    @JsonIgnore
    private PartyIdentificationEntity(Builder builder) {
        this.name = builder.name;
        this.postalAddress = builder.postalAddress;
        this.organisationId = builder.organisationId;
    }

    public static class Builder {
        private String name;
        private PostalAddressEntity postalAddress;
        private GenericIdentificationEntity organisationId;

        public Builder withPostalAddress(PostalAddressEntity postalAddress) {
            this.postalAddress = postalAddress;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withReference(GenericIdentificationEntity organisationId) {
            this.organisationId = organisationId;
            return this;
        }

        public PartyIdentificationEntity build() {
            return new PartyIdentificationEntity(this);
        }
    }
}
