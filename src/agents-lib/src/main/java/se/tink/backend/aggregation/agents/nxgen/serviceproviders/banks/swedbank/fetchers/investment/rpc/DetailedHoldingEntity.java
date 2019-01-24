package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.investment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class DetailedHoldingEntity {
    @JsonIgnore
    private static final String EMPTY_STRING = "";
    @JsonIgnore
    private static final Logger log = LoggerFactory.getLogger(DetailedHoldingEntity.class);

    private String name;
    private String fundCode;
    private boolean fundguide;
    private AmountEntity fundRate;
    private AmountEntity acquisitionValue;
    private String numberOfFundParts;
    private AmountEntity changeOfValue;
    private String changeOfValuePercent;
    private AmountEntity marketValue;
    private String holdingType;
    private LinksEntity links;
    private String shortName;
    private String tsid;
    private String instrumentType;
    private AmountEntity changeTodayAbsolute;
    private String changeTodayPercent;
    private String nameMarketPlace;
    private String isin;
    private boolean isAddOrderPossible;
    private NumberOrAmountEntity numberOrAmount;
    private TypedAmountEntity lastPaid;
    private TypedAmountEntity valuationPrice;
    private TypedAmountEntity acquisitionPrice;
    private TypedAmountEntity amountNominalBlocked;

    public String getName() {
        return name;
    }

    public String getFundCode() {
        return fundCode;
    }

    public boolean isFundguide() {
        return fundguide;
    }

    public AmountEntity getFundRate() {
        return fundRate;
    }

    public AmountEntity getAcquisitionValue() {
        return acquisitionValue;
    }

    public String getNumberOfFundParts() {
        return numberOfFundParts;
    }

    public AmountEntity getChangeOfValue() {
        return changeOfValue;
    }

    public String getChangeOfValuePercent() {
        return changeOfValuePercent;
    }

    public AmountEntity getMarketValue() {
        return marketValue;
    }

    public String getHoldingType() {
        return holdingType;
    }

    public LinksEntity getLinks() {
        return links;
    }

    public String getShortName() {
        return shortName;
    }

    public String getTsid() {
        return tsid;
    }

    public String getInstrumentType() {
        return instrumentType;
    }

    public AmountEntity getChangeTodayAbsolute() {
        return changeTodayAbsolute;
    }

    public String getChangeTodayPercent() {
        return changeTodayPercent;
    }

    public String getNameMarketPlace() {
        return nameMarketPlace;
    }

    public String getIsin() {
        return isin;
    }

    public boolean isAddOrderPossible() {
        return isAddOrderPossible;
    }

    public NumberOrAmountEntity getNumberOrAmount() {
        return numberOrAmount;
    }

    public TypedAmountEntity getLastPaid() {
        return lastPaid;
    }

    public TypedAmountEntity getValuationPrice() {
        return valuationPrice;
    }

    public TypedAmountEntity getAcquisitionPrice() {
        return acquisitionPrice;
    }

    public TypedAmountEntity getAmountNominalBlocked() {
        return amountNominalBlocked;
    }

    public Optional<Instrument> toTinkFundInstrument(String isinCode) {
        if (isinCode == null || numberOfFundParts == null) {
            return Optional.empty();
        }

        Instrument instrument = new Instrument();

        instrument.setAverageAcquisitionPriceFromAmount(
                Optional.ofNullable(acquisitionValue)
                        .map(AmountEntity::toAmountModel)
                        .orElse(null));
        instrument.setCurrency(
                Optional.ofNullable(marketValue)
                        .map(AmountEntity::getCurrencyCode)
                        .orElse(null));
        instrument.setIsin(isinCode);
        instrument.setMarketValueFromAmount(
                Optional.ofNullable(marketValue)
                        .map(AmountEntity::toAmountModel)
                        .orElse(null));
        instrument.setName(name);
        instrument.setPriceFromAmount(
                Optional.of(fundRate)
                        .map(AmountEntity::toAmountModel)
                        .orElse(null));
        instrument.setProfitFromAmount(
                Optional.ofNullable(changeOfValue)
                        .map(AmountEntity::toAmountModel)
                        .orElse(null));
        instrument.setQuantity(StringUtils.parseAmountEU(numberOfFundParts));
        instrument.setType(Instrument.Type.FUND);
        instrument.setUniqueIdentifier(isinCode + Optional.ofNullable(fundCode).orElse(EMPTY_STRING));

        return Optional.of(instrument);
    }

    public Optional<Instrument> toTinkInstrument(String rawType) {
        if (isin == null || nameMarketPlace == null || numberOrAmount == null ||
                numberOrAmount.getNominalValue() == null || numberOrAmount.getNominalValue().getAmount() == null) {
            return Optional.empty();
        }

        Instrument instrument = new Instrument();

        instrument.setAverageAcquisitionPriceFromAmount(
                Optional.ofNullable(acquisitionPrice)
                        .map(AmountEntity::toAmountModel)
                        .orElse(null));
        instrument.setCurrency(
                Optional.ofNullable(numberOrAmount)
                        .map(NumberOrAmountEntity::getNominalValue)
                        .map(AmountEntity::getCurrencyCode)
                        .orElse(null));
        instrument.setIsin(isin);
        instrument.setMarketValueFromAmount(
                Optional.ofNullable(marketValue)
                        .map(AmountEntity::toAmountModel)
                        .orElse(null));
        instrument.setName(name);
        instrument.setPriceFromAmount(
                Optional.of(valuationPrice)
                        .map(AmountEntity::toAmountModel)
                        .orElse(null));
        instrument.setProfitFromAmount(
                Optional.ofNullable(changeOfValue)
                        .map(AmountEntity::toAmountModel)
                        .orElse(null));
        instrument.setQuantity(StringUtils.parseAmountEU(
                Optional.ofNullable(numberOrAmount)
                        .map(NumberOrAmountEntity::getNominalValue)
                        .map(AmountEntity::getAmount)
                        .orElse(EMPTY_STRING)));
        instrument.setRawType(rawType);
        instrument.setType(getTinkType(rawType));
        instrument.setUniqueIdentifier(isin + Optional.ofNullable(nameMarketPlace).orElse(EMPTY_STRING));

        return Optional.of(instrument);
    }

    private static Instrument.Type getTinkType(String rawType) {
        if (Strings.isNullOrEmpty(rawType)) {
            return Instrument.Type.OTHER;
        }

        switch (rawType.toLowerCase()) {
        case "equity":
            return Instrument.Type.STOCK;
        case "equityfund":
            return Instrument.Type.FUND;
        default:
            log.warn("Unkown instrument type:[{}]", rawType);
            return Instrument.Type.OTHER;
        }
    }
}
