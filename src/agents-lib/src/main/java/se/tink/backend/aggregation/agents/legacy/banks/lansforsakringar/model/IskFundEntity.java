package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.models.Instrument;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IskFundEntity {
    @JsonIgnore
    private static final String CURRENCY = "SEK";
    private String isinCode;
    private boolean sellable;
    private String administrationFee;
    private double minimalReinvestmentAmount;
    private double minimumMonthlySavingsAmount;
    private double buyFeeInPercent;
    private double saleFeeInPercent;
    private Date valuationDate;
    private String ppmNumber;
    private FundDetailsEntity fund;

    public String getIsinCode() {
        return isinCode;
    }

    public void setIsinCode(String isinCode) {
        this.isinCode = isinCode;
    }

    public boolean isSellable() {
        return sellable;
    }

    public void setSellable(boolean sellable) {
        this.sellable = sellable;
    }

    public String getAdministrationFee() {
        return administrationFee;
    }

    public void setAdministrationFee(String administrationFee) {
        this.administrationFee = administrationFee;
    }

    public double getMinimalReinvestmentAmount() {
        return minimalReinvestmentAmount;
    }

    public void setMinimalReinvestmentAmount(double minimalReinvestmentAmount) {
        this.minimalReinvestmentAmount = minimalReinvestmentAmount;
    }

    public double getMinimumMonthlySavingsAmount() {
        return minimumMonthlySavingsAmount;
    }

    public void setMinimumMonthlySavingsAmount(double minimumMonthlySavingsAmount) {
        this.minimumMonthlySavingsAmount = minimumMonthlySavingsAmount;
    }

    public double getBuyFeeInPercent() {
        return buyFeeInPercent;
    }

    public void setBuyFeeInPercent(double buyFeeInPercent) {
        this.buyFeeInPercent = buyFeeInPercent;
    }

    public double getSaleFeeInPercent() {
        return saleFeeInPercent;
    }

    public void setSaleFeeInPercent(double saleFeeInPercent) {
        this.saleFeeInPercent = saleFeeInPercent;
    }

    public Date getValuationDate() {
        return valuationDate;
    }

    public void setValuationDate(Date valuationDate) {
        this.valuationDate = valuationDate;
    }

    public String getPpmNumber() {
        return ppmNumber;
    }

    public void setPpmNumber(String ppmNumber) {
        this.ppmNumber = ppmNumber;
    }

    public FundDetailsEntity getFund() {
        return fund;
    }

    public void setFund(FundDetailsEntity fund) {
        this.fund = fund;
    }

    public Optional<Instrument> toInstrument() {
        Instrument instrument = new Instrument();

        HoldingEntity holding = getFund().getHolding();

        if (holding.getNumberOfShares() == 0) {
            return Optional.empty();
        }

        instrument.setAverageAcquisitionPrice(holding.getPurchaseValue() / holding.getNumberOfShares());
        instrument.setCurrency(CURRENCY); // LF don't have a field for currency - if the add it change this
        instrument.setIsin(getIsinCode());
        instrument.setMarketPlace(getFund().getCompany());
        instrument.setMarketValue(holding.getTotalMarketValue());
        instrument.setName(getFund().getName());
        instrument.setProfit(holding.getDevelopment());
        instrument.setQuantity(holding.getNumberOfShares());
        instrument.setRawType(getFund().getType());
        instrument.setType(Instrument.Type.FUND);
        instrument.setUniqueIdentifier(getIsinCode() + getFund().getFundId());

        return Optional.of(instrument);
    }
}
