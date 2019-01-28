package se.tink.backend.aggregation.nxgen.core.account;

import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;

public final class AccountTypeMapperTest {
    @Test
    public void ensureIsCheckingAccount_withNothingKnown_returnsFalse() {
        final AccountTypeMapper mapper = AccountTypeMapper.builder().build();

        Assert.assertFalse(mapper.isCheckingAccount("CHECKING_ACCOUNT"));
    }

    @Test
    public void ensureIsCheckingAccount_withKnownButUnmapped_returnsFalse() {
        final AccountTypeMapper mapper =
                AccountTypeMapper.builder().ignoreKeys("CHECKING_ACCOUNT").build();

        Assert.assertFalse(mapper.isCheckingAccount("CHECKING_ACCOUNT"));
    }

    @Test
    public void ensureIsCheckingAccount_withCheckingStringMappedToChecking_returnsTrue() {
        final AccountTypeMapper mapper =
                AccountTypeMapper.builder().put(AccountTypes.CHECKING, "CHECKING_ACCOUNT").build();

        Assert.assertTrue(mapper.isCheckingAccount("CHECKING_ACCOUNT"));
    }

    @Test
    public void ensureIsCheckingAccount_withOtherStringMappedToChecking_returnsFalse() {
        final AccountTypeMapper mapper =
                AccountTypeMapper.builder().put(AccountTypes.CHECKING, "SAVINGS_ACCOUNT").build();

        Assert.assertFalse(mapper.isCheckingAccount("CHECKING_ACCOUNT"));
    }

    @Test
    public void ensureTranslate_withCheckingStringMappedToChecking_returnsChecking() {
        final AccountTypeMapper mapper =
                AccountTypeMapper.builder().put(AccountTypes.CHECKING, "CHECKING_ACCOUNT").build();

        final Optional<AccountTypes> returned = mapper.translate("CHECKING_ACCOUNT");

        Assert.assertTrue(returned.isPresent());
        Assert.assertEquals(AccountTypes.CHECKING, returned.get());
    }

    @Test
    public void ensureTranslate_withOtherStringMappedToChecking_returnsEmpty() {
        final AccountTypeMapper mapper =
                AccountTypeMapper.builder().put(AccountTypes.CHECKING, "SAVINGS_ACCOUNT").build();

        final Optional<AccountTypes> returned = mapper.translate("CHECKING_ACCOUNT");

        Assert.assertFalse(returned.isPresent());
    }
}
