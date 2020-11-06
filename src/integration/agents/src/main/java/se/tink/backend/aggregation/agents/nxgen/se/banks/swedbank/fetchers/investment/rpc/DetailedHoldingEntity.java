package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdModule;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class DetailedHoldingEntity {
    @JsonIgnore private static final String EMPTY_STRING = "";

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

    public InstrumentModule toTinkInstrumentModule(String isin) {
        return InstrumentModule.builder()
                .withType(SwedbankSEConstants.INSTRUMENT_TYPE_MAP.translate(holdingType).get())
                .withId(
                        InstrumentIdModule.of(
                                isin, nameMarketPlace, name, getUniqueIdentifier(isin)))
                .withMarketPrice(0)
                .withMarketValue(getMarketValue().toTinkAmount().getDoubleValue())
                .withAverageAcquisitionPrice(getAcquisitionValue().toTinkAmount().getDoubleValue())
                .withCurrency(getMarketValue().getCurrencyCode())
                .withQuantity(AgentParsingUtils.parseAmount(numberOfFundParts))
                .withProfit(changeOfValue.toTinkAmount().getDoubleValue())
                .build();
    }

    public Optional<Instrument> toTinkFundInstrument(String isinCode) {
        if (Strings.isNullOrEmpty(isinCode) || numberOfFundParts == null) {
            if (Strings.isNullOrEmpty(isinCode)) {
                log.warn(SwedbankBaseConstants.LogTags.FUND_MISSING_ISIN.toString(), name);
            }
            return Optional.empty();
        }

        Instrument instrument = new Instrument();

        instrument.setAverageAcquisitionPriceFromAmount(
                Optional.ofNullable(acquisitionValue).map(AmountEntity::toTinkAmount).orElse(null));
        instrument.setCurrency(
                Optional.ofNullable(marketValue).map(AmountEntity::getCurrencyCode).orElse(null));
        instrument.setIsin(isinCode);
        instrument.setMarketValueFromAmount(
                Optional.ofNullable(marketValue).map(AmountEntity::toTinkAmount).orElse(null));
        instrument.setName(name);
        instrument.setPriceFromAmount(
                Optional.of(fundRate).map(AmountEntity::toTinkAmount).orElse(null));
        instrument.setProfitFromAmount(
                Optional.ofNullable(changeOfValue).map(AmountEntity::toTinkAmount).orElse(null));
        instrument.setQuantity(StringUtils.parseAmountEU(numberOfFundParts));
        instrument.setType(Instrument.Type.FUND);
        instrument.setUniqueIdentifier(
                isinCode + Optional.ofNullable(fundCode).orElse(EMPTY_STRING));

        return Optional.of(instrument);
    }

    public Optional<Instrument> toTinkInstrument(String rawType) {
        if (isin == null
                || nameMarketPlace == null
                || numberOrAmount == null
                || numberOrAmount.getNominalValue() == null
                || numberOrAmount.getNominalValue().getAmount() == null) {
            return Optional.empty();
        }

        Instrument instrument = new Instrument();

        instrument.setAverageAcquisitionPriceFromAmount(
                Optional.ofNullable(acquisitionPrice).map(AmountEntity::toTinkAmount).orElse(null));
        instrument.setCurrency(getInstrumentCurrency());
        instrument.setIsin(isin);
        instrument.setMarketValueFromAmount(
                Optional.ofNullable(marketValue).map(AmountEntity::toTinkAmount).orElse(null));
        instrument.setName(name);
        instrument.setPriceFromAmount(
                Optional.of(valuationPrice).map(AmountEntity::toTinkAmount).orElse(null));
        instrument.setProfitFromAmount(
                Optional.ofNullable(changeOfValue).map(AmountEntity::toTinkAmount).orElse(null));
        instrument.setQuantity(
                StringUtils.parseAmountEU(
                        Optional.ofNullable(numberOrAmount)
                                .map(NumberOrAmountEntity::getNominalValue)
                                .map(AmountEntity::getAmount)
                                .orElse(EMPTY_STRING)));
        instrument.setRawType(rawType);
        instrument.setType(getTinkType(rawType));
        instrument.setUniqueIdentifier(
                isin + Optional.ofNullable(nameMarketPlace).orElse(EMPTY_STRING));
        instrument.setMarketPlace(nameMarketPlace);

        return Optional.of(instrument);
    }

    private String getUniqueIdentifier(String isin) {
        return isin + Optional.ofNullable(fundCode).orElse(EMPTY_STRING);
    }

    private String getInstrumentCurrency() {
        String numberOrAmountCurrency =
                Optional.ofNullable(numberOrAmount)
                        .map(NumberOrAmountEntity::getNominalValue)
                        .map(AmountEntity::getCurrencyCode)
                        .orElse(null);

        if (numberOrAmountCurrency != null) {
            return numberOrAmountCurrency;
        }

        // Fallback to use acquisitionValue if the currency of numberOrAmount is not set.
        return Optional.ofNullable(acquisitionValue)
                .map(AmountEntity::getCurrencyCode)
                .orElse(null);
    }

    private static Instrument.Type getTinkType(String rawType) {
        if (Strings.isNullOrEmpty(rawType)) {
            return Instrument.Type.OTHER;
        }

        switch (rawType.toLowerCase()) {
            case "equity":
            case "equities":
                return Instrument.Type.STOCK;
            case "equityfund":
                return Instrument.Type.FUND;
                // Instrument types that don't match any of our instrument types, but setting it
                // explicitly to OTHER
                // so we don't log it as a warning.
            case "subscription_right":
            case "spax":
            case "etf":
            case "warrant":
            case "fixed_income":
            case "interestequity":
                return Instrument.Type.OTHER;
            default:
                log.warn("Unkown instrument type:[{}]", rawType);
                return Instrument.Type.OTHER;
        }
    }
}
