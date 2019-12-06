package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class AdressEntity {
    private String street;
    private String buildingNumber;
    private String postalCode;
    private String city;
    private String country;

    public AdressEntity() {}

    private AdressEntity(Builder builder) {
        this.street = builder.street;
        this.buildingNumber = builder.buildingNumber;
        this.postalCode = builder.postalCode;
        this.city = builder.city;
        this.country = builder.country;
    }

    public static class Builder {
        private String street;
        private String buildingNumber;
        private String postalCode;
        private String city;
        private String country;

        public Builder withStreet(String street) {
            this.street = street;
            return this;
        }

        public Builder withBuildingNumber(String buildingNumber) {
            this.buildingNumber = buildingNumber;
            return this;
        }

        public Builder withPostalCode(String postalCode) {
            this.postalCode = postalCode;
            return this;
        }

        public Builder withCity(String city) {
            this.city = city;
            return this;
        }

        public Builder withCountry(String country) {
            this.country = country;
            return this;
        }

        public AdressEntity build() {
            return new AdressEntity(this);
        }
    }
}
