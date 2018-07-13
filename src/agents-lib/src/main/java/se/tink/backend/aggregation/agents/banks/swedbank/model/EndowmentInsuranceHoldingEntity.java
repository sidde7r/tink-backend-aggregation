package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.system.rpc.Instrument;
import se.tink.backend.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EndowmentInsuranceHoldingEntity {
    private String name;
    private String fundCode;
    private AmountEntity fundRate;
    private AmountEntity acquisitionValue;
    private String numberOfFundParts;
    private AmountEntity changeOfValue;
    private String changeOfValuePercent;
    private AmountEntity marketValue;
    private String holdingType;
    private SettlementEntity settlement;

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

    public Double getNumberOfFundParts() {
        return numberOfFundParts == null && numberOfFundParts.isEmpty() ?
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

    public AmountEntity getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(AmountEntity marketValue) {
        this.marketValue = marketValue;
    }

    public String getHoldingType() {
        return holdingType;
    }

    public void setHoldingType(String holdingType) {
        this.holdingType = holdingType;
    }

    public SettlementEntity getSettlement() {
        return settlement;
    }

    public void setSettlement(SettlementEntity settlement) {
        this.settlement = settlement;
    }

    public Optional<Instrument> toInstrument() {
        if (getHoldingType() != null && Objects.equals("cash", getHoldingType().toLowerCase())) {
            return Optional.of(toCashInstrument());
        }

        if (getNumberOfFundParts() == null || getNumberOfFundParts() == 0) {
            return Optional.empty();
        }

        return Optional.of(toFundInstrument());
    }

    private Instrument toFundInstrument() {
        Instrument instrument = new Instrument();

        instrument.setAverageAcquisitionPrice(
                getAcquisitionValue() != null && getAcquisitionValue().getAmount() != null ?
                        StringUtils.parseAmount(getAcquisitionValue().getAmount()) : null);
        instrument.setCurrency(getAcquisitionValue() != null ? getAcquisitionValue().getCurrencyCode() : null);
        Double marketValue = getMarketValue() != null && getMarketValue().getAmount() != null ?
                StringUtils.parseAmount(getMarketValue().getAmount()) : null;
        instrument.setMarketValue(marketValue);
        instrument.setName(getName());
        instrument.setPrice(marketValue != null && getNumberOfFundParts() != null ?
                marketValue / getNumberOfFundParts() : null);
        instrument.setProfit(getChangeOfValue() != null && getChangeOfValue().getAmount() != null ?
                StringUtils.parseAmount(getChangeOfValue().getAmount()) : null);
        instrument.setQuantity(getNumberOfFundParts());
        instrument.setRawType(getHoldingType());
        instrument.setType(getInstrumentType());

        return instrument;
    }

    private Instrument toCashInstrument() {
        return getSettlement().toInstrument(getHoldingType());
    }

    private Instrument.Type getInstrumentType() {
        if (getHoldingType() == null) {
            return Instrument.Type.OTHER;
        }

        switch (getHoldingType().toLowerCase()) {
        case "fund":
            return Instrument.Type.FUND;
        case "cash":
            // Intentional fall through
        default:
            return Instrument.Type.OTHER;
        }
    }
}
