package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities;

import java.math.BigDecimal;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.investment.entities.PortfolioEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.investment.rpc.InvestmentAccountResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule.PortfolioType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class FinancialMarketAccountEntity {

    private String accountId;
    private String account;
    private String profile;
    private String lastOperationDate;
    private String currency;
    private BigDecimal stockMarketBalance;
    private String stockMarketDate;
    private BigDecimal investmentFundBalance;
    private String investmentFundDate;
    private BigDecimal totalBalance;

    public Optional<InvestmentAccount> toTinkInvestmentAccount(
            InvestmentAccountResponse investmentAccountResponse) {

        String valueFund =
                investmentAccountResponse.getPortfolioValues().stream()
                        .findFirst()
                        .map(PortfolioEntity::getValueFund)
                        .get();

        return Optional.ofNullable(
                InvestmentAccount.nxBuilder()
                        .withPortfolios(
                                PortfolioModule.builder()
                                        .withType(PortfolioType.OTHER)
                                        .withUniqueIdentifier(valueFund)
                                        .withCashValue(totalBalance.doubleValue())
                                        .withTotalProfit(investmentFundBalance.doubleValue())
                                        .withTotalValue(totalBalance.doubleValue())
                                        .withInstruments()
                                        .build())
                        .withCashBalance(
                                ExactCurrencyAmount.of(totalBalance.doubleValue(), currency))
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(accountId)
                                        .withAccountNumber(account)
                                        .withAccountName(valueFund)
                                        .addIdentifier(
                                                AccountIdentifier.create(
                                                        AccountIdentifierType.OTHER,
                                                        account,
                                                        valueFund))
                                        .build())
                        .build());
    }

    public String getAccountId() {
        return accountId;
    }
}
