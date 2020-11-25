package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants.Account;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants.Market;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
@Slf4j
public class GroupAccountEntity {

    private String name;

    @JsonProperty("value")
    private String accountIdentifier;

    private String type;

    @JsonProperty("displayValue")
    private String displayAccountIdentifier;

    private List<CustodyAccountEntity> custodyAccounts;

    public InvestmentAccount toInvestmentAccount(
            String currency, List<Portfolio> portfolios, DanskeBankConfiguration configuration) {
        return InvestmentAccount.builder(
                        Market.SE_MARKET.equals(configuration.getMarketCode())
                                ? displayAccountIdentifier
                                : accountIdentifier)
                .setCashBalance(ExactCurrencyAmount.zero(currency))
                .setAccountNumber(getAccountNumber())
                .setName(getProperName(configuration))
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

    private String getProperName(DanskeBankConfiguration configuration) {
        if (Market.DK_MARKET.equals(configuration.getMarketCode())
                && Account.CUSTODY_ACCOUNT_TYPE.equals(type)
                && Account.EN_CUSTODY_ACCOUNT_NAME.equals(name)) {
            return Account.DA_CUSTODY_ACCOUNT_NAME;
        }
        return name;
    }

    private String getAccountNumber() {
        if (displayAccountIdentifier != null) {
            return displayAccountIdentifier;
        }
        return CollectionUtils.emptyIfNull(custodyAccounts).stream()
                .findFirst()
                .map(CustodyAccountEntity::getCustodyAccountId)
                .orElse(accountIdentifier);
    }

    public Portfolio toTinkPortfolio(
            ListSecuritiesResponse response, DanskeBankConfiguration configuration) {
        Portfolio portfolio = new Portfolio();
        portfolio.setTotalValue(response.getMarketValue().doubleValue());
        portfolio.setRawType(type);
        portfolio.setType(getTinkPortfolioType());
        portfolio.setUniqueIdentifier(
                Market.SE_MARKET.equals(configuration.getMarketCode())
                        ? displayAccountIdentifier
                        : accountIdentifier);
        portfolio.setTotalProfit(response.getPerformance().doubleValue());
        return portfolio;
    }

    private Portfolio.Type getTinkPortfolioType() {
        if (type != null
                && DanskeBankConstants.Investment.CUSTODY_ACCOUNT.equals(type.toLowerCase())) {
            return Portfolio.Type.DEPOT;
        }
        log.info(
                String.format(
                        "Danske Bank - portfolio info - portfolio name [%s] portfolio type [%s]",
                        name, type));
        return Portfolio.Type.OTHER;
    }
}
