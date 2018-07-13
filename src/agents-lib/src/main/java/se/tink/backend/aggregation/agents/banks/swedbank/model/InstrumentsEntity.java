package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Optional;
import se.tink.backend.system.rpc.Instrument;
import se.tink.backend.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InstrumentsEntity {
    private String name;
    private String shortName;
    private String tsid;
    private String instrumentType;
    private AmountEntity acquisitionValue;
    private String changePercent;
    private AmountEntity changeTodayAbsolute;
    private String changeTodayPercent;
    private String nameMarketPlace;
    private String isin;
    private AmountEntity marketValue;
    private Boolean isAddOrderPossible;
    private AmountEntity lastPaid;
    private AmountEntity valuationPrice;
    private AmountEntity acquisitionPrice;
    private AmountEntity changeAbsolute;
    private AmountEntity amountNominalBlocked;
    private TypedAmountEntity numberOrAmount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getTsid() {
        return tsid;
    }

    public void setTsid(String tsid) {
        this.tsid = tsid;
    }

    public String getInstrumentType() {
        return instrumentType;
    }

    public void setInstrumentType(String instrumentType) {
        this.instrumentType = instrumentType;
    }

    public AmountEntity getAcquisitionValue() {
        return acquisitionValue;
    }

    public void setAcquisitionValue(AmountEntity acquisitionValue) {
        this.acquisitionValue = acquisitionValue;
    }

    public String getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(String changePercent) {
        this.changePercent = changePercent;
    }

    public AmountEntity getChangeTodayAbsolute() {
        return changeTodayAbsolute;
    }

    public void setChangeTodayAbsolute(AmountEntity changeTodayAbsolute) {
        this.changeTodayAbsolute = changeTodayAbsolute;
    }

    public String getChangeTodayPercent() {
        return changeTodayPercent;
    }

    public void setChangeTodayPercent(String changeTodayPercent) {
        this.changeTodayPercent = changeTodayPercent;
    }

    public String getNameMarketPlace() {
        return nameMarketPlace;
    }

    public void setNameMarketPlace(String nameMarketPlace) {
        this.nameMarketPlace = nameMarketPlace;
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

    public Boolean getAddOrderPossible() {
        return isAddOrderPossible;
    }

    public void setAddOrderPossible(Boolean addOrderPossible) {
        isAddOrderPossible = addOrderPossible;
    }

    public AmountEntity getLastPaid() {
        return lastPaid;
    }

    public void setLastPaid(AmountEntity lastPaid) {
        this.lastPaid = lastPaid;
    }

    public AmountEntity getValuationPrice() {
        return valuationPrice;
    }

    public void setValuationPrice(AmountEntity valuationPrice) {
        this.valuationPrice = valuationPrice;
    }

    public AmountEntity getAcquisitionPrice() {
        return acquisitionPrice;
    }

    public void setAcquisitionPrice(AmountEntity acquisitionPrice) {
        this.acquisitionPrice = acquisitionPrice;
    }

    public AmountEntity getChangeAbsolute() {
        return changeAbsolute;
    }

    public void setChangeAbsolute(AmountEntity changeAbsolute) {
        this.changeAbsolute = changeAbsolute;
    }

    public AmountEntity getAmountNominalBlocked() {
        return amountNominalBlocked;
    }

    public void setAmountNominalBlocked(
            AmountEntity amountNominalBlocked) {
        this.amountNominalBlocked = amountNominalBlocked;
    }

    public TypedAmountEntity getNumberOrAmount() {
        return numberOrAmount;
    }

    public void setNumberOrAmount(TypedAmountEntity numberOrAmount) {
        this.numberOrAmount = numberOrAmount;
    }

    public Optional<Instrument> toInstrument() {
        if (getNumberOrAmount() == null || getNumberOrAmount().getNominalValue() == null ||
                getNumberOrAmount().getNominalValue().getAmount() == null) {
            return Optional.empty();
        }

        Instrument instrument = new Instrument();

        instrument.setAverageAcquisitionPrice(getAmountAsDouble(getAcquisitionPrice()));
        instrument.setCurrency(getNumberOrAmount().getNominalValue().getCurrencyCode());
        instrument.setIsin(getIsin());
        instrument.setMarketPlace(getNameMarketPlace());
        instrument.setMarketValue(getAmountAsDouble(getMarketValue()));
        instrument.setName(getName());
        instrument.setPrice(getAmountAsDouble(getValuationPrice()));
        instrument.setProfit(getAmountAsDouble(getChangeAbsolute()));
        instrument.setQuantity(getAmountAsDouble(getNumberOrAmount().getNominalValue()));
        instrument.setRawType(getInstrumentType());
        instrument.setType(getTinkInstrumentType());
        instrument.setUniqueIdentifier(getIsin() + getTsid());

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

    private Instrument.Type getTinkInstrumentType() {
        if (getInstrumentType() == null) {
            return Instrument.Type.OTHER;
        }

        switch (getInstrumentType().toLowerCase()) {
        case "equity":
            return Instrument.Type.STOCK;
        case "fund":
            return Instrument.Type.FUND;
        default:
            return Instrument.Type.OTHER;
        }
    }

}
