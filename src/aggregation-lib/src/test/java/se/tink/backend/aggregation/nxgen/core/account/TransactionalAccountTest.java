package se.tink.backend.aggregation.nxgen.core.account;

import org.assertj.core.util.Lists;
import org.assertj.core.util.Maps;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;
import static org.junit.Assert.assertEquals;

public class TransactionalAccountTest {

  @Test
  public void ensureBankIdentifierHasCorrectFormat() {
    TransactionalAccount transactionalAccount =
        TransactionalAccount.builder(AccountTypes.CHECKING, "123456", Amount.inSEK(1.0))
            .setBankIdentifier("123456")
            .setName("")
            .build();

    assertEquals("123456", transactionalAccount.getBankIdentifier());
  }

  @Test
  public void checkDifferentOrderOfMethods() {
    TransactionalAccount.Builder<?, ?> transactionalBuilder =
        TransactionalAccount.builder(AccountTypes.CHECKING)
            .setHolderName(new HolderName("name"))
            .setAccountNumber("123")
            .setBalance(Amount.inSEK(12d))
            .setTemporaryStorage(Maps.newHashMap());
    transactionalBuilder.setBalance(Amount.inDKK(20d));
    TransactionalAccount transactionalAccount = transactionalBuilder.build();
    assertEquals(Amount.inDKK(20d), transactionalAccount.getBalance());
  }

  @Test
  public void savingAccountBuilderRandomSettingOrder() {
    SavingsAccount savingsAccount =
        SavingsAccount.builder()
            // setting additional data before general one
            .setInterestRate(1d)
            .setAccountNumber("123")
            .setBalance(Amount.inEUR(1d))
            // setting additional data after general one
            .setInterestRate(2d)
            .build();
    assertEquals(Double.valueOf(2d), savingsAccount.getInterestRate());
  }
  // Below code is in place to ensure correctness of the builder types, by checking if the code is
  // compiled. No need to execute it.
  public void ensureProperTypingForBuilder() {
    SavingsAccount saving =
        SavingsAccount.builder().setBalance(Amount.inNOK(2d)).setAccountNumber("acc").build();
    CheckingAccount checking =
        CheckingAccount.builder().setBalance(Amount.inEUR(1d)).setAccountNumber("acc").build();
    CreditCardAccount creditCard =
        CreditCardAccount.builder()
            .setAvailableCredit(Amount.inDKK(1d))
            .setAccountNumber("acc")
            .setBalance(Amount.inDKK(1d))
            .build();
    LoanAccount loan =
        LoanAccount.builder()
            .setAccountNumber("acc")
            .setDetails(LoanDetails.builder().build())
            .setInterestRate(3d)
            .setBalance(Amount.inSEK(1d))
            .build();
    InvestmentAccount investment =
        InvestmentAccount.builder()
            .setAccountNumber("acc")
            .setPortfolios(Lists.newArrayList())
            .build();
  }
}
