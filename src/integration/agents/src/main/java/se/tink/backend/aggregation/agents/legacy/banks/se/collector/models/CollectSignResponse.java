package se.tink.backend.aggregation.agents.banks.se.collector.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CollectSignResponse extends CollectBankIdResponse {
    @JsonProperty("signature_reference")
    private String signatureReference;

    @JsonProperty("name")
    private String fullName;

    @JsonProperty("given_name")
    private String firstName;

    @JsonProperty("family_name")
    private String lastName;

    @JsonProperty("personal_number")
    private String ssn;

    public String getSignatureReference() {
        return signatureReference;
    }

    public void setSignatureReference(String signatureReference) {
        this.signatureReference = signatureReference;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    @Override
    public boolean isValid() {
        return !Strings.isNullOrEmpty(signatureReference);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("status", getStatus())
                .add(
                        "signature_reference",
                        String.format("exists(%s)", !Strings.isNullOrEmpty(signatureReference)))
                .add("name", fullName)
                .add("given_name", firstName)
                .add("family_name", lastName)
                .add("personal_number", ssn)
                .toString();
    }
}
