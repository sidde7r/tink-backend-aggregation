package se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.backend.core.enums.GenericApplicationFieldGroupNames;
import se.tink.backend.product.execution.log.ProductExecutionLogger;
import se.tink.backend.product.execution.unit.agents.exceptions.application.InvalidApplicationException;
import se.tink.backend.utils.ApplicationUtils;
import se.tink.libraries.application.ApplicationFieldOptionValues;
import se.tink.libraries.application.GenericApplicationFieldGroup;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Household {
    private static final ProductExecutionLogger log = new ProductExecutionLogger(Household.class);

    // The monthly amortization amount of the student loan debt (not required).
    @JsonProperty("amorteringStudieskuld")
    private Integer monthlyAmortizationOfStudentLoan;

    // The value of the other assets that the applicant has in SEK (not required).
    @JsonProperty("ovrigaTillgangar")
    private Integer otherAssets;

    // The student loan debt in SEK (not required).
    @JsonProperty("studieskuld")
    private Integer studentLoanDebt;

    // The value of the postponed tax payment on a property, in SEK (not required).
    @JsonProperty("uppskov")
    private Integer postponedTaxPaymentOnProperty;

    // The monthly income in SEK (not required).
    @JsonProperty("inkomstKapital")
    private Integer monthlyIncome;

    // The cost of child care, for example nursery school fees (not required).
    @JsonProperty("barnomsorg")
    private Integer childCareCosts;

    // The number of children in the household for which the applicant receives child allowances (not required).
    @JsonProperty("antalBarnMedBidrag")
    private Integer numberOfChildrenWithChildAllowances;

    // The number of children for which the applicant must pay child support money (not required).
    @JsonProperty("antalBarnUnderhallAttBetala")
    private Integer numberOfChildSupportsToPay;

    // The number of children for which the applicant receives child support money (not required).
    @JsonProperty("antalBarnUnderhallAttErhalla")
    private Integer numberOfChildSupportsToReceive;

    // The address of the applicant (required).
    @JsonProperty("adress")
    private Address address;

    // Information about the applicant(s) (Min = 1, max = 2).
    @JsonProperty("lantagare")
    private List<Borrower> borrowers;

    // Information about existing loans which the applicant has (not required).
    @JsonProperty("befintligtLan")
    private List<ExistingLoan> existingLoans;

    public Integer getMonthlyAmortizationOfStudentLoan() {
        return monthlyAmortizationOfStudentLoan;
    }

    public void setMonthlyAmortizationOfStudentLoan(Integer monthlyAmortizationOfStudentLoan) {
        this.monthlyAmortizationOfStudentLoan = monthlyAmortizationOfStudentLoan;
    }

    public Integer getOtherAssets() {
        return otherAssets;
    }

    public void setOtherAssets(Integer otherAssets) {
        this.otherAssets = otherAssets;
    }

    public Integer getStudentLoanDebt() {
        return studentLoanDebt;
    }

    public void setStudentLoanDebt(Integer studentLoanDebt) {
        this.studentLoanDebt = studentLoanDebt;
    }

    public Integer getPostponedTaxPaymentOnProperty() {
        return postponedTaxPaymentOnProperty;
    }

    public void setPostponedTaxPaymentOnProperty(Integer postponedTaxPaymentOnProperty) {
        this.postponedTaxPaymentOnProperty = postponedTaxPaymentOnProperty;
    }

    public Integer getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(Integer monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public Integer getChildCareCosts() {
        return childCareCosts;
    }

    public void setChildCareCosts(Integer childCareCosts) {
        this.childCareCosts = childCareCosts;
    }

    public Integer getNumberOfChildrenWithChildAllowances() {
        return numberOfChildrenWithChildAllowances;
    }

    public void setNumberOfChildrenWithChildAllowances(Integer numberOfChildrenWithChildAllowances) {
        this.numberOfChildrenWithChildAllowances = numberOfChildrenWithChildAllowances;
    }

    public Integer getNumberOfChildSupportsToPay() {
        return numberOfChildSupportsToPay;
    }

    public void setNumberOfChildSupportsToPay(Integer numberOfChildSupportsToPay) {
        this.numberOfChildSupportsToPay = numberOfChildSupportsToPay;
    }

    public Integer getNumberOfChildSupportsToReceive() {
        return numberOfChildSupportsToReceive;
    }

    public void setNumberOfChildSupportsToReceive(Integer numberOfChildSupportsToReceive) {
        this.numberOfChildSupportsToReceive = numberOfChildSupportsToReceive;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<Borrower> getBorrowers() {
        return borrowers;
    }

    public void setBorrowers(List<Borrower> borrowers) {
        this.borrowers = borrowers;
    }

    public List<ExistingLoan> getExistingLoans() {
        return existingLoans;
    }

    public void setExistingLoans(List<ExistingLoan> existingLoans) {
        this.existingLoans = existingLoans;
    }

    public static Household createFromApplication(List<GenericApplicationFieldGroup> applicantGroups,
            GenericApplicationFieldGroup householdGroup, GenericApplicationFieldGroup currentMortgageGroup,
            GenericApplicationFieldGroup mortgageSecurityGroup)
            throws InvalidApplicationException {

        List<Borrower> borrowers = Lists.newArrayList();
        Double monthlyAmortizationOfStudentLoan = 0d;
        int residenceId = 0;

        List<ExistingLoan> existingLoans = Lists.newArrayList();
        existingLoans.addAll(ExistingLoan.createFromApplication(
                ExistingLoan.Type.fromPropertyTypeOption(
                        mortgageSecurityGroup.getField(ApplicationFieldName.PROPERTY_TYPE)),
                currentMortgageGroup));

        for (GenericApplicationFieldGroup applicantGroup : applicantGroups) {
            Borrower borrower = Borrower.createBorrowerFromApplication(applicantGroup);
            borrowers.add(borrower);

            // Summarize student loans
            Optional<Double> amount = applicantGroup.tryGetFieldAsDouble(ApplicationFieldName.STUDENT_LOAN_MONTHLY_COST);
            if (amount.isPresent()) {
                monthlyAmortizationOfStudentLoan += amount.get();
            }

            // Other residences
            List<OtherCondominium> otherCondominiums = Lists.newArrayList();
            List<OtherProperty> otherProperties = Lists.newArrayList();

            for (GenericApplicationFieldGroup otherPropertyGroup : ApplicationUtils.getSubGroups(applicantGroup,
                    GenericApplicationFieldGroupNames.PROPERTY)) {

                Optional<String> propertyType = otherPropertyGroup.tryGetField(ApplicationFieldName.TYPE);

                if (!propertyType.isPresent() || Strings.isNullOrEmpty(propertyType.get())) {
                    continue;
                }

                String residenceReference = null;

                switch (propertyType.get()) {
                case ApplicationFieldOptionValues.APARTMENT: {
                    OtherCondominium otherCondominium = OtherCondominium.createFromApplication(otherPropertyGroup,
                            residenceId++);
                    otherCondominiums.add(otherCondominium);
                    residenceReference = String.format("bostadsrattsId: %d", otherCondominium.getCondominiumId());
                    break;
                }
                case ApplicationFieldOptionValues.HOUSE: {
                    OtherProperty otherProperty = OtherProperty
                            .createFromApplication(otherPropertyGroup, residenceId++);
                    otherProperties.add(otherProperty);
                    residenceReference = String.format("fastighetsId: %d", otherProperty.getPropertyId());
                    break;
                }
                default:

                    log.error(ProductExecutionLogger
                            .newBuilder().withMessage(String.format("No implementation for property type '%s'.", propertyType.get())));
                }

                Integer loanAmount = otherPropertyGroup.getFieldAsInteger(ApplicationFieldName.LOAN_AMOUNT);
                if (loanAmount > 0) {
                    ExistingLoan existingLoan = new ExistingLoan();
                    existingLoan.setConnectedToSamePropertyAsMortgageApplication(false);
                    existingLoan.setCurrentDebt(loanAmount);
                    existingLoan.setInterestRate(0.0001d); // Apparently, 0% interest rate is not allowed for loans.
                    existingLoan.setLender("Unknown");
                    existingLoan.setMonthlyAmortization(0);
                    existingLoan.setMonthlyInterestPayment(0);
                    existingLoan.setOtherInformation(residenceReference);
                    existingLoan.setShouldBeResolved(false);
                    existingLoan.setType(ExistingLoan.Type.fromPropertyTypeOption(propertyType.get()).getType());
                    existingLoans.add(existingLoan);
                }
            }

            borrower.setOtherCondominiums(otherCondominiums);
            borrower.setOtherProperties(otherProperties);
        }

        Preconditions.checkState(!borrowers.isEmpty(), "Cannot create a household without borrowers.");

        Household household = new Household();

        household.setAddress(Address.createFromApplication(applicantGroups.get(0)));
        household.setBorrowers(borrowers);
        household.setMonthlyAmortizationOfStudentLoan(monthlyAmortizationOfStudentLoan.intValue());
        household.setNumberOfChildrenWithChildAllowances(householdGroup
                .getFieldAsInteger(ApplicationFieldName.NUMBER_OF_CHILDREN_RECEIVING_CHILD_BENEFIT));
        household.setNumberOfChildSupportsToReceive(householdGroup
                .getFieldAsInteger(ApplicationFieldName.NUMBER_OF_CHILDREN_RECEIVING_ALIMONY));
        household.setNumberOfChildSupportsToPay(householdGroup
                .getFieldAsInteger(ApplicationFieldName.NUMBER_OF_CHILDREN_PAYING_ALIMONY));

        List<GenericApplicationFieldGroup> otherLoanFieldGroups = ApplicationUtils.getSubGroups(householdGroup,
                GenericApplicationFieldGroupNames.LOAN);

        if (!otherLoanFieldGroups.isEmpty()) {
            for (GenericApplicationFieldGroup otherLoanFieldGroup : otherLoanFieldGroups) {
                ExistingLoan existingLoan = new ExistingLoan();
                existingLoan.setConnectedToSamePropertyAsMortgageApplication(false);
                existingLoan.setCurrentDebt(otherLoanFieldGroup.getFieldAsInteger(ApplicationFieldName.AMOUNT));
                existingLoan.setInterestRate(0.0001d); // Apparently, 0% interest rate is not allowed for consumer loans.
                existingLoan.setLender(otherLoanFieldGroup.getField(ApplicationFieldName.LENDER));
                existingLoan.setMonthlyAmortization(0);
                existingLoan.setMonthlyInterestPayment(0);
                existingLoan.setShouldBeResolved(false);
                existingLoan.setType(ExistingLoan.Type.CONSUMER_LOAN.getType());
                existingLoans.add(existingLoan);
            }
        }

        household.setExistingLoans(existingLoans);

        return household;
    }
}
