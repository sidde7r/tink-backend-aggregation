package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.invoke.MethodHandles;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants.Market;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class GroupAccountEntity {
    @JsonIgnore
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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

    public InvestmentAccount toInvestmentAccount(
            String currency, List<Portfolio> portfolios, DanskeBankConfiguration configuration) {
        return InvestmentAccount.builder(
                        configuration.getMarketCode().equals(Market.SE_MARKET)
                                ? displayAccountIdentifier
                                : accountIdentifier)
                .setCashBalance(ExactCurrencyAmount.zero(currency))
                .setAccountNumber(displayAccountIdentifier)
                .setName(name)
                .setPortfolios(portfolios)
                .canExecuteExternalTransfer(AccountCapabilities.Answer.UNKNOWN)
                .canReceiveExternalTransfer(AccountCapabilities.Answer.UNKNOWN)
                .canWithdrawCash(AccountCapabilities.Answer.UNKNOWN)
                .canPlaceFunds(AccountCapabilities.Answer.UNKNOWN)
                .sourceInfo(
                        AccountSourceInfo.builder()
                                .bankProductName(name)
                                .bankProductCode(type)
                                .build())
                .build();
    }

    public Portfolio toTinkPortfolio(
            ListSecuritiesResponse response, DanskeBankConfiguration configuration) {
        Portfolio portfolio = new Portfolio();
        portfolio.setTotalValue(response.getMarketValue().doubleValue());
        portfolio.setRawType(type);
        portfolio.setType(getTinkPortfolioType());
        portfolio.setUniqueIdentifier(
                configuration.getMarketCode().equals(Market.SE_MARKET)
                        ? displayAccountIdentifier
                        : accountIdentifier);
        portfolio.setTotalProfit(response.getPerformance().doubleValue());
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
                logger.info(
                        String.format(
                                "Danske Bank - portfolio info - portfolio name [%s] portfolio type [%s]",
                                name, type));
                return Portfolio.Type.OTHER;
        }
    }
}
