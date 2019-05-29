package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.identitydata.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.identitydata.entities.AddressEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.FiIdentityData;

@JsonObject
public class CustomerInfoResponse {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @JsonProperty("customer_id")
    private String customerId;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("birth_date")
    private String birthDate;

    @JsonProperty("loyalty_group")
    private String loyaltyGroup;

    private String segment;
    private AddressEntity address;
    private boolean employee;

    @JsonProperty("us_resident")
    private boolean usResident;

    private String gender;

    @JsonIgnore
    private String getFullName() {
        // only seen first_name empty string and last_name containing both
        boolean hasFirstName = !Strings.isNullOrEmpty(firstName);
        boolean hasLastName = !Strings.isNullOrEmpty(lastName);

        if (hasFirstName && hasLastName) {
            return firstName + " " + lastName;
        } else if (hasFirstName) {
            return firstName;
        } else if (hasLastName) {
            return lastName;
        } else {
            return null;
        }
    }

    @JsonIgnore
    public IdentityData toIdentityData() {
        try {
            return FiIdentityData.of(getFullName(), customerId);
        } catch (IllegalArgumentException e) {
            // not a SSN?
            return IdentityData.builder()
                    .setFullName(getFullName())
                    .setDateOfBirth(LocalDate.parse(birthDate, DATE_FORMATTER))
                    .build();
        }
    }
}
