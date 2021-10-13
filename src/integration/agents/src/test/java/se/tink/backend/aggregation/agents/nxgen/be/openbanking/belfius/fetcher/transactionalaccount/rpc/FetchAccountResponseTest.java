package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.rpc;

import java.math.BigDecimal;
import java.util.Optional;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.enums.AccountFlag;

public class FetchAccountResponseTest {

    @Test
    public void shouldMapToTinkAccount() throws IllegalAccessException {
        // given
        final BigDecimal balance = new BigDecimal(100);
        final String currency = "EUR";
        final String iban = "BE68539007547034";
        final String accountType = "account type";
        final String accountName = "Name Surname";
        FetchAccountResponse objectUnderTest = new FetchAccountResponse();
        FieldUtils.writeDeclaredField(objectUnderTest, "balance", balance, true);
        FieldUtils.writeDeclaredField(objectUnderTest, "currency", currency, true);
        FieldUtils.writeDeclaredField(objectUnderTest, "iban", iban, true);
        FieldUtils.writeDeclaredField(objectUnderTest, "type", accountType, true);
        FieldUtils.writeDeclaredField(objectUnderTest, "accountName", accountName, true);
        // when
        Optional<TransactionalAccount> result = objectUnderTest.toTinkAccount("logicalId");
        // then
        Assert.assertTrue(result.isPresent());
        TransactionalAccount tinkAccount = result.get();
        Assert.assertEquals(
                TransactionalAccountType.CHECKING.toAccountType(), tinkAccount.getType());
        Assert.assertEquals(balance, tinkAccount.getExactBalance().getExactValue());
        Assert.assertEquals(currency, tinkAccount.getExactBalance().getCurrencyCode());
        Assert.assertEquals(iban, tinkAccount.getIdentifiersAsList().get(0).getIdentifier());
        Assert.assertEquals(iban, tinkAccount.getAccountNumber());
        Assert.assertEquals(accountType, tinkAccount.getName());
        Assert.assertEquals(accountName, tinkAccount.getHolderName().toString());
        Assert.assertTrue(tinkAccount.getAccountFlags().contains(AccountFlag.PSD2_PAYMENT_ACCOUNT));
    }
}
