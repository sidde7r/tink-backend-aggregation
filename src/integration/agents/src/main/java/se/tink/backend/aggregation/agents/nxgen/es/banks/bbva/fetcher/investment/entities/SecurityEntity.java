package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Date;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.CurrencyEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.TypeEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc.SecurityProfitabilityResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc.BbvaErrorResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

@JsonObject
public class SecurityEntity {
    private String positioningId;
    private String ricCode;
    private String marketRic;
    private String internalMarket;
    private String mic;
    private String enviroment;
    private String isin;
    private TypeEntity typeSecurities;
    private String name;
    private String marketName;

    @JsonProperty("totalTitles")
    private double quantity;

    private AmountEntity totalAmount;
    private double availableTitles;
    private AmountEntity availableBalance;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date evaluateDate;

    private String evaluateHourTime;
    private TypeEntity evaluateType;
    private String batch;
    private String seoOriginated;
    private TypeEntity offerId;
    private TypeEntity sharesType;
    private TypeEntity productType;
    private AmountEntity totalAmountMarketCurrency;
    private AmountEntity availableBalanceMarketCurrency;
    private CurrencyEntity marketCurrency;
    private String counterPart;
    private Object marketerBank;
    private CurrencyEntity currency;

    @JsonObject
    public Instrument toTinkInstrument(
            BbvaApiClient apiClient,
            Instrument.Type instrumentType,
            String portfolioId,
            String securityCode) {
        double marketValue = totalAmount.toTinkAmount().getDoubleValue();

        Instrument instrument = new Instrument();
        instrument.setUniqueIdentifier(marketName + isin);
        instrument.setName(name);
        instrument.setQuantity(quantity);
        instrument.setType(instrumentType);
        instrument.setPrice(getPrice());
        instrument.setMarketValue(marketValue);
        instrument.setCurrency(currency.getId());
        instrument.setIsin(isin);
        instrument.setMarketPlace(marketName);
        instrument.setRawType(typeSecurities.getId());

        Double totalProfit = getTotalProfit(apiClient, portfolioId, securityCode);
        if (totalProfit != null) {
            instrument.setProfit(totalProfit);
            instrument.setAverageAcquisitionPrice(
                    getAverageAcquisitionPrice(marketValue - totalProfit));
        }

        return instrument;
    }

    @JsonIgnore
    private Double getPrice() {
        return new BigDecimal(totalAmount.getAmountAsDouble() / quantity)
                .setScale(2, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }

    @JsonIgnore
    private Double getAverageAcquisitionPrice(double acquisitionAmount) {
        return new BigDecimal(acquisitionAmount / quantity)
                .setScale(2, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }

    @JsonIgnore
    private Double getTotalProfit(
            BbvaApiClient apiClient, String portfolioId, String securityCode) {
        try {
            SecurityProfitabilityResponse profitabilityResponse =
                    apiClient.fetchSecurityProfitability(portfolioId, securityCode);
            return profitabilityResponse.getTotalProfit();
        } catch (HttpResponseException exception) {
            HttpResponse response = exception.getResponse();
            if (response.getStatus() == HttpStatus.SC_CONFLICT
                    && response.getBody(BbvaErrorResponse.class).isContractNotOperableError()) {
                return null;
            }
            throw exception;
        }
    }

    public String getRicCode() {
        return ricCode;
    }

    // not able to define - could be for other types of securities
    // `shares` is null - cannot define it!
    // `availableShares` is null - cannot define it!
    // `subtype` is null - cannot define it!
    // `availableBalanceLocalCurrency` is null - cannot define it!
    // `self` is null - cannot define it!
    // `id` is null - cannot define it!
    // `country` is null - cannot define it!
    // `bank` is null - cannot define it!
    // `branch` is null - cannot define it!
    // `formats` is null - cannot define it!
    // `parentContract` is null - cannot define it!
    // `joinType` is null - cannot define it!
    // `userCustomization` is null - cannot define it!
    // `product` is null - cannot define it!
    // `participants` is null - cannot define it!
    // `sublevel` is null - cannot define it!
    // `relatedContracts` is null - cannot define it!
}
