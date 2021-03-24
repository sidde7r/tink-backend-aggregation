package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.investment.entities;

import static se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants.CURRENCY;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants.Investments;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;

@JsonObject
public class InvestmentAccountEntity {
    @JsonProperty private List<PortfolioEntity> portfolios;
    @JsonProperty private BigDecimal totalChange;
    @JsonProperty private BigDecimal totalChangePercent;
    @JsonProperty private BigDecimal totalMarketValue;
    @JsonProperty private BigDecimal totalPurchaseValue;

    @JsonIgnore
    public List<PortfolioEntity> getPortfolios() {
        return Optional.ofNullable(portfolios).orElse(Collections.emptyList());
    }

    @JsonIgnore
    public InvestmentAccount toTinkInvestmentAccount(
            List<PortfolioModule> portfolioModule, String customerUserId) {
        return InvestmentAccount.nxBuilder()
                .withPortfolios(portfolioModule)
                .withZeroCashBalance(CURRENCY)
                .withId(getInvestmentIdModule(customerUserId))
                .build();
    }

    @JsonIgnore
    private IdModule getInvestmentIdModule(String customerUserId) {
        return IdModule.builder()
                .withUniqueIdentifier(Investments.ACCOUNT_ID_PREFIX + customerUserId)
                .withAccountNumber(Investments.ACCOUNT_ID_PREFIX + customerUserId)
                .withAccountName(getAccountName())
                .addIdentifier(AccountIdentifier.create(AccountIdentifierType.TINK, customerUserId))
                .build();
    }

    @JsonIgnore
    private String getAccountName() {
        final PortfolioEntity portfolio = getPortfolios().get(0);
        return String.format(
                "%s (%s) %s",
                portfolio.getPortfolioName(), portfolio.getPortfolioId(), portfolio.getOwnerName());
    }
}
