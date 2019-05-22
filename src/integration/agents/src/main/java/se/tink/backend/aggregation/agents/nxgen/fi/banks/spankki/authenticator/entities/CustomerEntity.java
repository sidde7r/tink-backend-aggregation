package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.identitydata.IdentityData;

@JsonObject
public class CustomerEntity {
    private String customerId;
    private String firstName;
    private String lastName;
    private String memberNumber;
    private String email;
    private boolean allowPhoneMarketing;
    private boolean allowEmailMarketing;
    private String analyticsUserId;
    private String genre;
    private String yearOfBirth;
    private String coOpName;
    private String coOpLogoUrl;
    private String coOpMembershipRole;
    private boolean isCoOpMember;
    private int warrantyReceiptCount;

    public String getCustomerId() {
        return customerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getMemberNumber() {
        return memberNumber;
    }

    public String getEmail() {
        return email;
    }

    public boolean getAllowPhoneMarketing() {
        return allowPhoneMarketing;
    }

    public boolean getAllowEmailMarketing() {
        return allowEmailMarketing;
    }

    public String getAnalyticsUserId() {
        return analyticsUserId;
    }

    public String getGenre() {
        return genre;
    }

    public String getYearOfBirth() {
        return yearOfBirth;
    }

    public String getCoOpName() {
        return coOpName;
    }

    public String getCoOpLogoUrl() {
        return coOpLogoUrl;
    }

    public String getCoOpMembershipRole() {
        return coOpMembershipRole;
    }

    public boolean getCoOpMember() {
        return isCoOpMember;
    }

    public int getWarrantyReceiptCount() {
        return warrantyReceiptCount;
    }

    public IdentityData toTinkIdentity() {
        return IdentityData.builder()
                .addFirstNameElement(firstName)
                .addSurnameElement(lastName)
                .setDateOfBirth(null)
                .build();
    }
}
