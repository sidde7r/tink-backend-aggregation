package se.tink.backend.aggregation.nxgen.core.account.investment;

import com.google.common.collect.ImmutableList;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class InvestmentAccount extends Account {
    public static final ImmutableList<AccountTypes> ALLOWED_ACCOUNT_TYPES =
            ImmutableList.<AccountTypes>builder()
                    .add(AccountTypes.INVESTMENT)
                    .add(AccountTypes.PENSION)
                    .build();

    private List<Portfolio> portfolios;

    private InvestmentAccount(
            Builder<InvestmentAccount, DefaultInvestmentAccountsBuilder> builder) {
        super(builder);
        this.portfolios = builder.getPortfolios();
    }

    public static Builder<InvestmentAccount, DefaultInvestmentAccountsBuilder> builder(
            String uniqueIdentifier) {
        return new DefaultInvestmentAccountsBuilder(uniqueIdentifier);
    }

    /**
     * @deprecated Use {@link #builder(String)} and {@link Builder#setCashBalance(Amount)} instead
     */
    public static Builder<InvestmentAccount, DefaultInvestmentAccountsBuilder> builder(
            String uniqueIdentifier, Amount balance) {
        return builder(uniqueIdentifier).setBalance(balance);
    }

    public List<Portfolio> getPortfolios() {
        return Optional.ofNullable(this.portfolios)
                .<List<Portfolio>>map(p -> ImmutableList.copyOf(p))
                .orElseGet(Collections::emptyList);
    }

    @Override
    public AccountTypes getType() {
        return AccountTypes.INVESTMENT;
    }

    public abstract static class Builder<
                    A extends InvestmentAccount, T extends InvestmentAccount.Builder<A, T>>
            extends Account.Builder<InvestmentAccount, Builder<A, T>> {
        private List<Portfolio> portfolios;
        private ExactCurrencyAmount cashBalance = null;

        public Builder(String uniqueIdentifier) {
            super(uniqueIdentifier);
        }

        public List<Portfolio> getPortfolios() {
            return this.portfolios != null ? this.portfolios : Collections.emptyList();
        }

        public Builder<A, T> setPortfolios(List<Portfolio> portfolios) {
            this.portfolios = portfolios;
            return self();
        }

        @Deprecated
        Amount getCashBalance() {
            return new Amount(cashBalance.getCurrencyCode(), cashBalance.getDoubleValue());
        }

        @Deprecated
        public Builder<A, T> setCashBalance(Amount cashBalance) {
            this.cashBalance =
                    ExactCurrencyAmount.of(cashBalance.getValue(), cashBalance.getCurrency());
            return this;
        }

        public Builder<A, T> setCashBalance(ExactCurrencyAmount cashBalance) {
            this.cashBalance = ExactCurrencyAmount.of(cashBalance);
            return this;
        }

        ExactCurrencyAmount getExactCashBalance() {
            return ExactCurrencyAmount.of(cashBalance);
        }

        @Override
        @Deprecated
        public Amount getBalance() {
            if (cashBalance != null) {
                BigDecimal retVal = cashBalance.getExactValue();
                for (Portfolio portfolio : portfolios) {
                    retVal = retVal.add(BigDecimal.valueOf(portfolio.getTotalValue()));
                }
                return new Amount(cashBalance.getCurrencyCode(), retVal.doubleValue());
            } else {
                ExactCurrencyAmount exactBalance = super.getExactBalance();
                return new Amount(exactBalance.getCurrencyCode(), exactBalance.getDoubleValue());
            }
        }

        /** @deprecated Use {@link #setCashBalance(Amount)} instead */
        @Override
        @Deprecated
        public Builder<A, T> setBalance(Amount balance) {
            return super.setExactBalance(
                    ExactCurrencyAmount.of(balance.getValue(), balance.getCurrency()));
        }

        @Override
        public ExactCurrencyAmount getExactBalance() {
            if (cashBalance != null) {
                BigDecimal retVal = cashBalance.getExactValue();
                for (Portfolio portfolio : portfolios) {
                    retVal = retVal.add(BigDecimal.valueOf(portfolio.getTotalValue()));
                }
                return ExactCurrencyAmount.of(retVal, cashBalance.getCurrencyCode());
            } else {
                return super.getExactBalance();
            }
        }
    }

    private static class DefaultInvestmentAccountsBuilder
            extends InvestmentAccount.Builder<InvestmentAccount, DefaultInvestmentAccountsBuilder> {

        public DefaultInvestmentAccountsBuilder(String uniqueIdentifier) {
            super(uniqueIdentifier);
        }

        @Override
        protected DefaultInvestmentAccountsBuilder self() {
            return this;
        }

        @Override
        public InvestmentAccount build() {
            return new InvestmentAccount(this);
        }
    }
}
