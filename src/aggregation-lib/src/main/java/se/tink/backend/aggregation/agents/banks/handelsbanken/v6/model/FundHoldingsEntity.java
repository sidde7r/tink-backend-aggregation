package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Optional;
import se.tink.backend.system.rpc.Instrument;
import se.tink.backend.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FundHoldingsEntity {
    private boolean shbFund;
    private String isin;
    private String currency;
    private String companyName;
    private String financialInstrumentName;
    private String fundName;
    private String externalFundId;
    private String externalFundMsg;
    private double monthlySavings;
    private boolean hasWithdrawal;
    private FundHoldingDetailEntity fundHoldingDetail;

    public boolean isShbFund() {
        return shbFund;
    }

    public void setShbFund(boolean shbFund) {
        this.shbFund = shbFund;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getFinancialInstrumentName() {
        return financialInstrumentName;
    }

    public void setFinancialInstrumentName(String financialInstrumentName) {
        this.financialInstrumentName = financialInstrumentName;
    }

    public String getFundName() {
        return fundName;
    }

    public void setFundName(String fundName) {
        this.fundName = fundName;
    }

    public String getExternalFundId() {
        return externalFundId;
    }

    public void setExternalFundId(String externalFundId) {
        this.externalFundId = externalFundId;
    }

    public String getExternalFundMsg() {
        return externalFundMsg;
    }

    public void setExternalFundMsg(String externalFundMsg) {
        this.externalFundMsg = externalFundMsg;
    }

    public double getMonthlySavings() {
        return monthlySavings;
    }

    public void setMonthlySavings(double monthlySavings) {
        this.monthlySavings = monthlySavings;
    }

    public boolean isHasWithdrawal() {
        return hasWithdrawal;
    }

    public void setHasWithdrawal(boolean hasWithdrawal) {
        this.hasWithdrawal = hasWithdrawal;
    }

    public FundHoldingDetailEntity getFundHoldingDetail() {
        return fundHoldingDetail;
    }

    public void setFundHoldingDetail(
            FundHoldingDetailEntity fundHoldingDetail) {
        this.fundHoldingDetail = fundHoldingDetail;
    }

    public Optional<Instrument> toInstrument() {
        Instrument instrument = new Instrument();

        if (getFundHoldingDetail().getHoldingUnits() == 0) {
            return Optional.empty();
        }

        String marketValue = getFundHoldingDetail().getMarketValueFormatted();
        String profit = getFundHoldingDetail().getTotalChange().getAmountFormatted();

        instrument.setAverageAcquisitionPrice(getFundHoldingDetail().getAverageValueOfCost() != null ?
                getFundHoldingDetail().getAverageValueOfCost().getAmount() : null);
        instrument.setCurrency(getCurrency());
        instrument.setIsin(getIsin());
        instrument.setMarketValue(marketValue == null || marketValue.isEmpty() ? null :
                StringUtils.parseAmount(marketValue));
        instrument.setName(getFundName());
        instrument.setPrice(getFundHoldingDetail().getPrice().getAmount());
        instrument.setProfit(profit == null || profit.isEmpty() ? null : StringUtils.parseAmount(profit));
        instrument.setQuantity(getFundHoldingDetail().getHoldingUnits());
        instrument.setRawType(getCompanyName());
        instrument.setType(Instrument.Type.FUND);
        instrument.setUniqueIdentifier(getIsin());

        return Optional.of(instrument);
    }
}
