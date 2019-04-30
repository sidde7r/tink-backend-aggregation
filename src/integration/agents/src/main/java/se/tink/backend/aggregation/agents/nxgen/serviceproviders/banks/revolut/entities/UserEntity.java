package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.identitydata.IdentityData;

@JsonObject
public class UserEntity {
    private String id;
    private long createdDate;
    private AddressEntity address;
    private List<Integer> birthDate;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private boolean emailVerified;
    private String state;
    private String referralCode;
    private String kyc;
    private String termsVersion;
    private boolean underReview;
    private boolean riskAssessed;
    private String locale;

    public String getId() {
        return id;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public AddressEntity getAddress() {
        return address;
    }

    public List<Integer> getBirthDate() {
        return birthDate;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public String getState() {
        return state;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public String getKyc() {
        return kyc;
    }

    public String getTermsVersion() {
        return termsVersion;
    }

    public boolean isUnderReview() {
        return underReview;
    }

    public boolean isRiskAssessed() {
        return riskAssessed;
    }

    public String getLocale() {
        return locale;
    }

    @JsonIgnore
    public String getFullName() {
        return String.format("%s %s", firstName, lastName);
    }

    @JsonIgnore
    public IdentityData toTinkIdentity() {
        return IdentityData.builder()
                .addFirstNameElement(firstName)
                .addSurnameElement(lastName)
                .setDateOfBirth(getBirthDateAsLocalDate())
                .build();
    }

    @JsonIgnore
    public LocalDate getBirthDateAsLocalDate() {
        if (birthDate.size() != 3) {
            return null;
        } else {
            return LocalDate.of(birthDate.get(0), birthDate.get(1), birthDate.get(2));
        }
    }
}
