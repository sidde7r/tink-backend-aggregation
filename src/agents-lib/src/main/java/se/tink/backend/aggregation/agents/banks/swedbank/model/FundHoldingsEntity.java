package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Optional;
import se.tink.backend.system.rpc.Instrument;
import se.tink.backend.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FundHoldingsEntity {
    private AmountEntity value;
    private AmountEntity rate;
    private String fundCode;
    private AmountEntity acquisitionValue;
    private String numberOfFundParts;
    private AmountEntity changeOfValue;
    private String changeOfValuePercent;
    private String fundName;
    private LinksEntity links;

    public AmountEntity getValue() {
        return value;
    }

    public void setValue(AmountEntity value) {
        this.value = value;
    }

    public AmountEntity getRate() {
        return rate;
    }

    public void setRate(AmountEntity rate) {
        this.rate = rate;
    }

    public String getFundCode() {
        return fundCode;
    }

    public void setFundCode(String fundCode) {
        this.fundCode = fundCode;
    }

    public AmountEntity getAcquisitionValue() {
        return acquisitionValue;
    }

    public void setAcquisitionValue(AmountEntity acquisitionValue) {
        this.acquisitionValue = acquisitionValue;
    }

    public Double getNumberOfFundParts() {
        return numberOfFundParts == null || numberOfFundParts.isEmpty() ?
                null : StringUtils.parseAmount(numberOfFundParts);
    }

    public void setNumberOfFundParts(String numberOfFundParts) {
        this.numberOfFundParts = numberOfFundParts;
    }

    public AmountEntity getChangeOfValue() {
        return changeOfValue;
    }

    public void setChangeOfValue(AmountEntity changeOfValue) {
        this.changeOfValue = changeOfValue;
    }

    public String getChangeOfValuePercent() {
        return changeOfValuePercent;
    }

    public void setChangeOfValuePercent(String changeOfValuePercent) {
        this.changeOfValuePercent = changeOfValuePercent;
    }

    public String getFundName() {
        return fundName;
    }

    public void setFundName(String fundName) {
        this.fundName = fundName;
    }

    public LinksEntity getLinks() {
        return links;
    }

    public void setLinks(LinksEntity links) {
        this.links = links;
    }

    public Optional<Instrument> toInstrument(String placementType, String isin) {
        if (getNumberOfFundParts() == null || getNumberOfFundParts() == 0) {
            return Optional.empty();
        }

        Instrument instrument = new Instrument();

        Double totalAcquisitionValue = getAmountAsDouble(getAcquisitionValue());
        instrument.setAverageAcquisitionPrice(totalAcquisitionValue != null ?
                totalAcquisitionValue / getNumberOfFundParts() : null);
        Double marketValue = getAmountAsDouble(getValue());
        instrument.setCurrency(getValue() != null ? getValue().getCurrencyCode() : null);
        instrument.setIsin(isin);
        instrument.setMarketValue(marketValue);
        instrument.setName(getFundName());
        instrument.setPrice(getAmountAsDouble(getRate()));
        instrument.setProfit(marketValue != null && totalAcquisitionValue != null ?
                marketValue - totalAcquisitionValue : null);
        instrument.setQuantity(getNumberOfFundParts());
        instrument.setRawType(placementType);
        instrument.setType(Instrument.Type.FUND);
        instrument.setUniqueIdentifier(isin + getFundCode());

        return Optional.of(instrument);
    }

    private Double getAmountAsDouble(AmountEntity amountEntity) {
        if (amountEntity == null) {
            return null;
        }

        if (amountEntity.getAmount() == null) {
            return null;
        }

        return StringUtils.parseAmount(amountEntity.getAmount());
    }
}
