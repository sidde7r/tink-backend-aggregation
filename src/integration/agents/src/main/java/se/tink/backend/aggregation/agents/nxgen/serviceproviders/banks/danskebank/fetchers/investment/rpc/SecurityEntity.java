package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;

@JsonObject
public class SecurityEntity {
    private static final AggregationLogger log = new AggregationLogger(SecurityEntity.class);
    private String id;
    private String name;
    private String currency;
    private double value;

    @JsonProperty("noOfSecurities")
    private double quantity;

    private double portfolioAllocation;
    private double latestChangePercentage;
    private double price;
    private double unrealizedProfitLoss;
    private double unrealizedProfitLossPct;
    private boolean fixedIncome;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCurrency() {
        return currency;
    }

    public double getValue() {
        return value;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getPortfolioAllocation() {
        return portfolioAllocation;
    }

    public double getLatestChangePercentage() {
        return latestChangePercentage;
    }

    public double getPrice() {
        return price;
    }

    public double getUnrealizedProfitLoss() {
        return unrealizedProfitLoss;
    }

    public double getUnrealizedProfitLossPct() {
        return unrealizedProfitLossPct;
    }

    public boolean isFixedIncome() {
        return fixedIncome;
    }

    public Optional<Instrument> toTinkInstrument(ListSecurityDetailsResponse securityDetails) {
        if (securityDetails == null) {
            return Optional.empty();
        }

        Instrument instrument = new Instrument();

        instrument.setAverageAcquisitionPrice((value + unrealizedProfitLoss) / quantity);
        instrument.setCurrency(currency);
        String isin = securityDetails.getIsin();
        instrument.setIsin(isin);
        instrument.setMarketValue(value);
        instrument.setName(name);
        instrument.setPrice(price);
        instrument.setProfit(unrealizedProfitLoss);
        instrument.setQuantity(quantity);
        instrument.setRawType(securityDetails.getSecurityTypeName());
        instrument.setType(getTinkInstrumentType(securityDetails));
        instrument.setUniqueIdentifier(isin + securityDetails.getCurrencyCode());

        return Optional.of(instrument);
    }

    private Instrument.Type getTinkInstrumentType(ListSecurityDetailsResponse securityDetails) {
        String securityTypeName = securityDetails.getSecurityTypeName();
        int securityType = securityDetails.getType();

        switch (securityType) {
            case DanskeBankConstants.Investment.INSTRUMENT_TYPE_STOCK:
                return Instrument.Type.STOCK;

            case DanskeBankConstants.Investment.INSTRUMENT_TYPE_FUND:
            case DanskeBankConstants.Investment.INSTRUMENT_TYPE_INVESTMENT_ASSOCIATION:
                return Instrument.Type.FUND;

            case DanskeBankConstants.Investment.INSTRUMENT_TYPE_BOND:
            case DanskeBankConstants.Investment.INSTRUMENT_TYPE_STRUCTURED_PRODUCT:
                return Instrument.Type.OTHER;

            default:
                log.info(
                        String.format(
                                "danske bank - instrument info - security type name [%s] type [%d] securitySubTypeName [%s]",
                                securityTypeName,
                                securityType,
                                securityDetails.getSecuritySubTypeName()));
                return Instrument.Type.OTHER;
        }
    }
}
