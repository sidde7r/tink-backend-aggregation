package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Instrument.Type;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SecurityEntity {
    private String positioningId;
    private String enviroment;
    private CurrencyEntity totalAmountMarketCurrency;
    private CurrencyEntity marketCurrency;
    private String mic;
    private BankEntity marketerBank;
    private double availableTitles;
    private AmountEntity availableBalance;
    private String seoOriginated;
    private String evaluateHourTime;
    private TypeEntity currency;
    private String evaluateDate;
    private ProductTypeEntity productType;
    private String ricCode;
    private String counterPart;
    private String marketRic;
    private String internalMarket;
    private String batch;
    private AmountEntity availableBalanceMarketCurrency;
    private AmountEntity totalAmount;
    private EvaluateTypeEntity evaluateType;
    private String marketName;
    private SharesTypeEntity sharesType;
    private String name;
    private OfferIdEntity offerId;
    private TypeSecurityEntity typeSecurities;
    private double totalTitles;
    private String isin;

    public String getPositioningId() {
        return positioningId;
    }

    public String getEnviroment() {
        return enviroment;
    }

    public CurrencyEntity getTotalAmountMarketCurrency() {
        return totalAmountMarketCurrency;
    }

    public CurrencyEntity getMarketCurrency() {
        return marketCurrency;
    }

    public String getMic() {
        return mic;
    }

    public BankEntity getMarketerBank() {
        return marketerBank;
    }

    public double getAvailableTitles() {
        return availableTitles;
    }

    public AmountEntity getAvailableBalance() {
        return availableBalance;
    }

    public String getSeoOriginated() {
        return seoOriginated;
    }

    public String getEvaluateHourTime() {
        return evaluateHourTime;
    }

    public TypeEntity getCurrency() {
        return currency;
    }

    public String getEvaluateDate() {
        return evaluateDate;
    }

    public ProductTypeEntity getProductType() {
        return productType;
    }

    public String getRicCode() {
        return ricCode;
    }

    public String getCounterPart() {
        return counterPart;
    }

    public String getMarketRic() {
        return marketRic;
    }

    public String getInternalMarket() {
        return internalMarket;
    }

    public String getBatch() {
        return batch;
    }

    public AmountEntity getAvailableBalanceMarketCurrency() {
        return availableBalanceMarketCurrency;
    }

    public AmountEntity getTotalAmount() {
        return totalAmount;
    }

    public EvaluateTypeEntity getEvaluateType() {
        return evaluateType;
    }

    public String getMarketName() {
        return marketName;
    }

    public SharesTypeEntity getSharesType() {
        return sharesType;
    }

    public String getName() {
        return name;
    }

    public OfferIdEntity getOfferId() {
        return offerId;
    }

    public TypeSecurityEntity getTypeSecurities() {
        return typeSecurities;
    }

    public double getTotalTitles() {
        return totalTitles;
    }

    public String getIsin() {
        return isin;
    }

    public Instrument toTinkInstrument() {
        Instrument instrument = new Instrument();
        instrument.setUniqueIdentifier(marketName + isin);
        instrument.setName(name);
        instrument.setQuantity(totalTitles);
        instrument.setType(Type.STOCK);
        instrument.setPrice(getPrice());
        instrument.setMarketValue(totalAmount.getAmountAsDouble());
        instrument.setCurrency(currency.getId());
        instrument.setIsin(isin);
        instrument.setMarketPlace(marketName);
        instrument.setRawType(typeSecurities.getId());
        // instrument.setProfit(totalProfit);
        // instrument.setAverageAcquisitionPrice(aap);
        return instrument;
    }

    @JsonIgnore
    private Double getPrice() {
        return new BigDecimal(totalAmount.getAmountAsDouble() / totalTitles)
                .setScale(2, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }
}
