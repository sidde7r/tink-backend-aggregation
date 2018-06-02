package se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.backend.core.enums.GenericApplicationFieldGroupNames;
import se.tink.backend.utils.ApplicationUtils;
import se.tink.libraries.application.ApplicationFieldOptionValues;
import se.tink.libraries.application.GenericApplicationFieldGroup;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExistingLoan {

    enum Type {
        VILLA("VILLA"),
        VACATION_HOUSE("FRITIDSHUS"),
        APARTMENT("BOSTADSRATT"),
        CONSUMER_LOAN("PRIVATLAN");

        private final String type;

        Type(String type) {
            this.type = type;
        }

        public static Type fromPropertyTypeOption(String option) {
            switch (option) {
            case ApplicationFieldOptionValues.HOUSE:
                return VILLA;
            case ApplicationFieldOptionValues.APARTMENT:
                return APARTMENT;
            case ApplicationFieldOptionValues.VACATION_HOUSE:
                return VACATION_HOUSE;
            default:
                throw new IllegalArgumentException(String.format("Option not mapped: %s", option));
            }
        }

        public String getType() {
            return type;
        }
    }

    // Unique id to be able to separate loans for different borrowers (required).
    @JsonProperty("lanId")
    private Integer loanId;

    // The current debt in SEK (required).
    @JsonProperty("aktuellSkuld")
    private Integer currentDebt;

    // The monthly amortization amount in SEK (required).
    @JsonProperty("amortering")
    private Integer monthlyAmortization;

    // The lender (not required).
    @JsonProperty("langivare")
    private String lender;

    // The monthly interest payment in SEK (required).
    @JsonProperty("rantaPerManad")
    private Integer monthlyInterestPayment;

    // The interest rate in % (required).
    @JsonProperty("rantesats")
    private Double interestRate;

    // If this loan should be resolved (required).
    @JsonProperty("skallLosas")
    private Boolean shouldBeResolved;

    // The type (required).
    @JsonProperty("typ")
    private String type;

    // The date when the loan conditions will change ('yyyy-mm-dd', not required).
    @JsonProperty("villkorsandringsdag")
    private String conditionChangeDate;

    // If this loan is connected to the same real property as the mortgage in the mortgage application (required).
    @JsonProperty("lanPaObjekt")
    private Boolean connectedToSamePropertyAsMortgageApplication;

    // Percentage of the loan that should be included when calculating total credit exposure for the applicant(s)
    @JsonProperty("procentAndelAvLan")
    private Integer percentageOfBurden = 100;

    // Other information to the bank which could be of interest (not required).
    @JsonProperty("ovrigt")
    private String otherInformation;

    public Integer getPercentageOfBurden() {
        return percentageOfBurden;
    }

    public void setPercentageOfBurden(Integer percentageOfBurden) {
        this.percentageOfBurden = percentageOfBurden;
    }

    public Integer getLoanId() {
        return loanId;
    }

    public void setLoanId(int loanId) {
        this.loanId = loanId;
    }

    public Integer getCurrentDebt() {
        return currentDebt;
    }

    public void setCurrentDebt(int currentDebt) {
        this.currentDebt = currentDebt;
    }

    public Integer getMonthlyAmortization() {
        return monthlyAmortization;
    }

    public void setMonthlyAmortization(int monthlyAmortization) {
        this.monthlyAmortization = monthlyAmortization;
    }

    public String getLender() {
        return lender;
    }

    public void setLender(String lender) {
        this.lender = lender;
    }

    public Integer getMonthlyInterestPayment() {
        return monthlyInterestPayment;
    }

    public void setMonthlyInterestPayment(int monthlyInterestPayment) {
        this.monthlyInterestPayment = monthlyInterestPayment;
    }

    public Double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(Double interestRate) {
        this.interestRate = interestRate;
    }

    public Boolean isShouldBeResolved() {
        return shouldBeResolved;
    }

    public void setShouldBeResolved(Boolean shouldBeResolved) {
        this.shouldBeResolved = shouldBeResolved;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConditionChangeDate() {
        return conditionChangeDate;
    }

    public void setConditionChangeDate(String conditionChangeDate) {
        this.conditionChangeDate = conditionChangeDate;
    }

    public boolean isConnectedToSamePropertyAsMortgageApplication() {
        return connectedToSamePropertyAsMortgageApplication;
    }

    public void setConnectedToSamePropertyAsMortgageApplication(boolean connectedToSamePropertyAsMortgageApplication) {
        this.connectedToSamePropertyAsMortgageApplication = connectedToSamePropertyAsMortgageApplication;
    }

    public String getOtherInformation() {
        return otherInformation;
    }

    public void setOtherInformation(String otherInformation) {
        this.otherInformation = otherInformation;
    }

    public static List<ExistingLoan> createFromApplication(Type propertyType,
            GenericApplicationFieldGroup currentMortgageGroup) {

        List<ExistingLoan> existingMortgage = Lists.newArrayList();

        String lender = currentMortgageGroup.getField(ApplicationFieldName.LENDER);

        for (GenericApplicationFieldGroup part : ApplicationUtils.getSubGroups(currentMortgageGroup,
                GenericApplicationFieldGroupNames.LOAN)) {

            String accountNumberAsString = part.getField(ApplicationFieldName.ACCOUNT_NUMBER);
            Integer loanId = null;

            if (!Strings.isNullOrEmpty(accountNumberAsString)) {
                loanId = accountNumberAsString.hashCode();
            }

            String otherInformation = accountNumberAsString;

            Optional<String> contractDate = part.tryGetField(ApplicationFieldName.CONTRACT_DATE);
            Optional<String> firstRecordDate = part.tryGetField(ApplicationFieldName.FIRST_RECORD_DATE);

            if (contractDate.isPresent()) {
                otherInformation += String.format(" Upptogs %s", contractDate.get());
            } else if (firstRecordDate.isPresent()) {
                otherInformation += String.format(" Registrerat i Tink %s", firstRecordDate.get());
            }

            Double amount = part.tryGetFieldAsDouble(ApplicationFieldName.AMOUNT).orElse(0d);
            Double interestRate = part.tryGetFieldAsDouble(ApplicationFieldName.INTEREST_RATE).orElse(0d);
            Double monthlyInterestCost = amount * interestRate / 12d;

            ExistingLoan existingLoan = new ExistingLoan();
            existingLoan.setConnectedToSamePropertyAsMortgageApplication(true);
            existingLoan.setCurrentDebt(amount.intValue());
            existingLoan.setInterestRate(interestRate * 100);
            existingLoan.setLender(lender);
            existingLoan.setLoanId(loanId);
            existingLoan.setMonthlyAmortization(0);
            existingLoan.setMonthlyInterestPayment(monthlyInterestCost.intValue());
            existingLoan.setOtherInformation(otherInformation);
            existingLoan.setShouldBeResolved(true);
            existingLoan.setType(propertyType.getType());

            existingMortgage.add(existingLoan);
        }

        return existingMortgage;
    }
}
