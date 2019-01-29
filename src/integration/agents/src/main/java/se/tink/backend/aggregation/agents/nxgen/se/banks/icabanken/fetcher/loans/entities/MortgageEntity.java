package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.util.Maps;
import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.IcaBankenLoanParsingHelper;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;

@JsonObject
public class MortgageEntity {
    @JsonProperty("Deeds")
    private String deeds;
    @JsonProperty("MortgageDetails")
    private List<Map<String, String>> mortgageDetails;
    @JsonProperty("MortgageNumber")
    private String mortgageNumber;
    @JsonProperty("PresentDebt")
    private String presentDebt;

    @JsonIgnore
    private Map<String, String> transformedMortgageDetails;

    public void setMortgageDetails(List<Map<String, String>> mortgageDetails) {
        this.mortgageDetails = mortgageDetails;
        this.transformedMortgageDetails = buildLoanDetailsMap();
    }

    public String getDeeds() {
        return deeds;
    }

    public String getMortgageNumber() {
        return mortgageNumber;
    }

    public String getPresentDebt() {
        return presentDebt;
    }

    public List<Map<String, String>> getMortgageDetails() {
        return this.mortgageDetails;
    }

    @JsonIgnore
    private Map<String, String> buildLoanDetailsMap() {
        Map<String, String> map = Maps.newHashMap();

        for (Map<String, String> keyValuePair : mortgageDetails) {
            map.put(keyValuePair.get(IcaBankenConstants.IdTags.KEY_TAG).toLowerCase().trim(),
                    keyValuePair.get(IcaBankenConstants.IdTags.VALUE_TAG));
        }

        return map;
    }

    @JsonIgnore
    public LoanAccount toTinkLoan() {
        IcaBankenLoanParsingHelper loanParsingHelper = new IcaBankenLoanParsingHelper(transformedMortgageDetails);

        return LoanAccount.builder(mortgageNumber, loanParsingHelper.getBalance(presentDebt))
                .setAccountNumber(mortgageNumber)
                .setName(loanParsingHelper.getTypeOfLoan())
                .setBankIdentifier(mortgageNumber)
                .setHolderName(loanParsingHelper.getLoanHolderName())
                .setInterestRate(loanParsingHelper.getInterestRate())
                .setDetails(buildLoanDetails(loanParsingHelper))
                .build();
    }

    @JsonIgnore
    private LoanDetails buildLoanDetails(IcaBankenLoanParsingHelper loanParsingHelper) {
        return LoanDetails.builder(LoanDetails.Type.MORTGAGE)
                .setLoanNumber(mortgageNumber)
                .setNextDayOfTermsChange(loanParsingHelper.getNextDayOfTermsChange())
                .setNumMonthsBound(loanParsingHelper.getNumMonthsBound())
                .setInitialBalance(loanParsingHelper.getInitialBalance())
                .setAmortized(loanParsingHelper.getAmortized(presentDebt))
                .setApplicants(loanParsingHelper.getApplicantsList())
                .setSecurity(loanParsingHelper.getSecurity())
                .setCoApplicant(loanParsingHelper.hasCoApplicant())
                .build();
    }
}


