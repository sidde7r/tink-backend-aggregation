package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HoldingsResponse {
    private String errorCode;
    private List<FundAccountsEntity> fundAccounts;
    private List<SavingsAccountsEntity> savingsAccounts;
    private List<EquityTradersEntity> equityTraders;
    private List<InvestmentSavingsEntity> investmentSavings;
    private List<EndowmentInsuranceEntity> endowmentInsurances;

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public List<FundAccountsEntity> getFundAccounts() {
        return fundAccounts;
    }

    public void setFundAccounts(
            List<FundAccountsEntity> fundAccounts) {
        this.fundAccounts = fundAccounts;
    }

    public List<SavingsAccountsEntity> getSavingsAccounts() {
        return savingsAccounts;
    }

    public void setSavingsAccounts(
            List<SavingsAccountsEntity> savingsAccounts) {
        this.savingsAccounts = savingsAccounts;
    }

    public List<EquityTradersEntity> getEquityTraders() {
        return equityTraders;
    }

    public void setEquityTraders(List<EquityTradersEntity> equityTraders) {
        this.equityTraders = equityTraders;
    }

    public List<InvestmentSavingsEntity> getInvestmentSavings() {
        return investmentSavings;
    }

    public void setInvestmentSavings(List<InvestmentSavingsEntity> investmentSavings) {
        this.investmentSavings = investmentSavings;
    }

    public List<EndowmentInsuranceEntity> getEndowmentInsurances() {
        return endowmentInsurances;
    }

    public void setEndowmentInsurances(List<EndowmentInsuranceEntity> endowmentInsurances) {
        this.endowmentInsurances = endowmentInsurances;
    }
}
