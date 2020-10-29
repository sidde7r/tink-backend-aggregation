package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class SecurityEntity {
    @JsonIgnore
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private String id;
    private String name;
    private String currency;
    private BigDecimal value;

    @JsonProperty("noOfSecurities")
    private BigDecimal quantity;

    private BigDecimal portfolioAllocation;
    private BigDecimal latestChangePercentage;
    private BigDecimal price;
    private BigDecimal unrealizedProfitLoss;
    private BigDecimal unrealizedProfitLossPct;
    private boolean fixedIncome;

    public Optional<Instrument> toTinkInstrument(ListSecurityDetailsResponse securityDetails) {
        if (securityDetails == null) {
            return Optional.empty();
        }

        Instrument instrument = new Instrument();

        instrument.setAverageAcquisitionPrice(
                value.add(unrealizedProfitLoss).divide(quantity, 4, RoundingMode.HALF_UP));
        instrument.setCurrency(currency);
        String isin = securityDetails.getIsin();
        instrument.setIsin(isin);
        instrument.setMarketValue(value.doubleValue());
        instrument.setName(name);
        instrument.setPrice(price.doubleValue());
        instrument.setProfit(unrealizedProfitLoss.doubleValue());
        instrument.setQuantity(quantity.doubleValue());
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
                logger.info(
                        String.format(
                                "danske bank - instrument info - security type name [%s] type [%d] securitySubTypeName [%s]",
                                securityTypeName,
                                securityType,
                                securityDetails.getSecuritySubTypeName()));
                return Instrument.Type.OTHER;
        }
    }
}
