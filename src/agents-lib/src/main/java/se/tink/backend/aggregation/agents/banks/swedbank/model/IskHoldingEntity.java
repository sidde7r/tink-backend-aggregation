package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Optional;
import se.tink.backend.system.rpc.Instrument;
import se.tink.backend.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IskHoldingEntity {
    private SettlementEntity settlement;
    private String holdingType;
    private String name;
    private String fundCode;
    private AmountEntity fundRate;
    private AmountEntity acquisitionValue;
    private String numberOfFundParts;
    private AmountEntity changeOfValue;
    private String changeOfValuePercent;
    private AmountEntity marketValue;

    public SettlementEntity getSettlement() {
        return settlement;
    }

    public void setSettlement(SettlementEntity settlement) {
        this.settlement = settlement;
    }

    public String getHoldingType() {
        return holdingType;
    }

    public void setHoldingType(String holdingType) {
        this.holdingType = holdingType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFundCode() {
        return fundCode;
    }

    public void setFundCode(String fundCode) {
        this.fundCode = fundCode;
    }

    public AmountEntity getFundRate() {
        return fundRate;
    }

    public void setFundRate(AmountEntity fundRate) {
        this.fundRate = fundRate;
    }

    public AmountEntity getAcquisitionValue() {
        return acquisitionValue;
    }

    public void setAcquisitionValue(AmountEntity acquisitionValue) {
        this.acquisitionValue = acquisitionValue;
    }

    public String getNumberOfFundParts() {
        return numberOfFundParts;
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

    public AmountEntity getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(AmountEntity marketValue) {
        this.marketValue = marketValue;
    }

    public Optional<Instrument> toInstrument(String isin) {
        double quantity = StringUtils.parseAmount(numberOfFundParts);
        if (numberOfFundParts == null || quantity == 0) {
            return Optional.empty();
        }

        Instrument instrument = new Instrument();

        instrument.setIsin(isin);
        instrument.setRawType(holdingType);
        instrument.setProfit(changeOfValue != null ? StringUtils.parseAmount(changeOfValuePercent) : null);
        instrument.setQuantity(quantity);
        double totalValue = StringUtils.parseAmount(marketValue.getAmount());
        instrument.setMarketValue(totalValue);
        instrument.setPrice(totalValue / quantity);
        instrument.setCurrency(marketValue.getCurrencyCode());
        instrument.setName(name);
        instrument.setAverageAcquisitionPrice(acquisitionValue != null ?
                StringUtils.parseAmount(acquisitionValue.getAmount()) / quantity : null);
        instrument.setUniqueIdentifier(fundCode + isin);
        instrument.setType(getType());

        return Optional.of(instrument);
    }

    private Instrument.Type getType() {
        if (holdingType == null) {
            return Instrument.Type.OTHER;
        }

        switch (holdingType.toLowerCase()) {
        case "fund":
            return Instrument.Type.FUND;
        default:
            return Instrument.Type.OTHER;
        }
    }
}
