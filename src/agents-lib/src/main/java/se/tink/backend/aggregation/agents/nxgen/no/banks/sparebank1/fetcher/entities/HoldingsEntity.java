package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities;

import com.google.common.base.Strings;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1AmountUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.agents.models.Instrument;

@JsonObject
public class HoldingsEntity {
    private String productId;
    private String productName;
    private String isin;
    private String marketValueInteger;
    private String marketValueFraction;
    private String costPriceInteger;
    private String costPriceFraction;
    private String rateOfInvestCurrencyInteger;
    private String rateOfInvestCurrencyFraction;
    private String rateOfInvestPercentageInteger;
    private String rateOfInvestPercentageFraction;
    private String sharesInteger;
    private String sharesFraction;
    private Object periodicReportDTO; //Don't know what this is, only gotten empty object so far

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getIsin() {
        return isin;
    }

    public String getMarketValueInteger() {
        return marketValueInteger;
    }

    public String getMarketValueFraction() {
        return marketValueFraction;
    }

    public String getCostPriceInteger() {
        return costPriceInteger;
    }

    public String getCostPriceFraction() {
        return costPriceFraction;
    }

    public String getRateOfInvestCurrencyInteger() {
        return rateOfInvestCurrencyInteger;
    }

    public String getRateOfInvestCurrencyFraction() {
        return rateOfInvestCurrencyFraction;
    }

    public String getRateOfInvestPercentageInteger() {
        return rateOfInvestPercentageInteger;
    }

    public String getRateOfInvestPercentageFraction() {
        return rateOfInvestPercentageFraction;
    }

    public String getSharesInteger() {
        return sharesInteger;
    }

    public String getSharesFraction() {
        return sharesFraction;
    }

    public Object getPeriodicReportDTO() {
        return periodicReportDTO;
    }

    private Double getMarketValue() {
        if (Strings.isNullOrEmpty(marketValueInteger) || Strings.isNullOrEmpty(marketValueFraction)) {
            return 0.0;
        }

        return Sparebank1AmountUtils.constructDouble(marketValueInteger, marketValueFraction);
    }

    private Double getPrice() {
        if (Strings.isNullOrEmpty(costPriceInteger) || Strings.isNullOrEmpty(costPriceFraction)) {
            return 0.0;
        }

        return Sparebank1AmountUtils.constructDouble(costPriceInteger, costPriceFraction);
    }

    private Double getProfit() {
        // totalRateOfInvestCurrency is the diff between totalMarketValue and totalCostPrice
        if (Strings.isNullOrEmpty(rateOfInvestCurrencyInteger) ||
                Strings.isNullOrEmpty(rateOfInvestCurrencyFraction)) {
            return 0.0;
        }

        return Sparebank1AmountUtils.constructDouble(rateOfInvestCurrencyInteger, rateOfInvestCurrencyFraction);
    }

    private Double getQuantity() {
        if (Strings.isNullOrEmpty(sharesInteger) || Strings.isNullOrEmpty(sharesFraction)) {
            return 0.0;
        }

        return Sparebank1AmountUtils.constructDouble(sharesInteger, sharesFraction);
    }

    public Optional<Instrument> toInstrument() {
        if (getQuantity() == 0) {
            return Optional.empty();
        }

        Instrument instrument = new Instrument();

        instrument.setUniqueIdentifier(productId);
        instrument.setName(productName);
        instrument.setIsin(isin);
        instrument.setMarketValue(getMarketValue());
        instrument.setPrice(getPrice());
        instrument.setProfit(getProfit());
        instrument.setQuantity(getQuantity());
        instrument.setType(Instrument.Type.OTHER);

        return Optional.of(instrument);
    }
}
