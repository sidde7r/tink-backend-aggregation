package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.system.rpc.Portfolio;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class PensionDetailsResponse extends AbstractResponse {

    private List<PensionFundEntity> funds;
    private String extraInsuranceValue;
    private String fundHeader;
    private String fundHoldingHeader;
    private String fundInfotext;
    private String pensionName;
    private PensionSummaryEntity summary;

    public List<PensionFundEntity> getFunds() {
        return funds == null ? Collections.emptyList() : funds;
    }

    public void setFunds(List<PensionFundEntity> funds) {
        this.funds = funds;
    }

    public String getExtraInsuranceValue() {
        return extraInsuranceValue;
    }

    public void setExtraInsuranceValue(String extraInsuranceValue) {
        this.extraInsuranceValue = extraInsuranceValue;
    }

    public String getFundHeader() {
        return fundHeader;
    }

    public void setFundHeader(String fundHeader) {
        this.fundHeader = fundHeader;
    }

    public String getFundHoldingHeader() {
        return fundHoldingHeader;
    }

    public void setFundHoldingHeader(String fundHoldingHeader) {
        this.fundHoldingHeader = fundHoldingHeader;
    }

    public String getFundInfotext() {
        return fundInfotext;
    }

    public void setFundInfotext(String fundInfotext) {
        this.fundInfotext = fundInfotext;
    }

    public String getPensionName() {
        return pensionName;
    }

    public void setPensionName(String pensionName) {
        this.pensionName = pensionName;
    }

    public PensionSummaryEntity getSummary() {
        if (summary == null) {
            summary = new PensionSummaryEntity();
        }
        return summary;
    }

    public void setSummary(PensionSummaryEntity summary) {
        this.summary = summary;
    }

    public Account toAccount(CustodyAccountEntity custodyAccount) {
        Account account = new Account();

        account.setName(getPensionName());
        account.setAccountNumber(custodyAccount.getCustodyAccountNumber());
        account.setBankId(custodyAccount.getCustodyAccountNumber());
        account.setBalance(toValue(custodyAccount));
        account.setType(AccountTypes.INVESTMENT);
        return account;
    }

    public Portfolio toPortfolio(CustodyAccountEntity custodyAccount) {
        Portfolio portfolio = new Portfolio();

        portfolio.setUniqueIdentifier(custodyAccount.getCustodyAccountNumber());
        portfolio.setType(Portfolio.Type.PENSION);
        double totalValue = toValue(custodyAccount);
        portfolio.setTotalValue(totalValue);
        portfolio.setTotalProfit(totalValue - getSummary().toPaymentsMade());
        return portfolio;
    }

    private double toValue(CustodyAccountEntity custodyAccount) {
        return custodyAccount.getMarketValue().getAmount();
    }

    @Override
    public String toString() {
        return SerializationUtils.serializeToString(this);
    }
}
