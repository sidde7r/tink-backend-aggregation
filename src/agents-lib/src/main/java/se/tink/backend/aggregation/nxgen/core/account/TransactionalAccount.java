package se.tink.backend.aggregation.nxgen.core.account;

import com.google.common.collect.ImmutableList;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;

public class TransactionalAccount extends Account {
  public static final ImmutableList<AccountTypes> ALLOWED_ACCOUNT_TYPES =
      ImmutableList.<AccountTypes>builder()
          .add(AccountTypes.SAVINGS)
          .add(AccountTypes.CHECKING)
          .add(AccountTypes.OTHER)
          .build();

  protected TransactionalAccount(Builder<?, ?> builder) {
    super(builder);
  }

  public static Builder<?, ?> builder(AccountTypes type, String accountNumber, Amount balance) {
    return builder(type).setAccountNumber(accountNumber).setBalance(balance);
  }

  public static Builder<? extends Account, ?> builder(AccountTypes type) {
    switch (type) {
      case SAVINGS:
        return SavingsAccount.builder();
      case CHECKING:
      case OTHER:
        return CheckingAccount.builder();
      default:
        throw new IllegalStateException(
            String.format("Unknown TransactionalAccount type (%s)", type));
    }
  }

  public abstract static class Builder<A extends TransactionalAccount, T extends Builder<A, T>>
      extends Account.Builder<A, T> {

    protected Builder() {
      super();
    }

    @Override
    protected abstract T self();

    @Override
    public abstract A build();
  }
}
