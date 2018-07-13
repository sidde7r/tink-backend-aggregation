package se.tink.backend.aggregation.agents.banks.se.collector.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.application.GenericApplicationFieldGroup;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.backend.utils.ApplicationUtils;

public class CreateAccountRequest {
    @JsonProperty("Adress")
    private String address;
    @JsonProperty("FirstName")
    private String firstName;
    @JsonProperty("LastName")
    private String lastName;
    @JsonProperty("PostCode")
    private String zipCode;
    @JsonProperty("City")
    private String city;
    @JsonProperty("Account")
    private WithdrawalAccount withdrawalAccount;
    @JsonProperty("MobileNumber")
    private String mobileNumber;
    @JsonProperty("EmailAddress")
    private String emailAddress;
    @JsonProperty("SigningReference")
    private String signingReference;

    private void setAddress(String address) {
        this.address = address;
    }

    private void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    private void setLastName(String lastName) {
        this.lastName = lastName;
    }

    private void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    private void setCity(String city) {
        this.city = city;
    }

    private void setWithdrawalAccount(WithdrawalAccount account) {
        this.withdrawalAccount = account;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getAddress() {
        return address;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getCity() {
        return city;
    }

    public WithdrawalAccount getWithdrawalAccount() {
        return withdrawalAccount;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public static CreateAccountRequest from(GenericApplication application, String signingReference) {
        
        ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName = Multimaps.index(
                application.getFieldGroups(), GenericApplicationFieldGroup::getName);

        Optional<GenericApplicationFieldGroup> applicantFieldGroup = ApplicationUtils.getApplicant(fieldGroupByName);

        Preconditions.checkState(applicantFieldGroup.isPresent(),
                "No applicant data supplied.");
        
        String firstName = "";
        String lastName = "";
        
        String name = applicantFieldGroup.get().getField(ApplicationFieldName.NAME);

        if (!Strings.isNullOrEmpty(name)) {
            String[] nameParts = name.split(" ", 2);
            if (nameParts.length > 0) {
                firstName = nameParts[0];
                if (nameParts.length > 1) {
                    lastName = nameParts[1];
                }
            }
        }

        CreateAccountRequest request = new CreateAccountRequest();
        request.setAddress(applicantFieldGroup.get().getField(ApplicationFieldName.STREET_ADDRESS));
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setZipCode(applicantFieldGroup.get().getField(ApplicationFieldName.POSTAL_CODE));
        request.setCity(applicantFieldGroup.get().getField(ApplicationFieldName.TOWN));
        request.setWithdrawalAccount(WithdrawalAccount.from(fieldGroupByName));
        request.setMobileNumber(applicantFieldGroup.get().getField(ApplicationFieldName.PHONE_NUMBER));
        request.setEmailAddress(applicantFieldGroup.get().getField(ApplicationFieldName.EMAIL));
        request.setSigningReference(signingReference);

        return request;
    }

    public String getSigningReference() {
        return signingReference;
    }

    public void setSigningReference(String signingReference) {
        this.signingReference = signingReference;
    }
}
