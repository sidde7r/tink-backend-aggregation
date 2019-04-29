package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.entities;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import se.tink.backend.aggregation.annotations.JsonObject;

@SuppressWarnings("unused")
@JsonObject
public class UserInfoEntity {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String kycFirstName;
    private String kycLastName;
    private String title;
    private String gender;
    private long birthDate;
    private boolean signupCompleted;
    private String nationality;
    private String mobilePhoneNumber;
    private String shadowID;

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getBirthDate() {
        return Instant.ofEpochMilli(birthDate).atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
