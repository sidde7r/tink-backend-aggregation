package se.tink.backend.aggregation.agents.brokers.nordnet.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.SeIdentityData;

@JsonObject
public class CustomerInfoResponse {
    private String address1;
    private String city;
    private String country;
    private String citizen;
    private String email;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("national_id_number")
    private String nationalIdNumber;

    @JsonProperty("national_id_number_country_code")
    private String nationalIdNumberCountryCode;

    @JsonProperty("zip_code")
    private String zipCode;

    @JsonProperty("registration_date")
    private String registrationDate;

    @JsonProperty("person_type")
    private String personType;

    public IdentityData toTinkIdentity() {
        return SeIdentityData.of(firstName, lastName, nationalIdNumber);
    }
}
