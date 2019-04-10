package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import java.util.regex.Pattern;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapDeserializer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.NordeaV20Constants.Investments;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class CustodyAccount {
    private static final AggregationLogger LOGGER = new AggregationLogger(CustodyAccount.class);
    private static Pattern ALLOWED_BANKID_PATTERN =
            Pattern.compile(
                    "[\\w]+:.*"); // ASDF:[anything] just check we have the initial type string

    @JsonProperty("custodyAccountId")
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String accountId;

    @JsonProperty("displayName")
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String name;

    @JsonProperty("displayAccountNumber")
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String accountNumber;

    @JsonProperty("baseCurrency")
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String currency;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String classification;

    @JsonProperty("profitLoss")
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String profit;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String marketValue;

    private List<HoldingsEntity> holdings;

    public String getAccountId() {
        return accountId;
    }

    public String getName() {
        return name;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getCurrency() {
        return currency;
    }

    public Double getMarketValue() {
        return marketValue == null || marketValue.isEmpty()
                ? null
                : StringUtils.parseAmount(marketValue);
    }

    public String getClassification() {
        return classification;
    }

    public Double getProfit() {
        return profit == null || profit.isEmpty() ? null : StringUtils.parseAmount(profit);
    }

    public boolean hasValidBankId() {
        String bankId = getAccountId();

        // Example formats of custody accounts are FONDA:01409805511 and ASBS:270111.1
        return bankId != null && ALLOWED_BANKID_PATTERN.matcher(bankId).matches();
    }

    public List<HoldingsEntity> getHoldings() {
        return holdings;
    }

    public Portfolio.Type getPortfolioType(Credentials credentials) {
        String[] accountIdArray = getAccountId().split(":");
        if (accountIdArray.length != 2) {
            throw new IllegalStateException(
                    "This should not happen since we've check the bank id pattern");
        }

        switch (accountIdArray[0].toLowerCase()) {
            case Investments.PortfolioTypes.FOND:
                return Portfolio.Type.DEPOT;
            case Investments.PortfolioTypes.ISK:
                return Portfolio.Type.ISK;
            case Investments.PortfolioTypes.NLP:
            case Investments.PortfolioTypes.IPS:
                return Portfolio.Type.PENSION;
            case Investments.PortfolioTypes.ASBS:
                return Portfolio.Type.DEPOT;
            case Investments.PortfolioTypes.AKTIV:
                return Portfolio.Type.OTHER;
            case Investments.PortfolioTypes.BMS:
                return bmsUfoPortfolioToTinkType();
            case Investments.PortfolioTypes.UFO:
                return bmsUfoPortfolioToTinkType();
            default:
                LOGGER.info(
                        String.format(
                                "Unknown account type '%s' for account.name '%s'.",
                                accountId, name));
                return Portfolio.Type.OTHER;
        }
    }

    private Portfolio.Type bmsUfoPortfolioToTinkType() {
        String classification = getClassification();
        if (Investments.PortfolioClassification.PENSION.equalsIgnoreCase(classification)) {
            return Portfolio.Type.PENSION;
        }

        return Portfolio.Type.DEPOT;
    }
}
