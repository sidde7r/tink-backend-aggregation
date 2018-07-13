package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import java.util.Optional;
import se.tink.backend.system.rpc.Instrument;

public class FundAccountHoldingDetailResponse extends AbstractResponse {

    private String currency;
    private FundDetailEntity fundDetails;
    private String isin;
    private AmountEntity marketValue;
    private AmountEntity averageValueOfCost;

    public Optional<Instrument> toInstrument() {
        if (marketValue == null || marketValue.getAmount() == 0d) {
            return Optional.empty();
        }
        Instrument instrument = new Instrument();
        instrument.setCurrency(marketValue.getCurrency());
        instrument.setMarketValue(marketValue.getAmount());

        String isin = Optional.ofNullable(getIsin()).orElse(getFundDetails().getIsin());
        String marketPlace = getFundDetails().getExternalFundId();
        instrument.setIsin(isin);
        instrument.setMarketPlace(marketPlace);
        instrument.setUniqueIdentifier(isin + marketPlace);

        instrument.setName(getFundDetails().getName());
        instrument.setAverageAcquisitionPrice(
                Optional.ofNullable(averageValueOfCost)
                        .map(AmountEntity::getAmount)
                        .orElse(null)
        );
        instrument.setQuantity(calculateQuantity());
        instrument.setType(Instrument.Type.FUND);

        return Optional.of(instrument);
    }

    private Double calculateQuantity() {
        return getFundDetails().parseNavAmount()
                .filter(navAmount -> navAmount != 0d)
                .map(navAmount -> Math.floor(marketValue.getAmount() / navAmount))
                .orElse(null);
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public FundDetailEntity getFundDetails() {
        if (fundDetails == null) {
            fundDetails = new FundDetailEntity();
        }
        return fundDetails;
    }

    public void setFundDetails(FundDetailEntity fundDetails) {
        this.fundDetails = fundDetails;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public AmountEntity getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(AmountEntity marketValue) {
        this.marketValue = marketValue;
    }

    public AmountEntity getAverageValueOfCost() {
        return averageValueOfCost;
    }

    public void setAverageValueOfCost(
            AmountEntity averageValueOfCost) {
        this.averageValueOfCost = averageValueOfCost;
    }
}
