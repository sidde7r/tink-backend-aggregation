package se.tink.backend.aggregation.nxgen.core.account;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;
import se.tink.backend.system.rpc.Portfolio;
import se.tink.libraries.account.AccountIdentifier;

public class InvestmentAccount extends Account {
    public static final ImmutableList<AccountTypes> ALLOWED_ACCOUNT_TYPES = ImmutableList.<AccountTypes>builder()
            .add(AccountTypes.INVESTMENT)
            .add(AccountTypes.PENSION)
            .build();

    private final List<Portfolio> portfolios;

    private InvestmentAccount(String name, String accountNumber, Amount balance, List<AccountIdentifier> identifiers,
            String tinkId, String bankIdentifier, List<Portfolio> portfolios, Map<String, String> temporaryStorage) {
        super(name, accountNumber, balance, identifiers, tinkId, bankIdentifier, null, temporaryStorage);
        this.portfolios = portfolios;
    }

    public List<Portfolio> getPortfolios() {
        return this.portfolios != null ? ImmutableList.copyOf(this.portfolios) : Collections.emptyList();
    }

    @Override
    public AccountTypes getType() {
        return AccountTypes.INVESTMENT;
    }

    public static Builder builder(String accountNumber, Amount balance) {
        return new Builder(accountNumber, balance);
    }

    public static class Builder extends Account.Builder {
        private List<Portfolio> portfolios;

        private Builder(String accountNumber, Amount balance) {
            super(accountNumber, balance);
        }

        public List<Portfolio> getPortfolios() {
            return this.portfolios != null ? this.portfolios : Collections.emptyList();
        }

        public Builder setPortfolios(List<Portfolio> portfolios) {
            this.portfolios = portfolios;
            return this;
        }

        @Override
        public Builder setName(String name) {
            return (Builder) super.setName(name);
        }

        @Override
        public Builder addIdentifier(AccountIdentifier identifier) {
            return (Builder) super.addIdentifier(identifier);
        }

        @Override
        public Builder setUniqueIdentifier(String uniqueIdentifier) {
            return (Builder) super.setUniqueIdentifier(uniqueIdentifier);
        }

        @Override
        public Builder setBankIdentifier(String bankIdentifier) {
            return (Builder) super.setBankIdentifier(bankIdentifier);
        }

        @Override
        public InvestmentAccount build() {
            return new InvestmentAccount(getName(), getAccountNumber(), getBalance(), getIdentifiers(), getUniqueIdentifier(),
                    getBankIdentifier(), getPortfolios(), getTemporaryStorage());
        }

        @Override
        public Builder setTemporaryStorage(Map<String, String> temporaryStorage) {
            return (Builder) super.setTemporaryStorage(temporaryStorage);
        }

        @Override
        public <T> Builder addToTemporaryStorage(String key, T value) {
            return (Builder) super.addToTemporaryStorage(key, value);
        }
    }
}
