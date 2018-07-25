package se.tink.backend.aggregation.nxgen.core.account;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;
import se.tink.backend.system.rpc.Portfolio;

public class InvestmentAccount extends Account {
    public static final ImmutableList<AccountTypes> ALLOWED_ACCOUNT_TYPES =
            ImmutableList.<AccountTypes>builder()
                    .add(AccountTypes.INVESTMENT)
                    .add(AccountTypes.PENSION)
                    .build();

    private List<Portfolio> portfolios;

    private InvestmentAccount(Builder<InvestmentAccount, DefaultInvestmentAccountsBuilder> builder) {
        super(builder);
        this.portfolios = builder.getPortfolios();
    }

    public static Builder<InvestmentAccount, DefaultInvestmentAccountsBuilder> builder(String uniqueIdentifier) {
        return new DefaultInvestmentAccountsBuilder(uniqueIdentifier);
    }

    public static Builder<InvestmentAccount, DefaultInvestmentAccountsBuilder> builder(
            String uniqueIdentifier, Amount balance) {
        return builder(uniqueIdentifier)
                .setBalance(balance);
    }

    public List<Portfolio> getPortfolios() {
        return this.portfolios != null
                ? ImmutableList.copyOf(this.portfolios)
                : Collections.emptyList();
    }

    @Override
    public AccountTypes getType() {
        return AccountTypes.INVESTMENT;
    }

    public abstract static class Builder<
            A extends InvestmentAccount, T extends InvestmentAccount.Builder<A, T>>
            extends Account.Builder<InvestmentAccount, Builder<A, T>> {
        private List<Portfolio> portfolios;

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
