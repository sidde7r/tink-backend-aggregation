package se.tink.libraries.identity.model;

import com.google.common.collect.Lists;
import java.util.List;

public class Identity {
    private String firstName;
    private String lastName;
    private String nationalId;
    private Address address;
    private List<Property> properties;
    private List<CompanyEngagement> companies;
    private CreditScore creditScore;
    private TaxDeclaration mostRecentTaxDeclaration;
    private List<RecordOfNonPayment> recordsOfNonPayment;
    private OutstandingDebt outstandingDebt;

    public Identity() {
        properties = Lists.newArrayList();
        companies = Lists.newArrayList();
        recordsOfNonPayment = Lists.newArrayList();
    }

    public Identity(String firstName, String lastName, String nationalId, Address address,
            List<Property> properties, List<CompanyEngagement> companies,
            CreditScore creditScore, TaxDeclaration mostRecentTaxDeclaration,
            List<RecordOfNonPayment> recordsOfNonPayment, OutstandingDebt outstandingDebt) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.nationalId = nationalId;
        this.address = address;
        this.properties = properties;
        this.companies = companies;
        this.creditScore = creditScore;
        this.mostRecentTaxDeclaration = mostRecentTaxDeclaration;
        this.recordsOfNonPayment = recordsOfNonPayment;
        this.outstandingDebt = outstandingDebt;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getNationalId() {
        return nationalId;
    }

    public Address getAddress() {
        return address;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void addProperty(Property property) {
        properties.add(property);
    }

    public List<CompanyEngagement> getCompanies() {
        return companies;
    }

    public CreditScore getCreditScore() {
        return creditScore;
    }

    public TaxDeclaration getMostRecentTaxDeclaration() {
        return mostRecentTaxDeclaration;
    }

    public List<RecordOfNonPayment> getRecordsOfNonPayment() {
        return recordsOfNonPayment;
    }

    public OutstandingDebt getOutstandingDebt() {
        return outstandingDebt;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public void setCompanies(List<CompanyEngagement> companies) {
        this.companies = companies;
    }

    public void setCreditScore(CreditScore creditScore) {
        this.creditScore = creditScore;
    }

    public void setMostRecentTaxDeclaration(TaxDeclaration mostRecentTaxDeclaration) {
        this.mostRecentTaxDeclaration = mostRecentTaxDeclaration;
    }

    public void setRecordsOfNonPayment(List<RecordOfNonPayment> recordsOfNonPayment) {
        this.recordsOfNonPayment = recordsOfNonPayment;
    }

    public void setOutstandingDebt(OutstandingDebt outstandingDebt) {
        this.outstandingDebt = outstandingDebt;
    }

    public void addRecordOfNonPaymentIfValid(RecordOfNonPayment recordOfNonPayment) {
        if (recordOfNonPayment.isPaymentNoteValid()) {
            addRecordOfNonPayment(recordOfNonPayment);
        }
    }

    public void addRecordOfNonPayment(RecordOfNonPayment recordOfNonPayment) {
        this.recordsOfNonPayment.add(recordOfNonPayment);
    }

    public void setOutstandingDebtIfMoreRecent(OutstandingDebt newDebtEntry) {
        if (isNewOutstandingDebtMoreRecent(newDebtEntry)) {
            outstandingDebt = newDebtEntry;
        }
    }

    private boolean isNewOutstandingDebtMoreRecent(OutstandingDebt newDebt) {
        if (outstandingDebt == null) {
            return true;
        }

        if (outstandingDebt.getRegisteredDate() != null && newDebt.getRegisteredDate() != null) {
            return newDebt.getRegisteredDate().after(outstandingDebt.getRegisteredDate());
        }

        // If we don't have a registered date, fallback to the date when the fraud item was created in our database.
        return newDebt.getCreatedDate().after(outstandingDebt.getCreatedDate());
    }

    public void setTaxDeclarationIfMoreRecent(TaxDeclaration newDeclarationEntry) {
        if (isNewTaxDeclarationMoreRecent(newDeclarationEntry)) {
            mostRecentTaxDeclaration = newDeclarationEntry;
        }
    }

    private boolean isNewTaxDeclarationMoreRecent(TaxDeclaration newDeclaration) {
        if (mostRecentTaxDeclaration == null) {
            return true;
        }

        if (mostRecentTaxDeclaration.getRegisteredDate() != null && newDeclaration.getRegisteredDate() != null) {
            return newDeclaration.getRegisteredDate().after(mostRecentTaxDeclaration.getRegisteredDate());
        }

        // If we don't have a registered date, fallback to the date when the fraud item was created in our database.
        return newDeclaration.getCreatedDate().after(mostRecentTaxDeclaration.getCreatedDate());
    }

    public void addCompanyEngagement(CompanyEngagement companyEngagement) {
        this.companies.add(companyEngagement);
    }

    public void removeCompanyEngagement(Company company) {
        companies.removeIf(c -> c.getCompany().equals(company));
    }
}
