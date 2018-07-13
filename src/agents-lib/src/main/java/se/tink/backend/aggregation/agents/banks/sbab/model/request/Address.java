package se.tink.backend.aggregation.agents.banks.sbab.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.libraries.application.GenericApplicationFieldGroup;
import se.tink.backend.core.enums.ApplicationFieldName;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Address {

    // The street address including street number (required).
    @JsonProperty("gatuadress")
    private String streetAddress;

    // The postal code (required).
    @JsonProperty("postnr")
    private String postalCode;

    // The post town (required).
    @JsonProperty("postort")
    private String postTown;

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPostTown() {
        return postTown;
    }

    public void setPostTown(String postTown) {
        this.postTown = postTown;
    }

    public static Address createFromApplication(GenericApplicationFieldGroup applicantGroup) {
        Address address = new Address();
        address.setStreetAddress(applicantGroup.getField(ApplicationFieldName.STREET_ADDRESS));
        address.setPostalCode(applicantGroup.getField(ApplicationFieldName.POSTAL_CODE));
        address.setPostTown(applicantGroup.getField(ApplicationFieldName.TOWN));
        return address;
    }
}
