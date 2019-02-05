package se.tink.backend.aggregation.agents.models.fraud;

import java.util.Date;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FraudIdentityContent extends FraudDetailsContent {

    private String personIdentityNumber;
    private String lastName;
    private String firstName;
    private String givenName;
    private Date blocked;
    
    public String getPersonIdentityNumber() {
        return personIdentityNumber;
    }
    public void setPersonIdentityNumber(String personIdentityNumber) {
        this.personIdentityNumber = personIdentityNumber;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getGivenName() {
        return givenName;
    }
    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }
    
    @Override
    public String generateContentId() {
        return String.valueOf(Objects.hash(itemType(), personIdentityNumber, firstName, lastName, blocked));
    }
    
    @Override
    public FraudTypes itemType() {
        return FraudTypes.IDENTITY;
    }
    public Date getBlocked() {
        return blocked;
    }
    public void setBlocked(Date blocked) {
        this.blocked = blocked;
    }
}
