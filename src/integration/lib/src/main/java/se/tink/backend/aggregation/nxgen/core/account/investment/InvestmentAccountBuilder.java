package se.tink.backend.aggregation.nxgen.core.account.investment;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import se.tink.backend.aggregation.nxgen.core.account.AccountBuilder;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder.WithIdStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.investment.InvestmentBalanceStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.investment.InvestmentBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.investment.WithPortfoliosStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class InvestmentAccountBuilder extends AccountBuilder<InvestmentAccount, InvestmentBuildStep>
        implements WithPortfoliosStep<InvestmentBuildStep>,
                InvestmentBalanceStep<InvestmentBuildStep>,
                InvestmentBuildStep {

    private List<PortfolioModule> portfolioModules = Lists.newArrayList();
    private ExactCurrencyAmount exactBalance;

    @Override
    protected InvestmentBuildStep buildStep() {
        return this;
    }

    @Override
    public InvestmentAccount build() {
        return new InvestmentAccount(this);
    }

    private void addPortfolio(@Nonnull PortfolioModule portfolioModule) {
        Preconditions.checkNotNull(portfolioModule, "Portfolio must not be null.");
        this.portfolioModules.add(portfolioModule);
    }

    @Override
    public InvestmentBalanceStep<InvestmentBuildStep> withPortfolios(
            @Nonnull List<PortfolioModule> portfolioModules) {
        Preconditions.checkNotNull(portfolioModules, "Portfolios List must not be null.");
        Preconditions.checkArgument(portfolioModules.size() > 0, "Portfolios must not be empty.");
        portfolioModules.forEach(this::addPortfolio);
        return this;
    }

    @Override
    public InvestmentBalanceStep<InvestmentBuildStep> withPortfolios(
            @Nonnull PortfolioModule... portfolioModules) {
        Preconditions.checkNotNull(portfolioModules, "Portfolios Array must not be null.");
        return this.withPortfolios(Arrays.asList(portfolioModules));
    }

    @Override
    public InvestmentBalanceStep<InvestmentBuildStep> withoutPortfolios() {
        return this;
    }

    @Override
    public WithIdStep<InvestmentBuildStep> withZeroCashBalance(@Nonnull String currencyCode) {
        Preconditions.checkNotNull(currencyCode, "Currency Code must not be null.");
        return this.withCashBalance(ExactCurrencyAmount.zero(currencyCode));
    }

    @Override
    public WithIdStep<InvestmentBuildStep> withCashBalance(
            @Nonnull ExactCurrencyAmount cashBalance) {
        Preconditions.checkNotNull(cashBalance, "Cash Balance must not be null.");
        this.exactBalance =
                ExactCurrencyAmount.of(
                        calculateExactBalance(cashBalance.getExactValue()),
                        cashBalance.getCurrencyCode());
        return this;
    }

    private BigDecimal calculateExactBalance(BigDecimal cashBalance) {
        return portfolioModules.stream()
                .map(PortfolioModule::getTotalValue)
                .map(BigDecimal::valueOf)
                .reduce(cashBalance, BigDecimal::add);
    }

    List<PortfolioModule> getPortfolioModules() {
        return ImmutableList.copyOf(portfolioModules);
    }

    ExactCurrencyAmount getExactBalance() {
        return exactBalance;
    }
}
