package se.tink.backend.aggregation.nxgen.core.account;

import java.util.Optional;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.rpc.AccountTypes;

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

    @Test
    public void ensureTranslate_withSavingsStringMappedToSavingsAndRegex_returnsSavings() {
        final AccountTypeMapper mapper =
                AccountTypeMapper.builder()
                        .put(AccountTypes.SAVINGS, "S042", "S108")
                        .putRegex(AccountTypes.SAVINGS, Pattern.compile("S\\w\\w\\w"))
                        .build();

        final Optional<AccountTypes> returned = mapper.translate("S108");

        Assert.assertTrue(returned.isPresent());
        Assert.assertEquals(AccountTypes.SAVINGS, returned.get());
    }

    @Test
    public void ensureTranslate_withSavingsStringMappedToRegexOnly_returnsSavings() {
        final AccountTypeMapper mapper =
                AccountTypeMapper.builder()
                        .put(AccountTypes.SAVINGS, "S042", "S108")
                        .putRegex(AccountTypes.SAVINGS, Pattern.compile("S\\w\\w\\w"))
                        .build();

        final Optional<AccountTypes> returned = mapper.translate("S023");

        Assert.assertTrue(returned.isPresent());
        Assert.assertEquals(AccountTypes.SAVINGS, returned.get());
    }

    @Test
    public void ensureTranslate_withSavingsStringMappedToNeither_returnsEmpty() {
        final AccountTypeMapper mapper =
                AccountTypeMapper.builder()
                        .put(AccountTypes.SAVINGS, "S042", "S108")
                        .putRegex(AccountTypes.SAVINGS, Pattern.compile("S\\w\\w\\w"))
                        .build();

        final Optional<AccountTypes> returned = mapper.translate("D023");

        Assert.assertTrue(!returned.isPresent());
    }

    @Test
    public void ensureTranslate_withTwoSavingsRegexesOneMatch_returnsSavings() {
        final AccountTypeMapper mapper =
                AccountTypeMapper.builder()
                        .putRegex(AccountTypes.SAVINGS, Pattern.compile("S\\w\\w\\w"))
                        .putRegex(AccountTypes.SAVINGS, Pattern.compile("s\\w\\w\\w"))
                        .build();

        final Optional<AccountTypes> returned = mapper.translate("S023");

        Assert.assertTrue(returned.isPresent());
        Assert.assertEquals(AccountTypes.SAVINGS, returned.get());
    }

    @Test
    public void ensureTranslate_withTwoMatchingMappedToSavings_returnsSavings() {
        final AccountTypeMapper mapper =
                AccountTypeMapper.builder()
                        .putRegex(AccountTypes.SAVINGS, Pattern.compile("S\\w\\w\\w"))
                        .putRegex(AccountTypes.SAVINGS, Pattern.compile("\\w\\w\\wS"))
                        .putRegex(AccountTypes.LOAN, Pattern.compile("L\\w\\w\\w"))
                        .build();

        final Optional<AccountTypes> returned = mapper.translate("S42S");

        Assert.assertTrue(returned.isPresent());
        Assert.assertEquals(AccountTypes.SAVINGS, returned.get());
    }

    @Test
    public void
            ensureTranslate_withTwoMatchingMappedToSavingsAndChecking_returnsSavingsOrChecking() {
        final AccountTypeMapper mapper =
                AccountTypeMapper.builder()
                        .putRegex(AccountTypes.SAVINGS, Pattern.compile("S\\w\\w\\w"))
                        .putRegex(AccountTypes.CHECKING, Pattern.compile("\\w\\w\\wC"))
                        .putRegex(AccountTypes.LOAN, Pattern.compile("L\\w\\w\\w"))
                        .build();

        final Optional<AccountTypes> returned = mapper.translate("S42C");

        Assert.assertTrue(returned.isPresent());
        Assert.assertTrue(
                returned.get() == AccountTypes.CHECKING || returned.get() == AccountTypes.SAVINGS);
    }
}
