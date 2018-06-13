package se.tink.backend.aggregation.nxgen.core.account;

import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;

public class SavingsAccount extends TransactionalAccount {
  private final Double interestRate;

  //    private SavingsAccount(String name, String accountNumber, Amount balance,
  // List<AccountIdentifier> identifiers,
  //            String tinkId, String bankIdentifier, Double interestRate, HolderName holderName,
  //            Map<String, String> temporaryStorage) {
  //        super(name, accountNumber, balance, identifiers, tinkId, bankIdentifier, holderName,
  // temporaryStorage);
  //        this.interestRate = interestRate;
  //    }

  private SavingsAccount(Builder<?, ?> builder) {
    super(builder);
    this.interestRate = builder.getInterestRate();
  }

  //    public static Builder builder(String accountNumber, Amount balance) {
  //        return new Builder(accountNumber, balance);
  //    }

  public static Builder<?, ?> builder() {
    return new DefaultSavingAccountsBuilder();
  }

  public static Builder<?, ?> builder(String accountNumber, Amount balance) {
    DefaultSavingAccountsBuilder defaultSavingAccountsBuilder = new DefaultSavingAccountsBuilder();
    defaultSavingAccountsBuilder.setBalance(balance).setAccountNumber(accountNumber);
    return defaultSavingAccountsBuilder;
  }

  @Override
  public AccountTypes getType() {
    return AccountTypes.SAVINGS;
  }

  public Double getInterestRate() {
    return this.interestRate;
  }

  public abstract static class Builder<
          A extends SavingsAccount, T extends SavingsAccount.Builder<A, T>>
      extends TransactionalAccount.Builder<SavingsAccount, Builder<A, T>> {
    private Double interestRate;

    public Double getInterestRate() {
      return this.interestRate;
    }

    public Builder<A, T> setInterestRate(Double interestRate) {
      self().interestRate = interestRate;
      return self();
    }
  }

  private static class DefaultSavingAccountsBuilder
      extends SavingsAccount.Builder<SavingsAccount, DefaultSavingAccountsBuilder> {
    @Override
    protected DefaultSavingAccountsBuilder self() {
      return this;
    }

    @Override
    public SavingsAccount build() {
      return new SavingsAccount(this);
    }
  }
}
