package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarConstants.Accounts;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class AccountEntity {
    private String accountNumber;
    private String balance;
    private String customName;

    @JsonIgnore
    public InvestmentAccount toTinkInvestmentAccount(PortfolioModule... portfolios) {
        return InvestmentAccount.nxBuilder()
                .withPortfolios(portfolios)
                .withCashBalance(
                        ExactCurrencyAmount.of(StringUtils.parseAmount(balance), Accounts.CURRENCY))
                .withId(getIdModule())
                .build();
    }

    private IdModule getIdModule() {
        return IdModule.builder()
                .withUniqueIdentifier(accountNumber)
                .withAccountNumber(accountNumber)
                .withAccountName(Strings.isNullOrEmpty(customName) ? accountNumber : customName)
                .addIdentifier(AccountIdentifier.create(AccountIdentifierType.SE, accountNumber))
                .build();
    }
}
