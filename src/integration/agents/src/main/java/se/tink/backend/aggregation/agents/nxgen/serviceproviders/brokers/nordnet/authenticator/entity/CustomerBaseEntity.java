package se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CustomerBaseEntity {

    @JsonProperty("country")
    private String country;

    @JsonProperty("citizen")
    private String citizen;

    @JsonProperty("city")
    private String city;

    @JsonProperty("address1")
    private String address1;

    @JsonProperty("person_type")
    private String personType;

    @Getter
    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("national_id_number_country_code")
    private String countryCode;

    @JsonProperty("zip_code")
    private String zipCode;

    @JsonProperty("mobile_phone_number")
    private String mobilePhoneNumber;

    @JsonProperty("registration_date")
    private String registrationDate;

    @Getter
    @JsonProperty("national_id_number")
    private String nationalIdNumber;

    @Getter
    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("email")
    private String email;
}
