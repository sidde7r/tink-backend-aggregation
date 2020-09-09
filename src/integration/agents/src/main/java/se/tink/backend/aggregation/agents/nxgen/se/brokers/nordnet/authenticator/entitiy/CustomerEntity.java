package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.entitiy;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.SeIdentityData;

@JsonObject
public class CustomerEntity {

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

    public IdentityData toTinkIdentity() {
        return SeIdentityData.of(firstName, lastName, nationalIdNumber);
    }
}
