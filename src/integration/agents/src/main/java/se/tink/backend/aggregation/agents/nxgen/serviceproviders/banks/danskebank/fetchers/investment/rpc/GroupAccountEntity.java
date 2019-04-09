package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.amount.Amount;

@JsonObject
public class GroupAccountEntity {
    @JsonIgnore
    private static final AggregationLogger log = new AggregationLogger(GroupAccountEntity.class);

    private String name;

    @JsonProperty("value")
    private String accountIdentifier;

    private String type;

    @JsonProperty("displayValue")
    private String displayAccountIdentifier;

    public String getName() {
        return name;
    }

    public String getAccountIdentifier() {
        return accountIdentifier;
    }

    public String getType() {
        return type;
    }

    public String getDisplayAccountIdentifier() {
        return displayAccountIdentifier;
    }

    public InvestmentAccount toInvestmentAccount(String currency, List<Portfolio> portfolios) {
        return InvestmentAccount.builder(accountIdentifier)
                .setCashBalance(new Amount(currency, 0))
                .setAccountNumber(displayAccountIdentifier)
                .setName(name)
                .setPortfolios(portfolios)
                .build();
    }

    public Portfolio toTinkPortfolio(double marketValue) {
        Portfolio portfolio = new Portfolio();

        portfolio.setTotalValue(marketValue);
        portfolio.setRawType(type);
        portfolio.setType(getTinkPortfolioType());
        portfolio.setUniqueIdentifier(accountIdentifier);

        return portfolio;
    }

    private Portfolio.Type getTinkPortfolioType() {
        if (type == null) {
            return Portfolio.Type.OTHER;
        }

        switch (type.toLowerCase()) {
            case DanskeBankConstants.Investment.CUSTODY_ACCOUNT:
                return Portfolio.Type.DEPOT;
            default:
                log.info(
                        String.format(
                                "Danske Bank - portfolio info - portfolio name [%s] portfolio type [%s]",
                                name, type));
                return Portfolio.Type.OTHER;
        }
    }
}
