package se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Optional;
import se.tink.backend.common.i18n.SocialSecurityNumber;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.backend.product.execution.unit.agents.exceptions.application.InvalidApplicationException;
import se.tink.libraries.application.GenericApplicationFieldGroup;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Borrower {
    // The employment status of the borrower (required).
    @JsonProperty("anstallningsform")
    private String employmentStatus;

    // The employer (required).
    @JsonProperty("arbetsgivare")
    private String employer;

    // The civil status/marital status (required).
    @JsonProperty("civilstand")
    private String civilStatus;

    // The first name/given name (required).
    @JsonProperty("fornamn")
    private String firstName;

    // The last name/surname (required).
    @JsonProperty("efternamn")
    private String lastName;

    // The email address (required).
    @JsonProperty("emailadress")
    private String emailAddress;

    // The monthly income before taxes in SEK (required).
    @JsonProperty("manadsinkomst")
    private Integer monthlyIncome;

    // The social security number/personal number on the format 'yyyymmdd-nnnn' (required).
    @JsonProperty("personnummer")
    private String socialSecurityNumber;

    // If the borrower accepts that the personal information in the mortgage application or information which is
    // collected and registered in the process will be handled in data systems at SBAB SBAB Bank AB (publ) (required).
    @JsonProperty("pulGodkannande")
    private Boolean acceptsHandlingOfPersonalInformation;

    // Telephone number on the format '(\+|0){1}[0-9]{1,5}(\-){0,1}[0-9]{5,10}' (required).
    @JsonProperty("telefonnummer")
    private String telephoneNumber;

    // The form of housing/in what type of place the borrower lives (required).
    @JsonProperty("boendeform")
    private String formOfHousing;

    // If the address of the borrower is different than the household address (not required).
    @JsonProperty("avvikandeAdress")
    private Address differentAddress;

    // If the borrower has other properties to keep or sell (not required).
    @JsonProperty("annanFastighet")
    private List<OtherProperty> otherProperties;

    // If the borrower has other condominiums to keep or sell (not required).
    @JsonProperty("annanBostadsratt")
    private List<OtherCondominium> otherCondominiums;

    public String getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(String employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public String getEmployer() {
        return employer;
    }

    public void setEmployer(String employer) {
        this.employer = employer;
    }

    public String getCivilStatus() {
        return civilStatus;
    }

    public void setCivilStatus(String civilStatus) {
        this.civilStatus = civilStatus;
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

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public Integer getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(Integer monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public String getSocialSecurityNumber() {
        return socialSecurityNumber;
    }

    public void setSocialSecurityNumber(String socialSecurityNumber) {
        this.socialSecurityNumber = socialSecurityNumber;
    }

    public Boolean isAcceptsHandlingOfPersonalInformation() {
        return acceptsHandlingOfPersonalInformation;
    }

    public void setAcceptsHandlingOfPersonalInformation(Boolean acceptsHandlingOfPersonalInformation) {
        this.acceptsHandlingOfPersonalInformation = acceptsHandlingOfPersonalInformation;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public String getFormOfHousing() {
        return formOfHousing;
    }

    public void setFormOfHousing(String formOfHousing) {
        this.formOfHousing = formOfHousing;
    }

    public Address getDifferentAddress() {
        return differentAddress;
    }

    public void setDifferentAddress(Address differentAddress) {
        this.differentAddress = differentAddress;
    }

    public List<OtherProperty> getOtherProperties() {
        return otherProperties;
    }

    public void setOtherProperties(List<OtherProperty> otherProperties) {
        this.otherProperties = otherProperties;
    }

    public List<OtherCondominium> getOtherCondominiums() {
        return otherCondominiums;
    }

    public void setOtherCondominiums(List<OtherCondominium> otherCondominiums) {
        this.otherCondominiums = otherCondominiums;
    }

    public static Borrower createBorrowerFromApplication(GenericApplicationFieldGroup applicantGroup)
            throws InvalidApplicationException {
        Borrower borrower = new BorrowerWithNewFields();
        borrower.setAcceptsHandlingOfPersonalInformation(true);

        Optional<String> civilStatus = MortgageApplicationMapper.getCivilStatus(applicantGroup
                .getField(ApplicationFieldName.RELATIONSHIP_STATUS));
        Preconditions.checkState(civilStatus.isPresent(), "Missing relationship status.");
        borrower.setCivilStatus(civilStatus.get());

        Optional<String> emailAddress = applicantGroup.tryGetField(ApplicationFieldName.EMAIL);
        Preconditions.checkState(emailAddress.isPresent(), "Missing email address.");
        borrower.setEmailAddress(emailAddress.get());

        Optional<EmploymentStatus> employmentStatus = MortgageApplicationMapper
                .getEmploymentStatus(applicantGroup.getField(ApplicationFieldName.EMPLOYMENT_TYPE));
        Preconditions.checkState(employmentStatus.isPresent(), "Missing employer status.");
        borrower.setEmploymentStatus(employmentStatus.get().toString());

        if (isEmploymentStatusRequiringEmployer(employmentStatus.get())) {
            Optional<String> employer = applicantGroup.tryGetField(ApplicationFieldName.EMPLOYER_OR_COMPANY_NAME);
            Preconditions.checkState(employer.isPresent(), "Missing employer/company name.");
            borrower.setEmployer(employer.get());

            if (borrower instanceof BorrowerWithNewFields) {
                BorrowerWithNewFields borrowerWithNewFields = ((BorrowerWithNewFields) borrower);

                Optional<String> employeeSince = applicantGroup.tryGetField(ApplicationFieldName.EMPLOYEE_SINCE);
                Preconditions.checkState(employeeSince.isPresent(), "Missing employee since");
                borrowerWithNewFields.setEmploymentDate(employeeSince.get());

                Optional<String> profession = applicantGroup.tryGetField(ApplicationFieldName.PROFESSION);
                Preconditions.checkState(profession.isPresent(), "Missing profession.");
                borrowerWithNewFields.setProfession(profession.get());
            }
        }

        Optional<String> firstName = applicantGroup.tryGetField(ApplicationFieldName.FIRST_NAME);
        Preconditions.checkState(firstName.isPresent(), "Missing first name.");
        borrower.setFirstName(firstName.get());

        Optional<String> formOfHousing = MortgageApplicationMapper.getFormOfHousing(applicantGroup
                .getField(ApplicationFieldName.RESIDENCE_PROPERTY_TYPE));
        Preconditions.checkState(formOfHousing.isPresent(), "Missing residence property type.");
        borrower.setFormOfHousing(formOfHousing.get());

        Optional<String> lastName = applicantGroup.tryGetField(ApplicationFieldName.LAST_NAME);
        Preconditions.checkState(lastName.isPresent(), "Missing last name.");
        borrower.setLastName(lastName.get());

        Optional<Integer> monthlyIncome = applicantGroup.tryGetFieldAsInteger(ApplicationFieldName.MONTHLY_INCOME);
        Preconditions.checkState(monthlyIncome.isPresent(), "Missing monthly income.");
        borrower.setMonthlyIncome(monthlyIncome.get());

        Optional<String> ssn = applicantGroup.tryGetField(ApplicationFieldName.PERSONAL_NUMBER);
        Preconditions.checkState(ssn.isPresent(), "Missing SSN.");
        borrower.setSocialSecurityNumber(getPersonalNumberWithDash(ssn.get()));

        Optional<String> phoneNumber = applicantGroup.tryGetField(ApplicationFieldName.PHONE_NUMBER);
        Preconditions.checkState(phoneNumber.isPresent(), "Missing phone number.");
        borrower.setTelephoneNumber(phoneNumber.get());

        return borrower;
    }

    private static String getPersonalNumberWithDash(String personalNumber) throws InvalidApplicationException {
        SocialSecurityNumber.Sweden ssn = new SocialSecurityNumber.Sweden(personalNumber);
        if (!ssn.isValid()) {
            String failedSsn = ssn.asStringWithoutValidityCheck();
            throw new InvalidApplicationException("Du har angivit ett felaktigt personnummer: " + failedSsn);
        }

        return ssn.asStringWithDash();
    }

    private static boolean isEmploymentStatusRequiringEmployer(EmploymentStatus employmentStatus) {
        switch (employmentStatus) {
        case TILLSVIDARE:
        case EGEN_FORETAGARE:
        case VISSTIDS:
            return true;
        default:
            return false;
        }
    }
}
