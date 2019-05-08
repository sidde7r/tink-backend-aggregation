package se.tink.backend.aggregation.nxgen.core.account;

import java.util.Arrays;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;

public class GenericTypeMapperTest {
    @Test
    public void ensureTranslate_withNothingKnown_returnsEmpty() {
        final GenericTypeMapper<String, Integer> mapper =
                GenericTypeMapper.<String, Integer>genericBuilder().build();

        Assert.assertFalse(mapper.translate(Integer.valueOf(1)).isPresent());
    }

    @Test
    public void ensureTranslate_withIgnored_returnsEmpty() {
        final GenericTypeMapper<String, Integer> mapper =
                GenericTypeMapper.<String, Integer>genericBuilder()
                        .ignoreKeys(Integer.valueOf(7), null)
                        .build();

        Assert.assertFalse(mapper.translate(Integer.valueOf(7)).isPresent());
    }

    @Test
    public void ensureTranslate_withOnlyOneValue_returnsProper() {
        final GenericTypeMapper<String, AccountTypes> mapper =
                GenericTypeMapper.<String, AccountTypes>genericBuilder()
                        .put("CHECKING_ACCOUNT", AccountTypes.CHECKING)
                        .build();

        Assert.assertEquals(
                mapper.translate(AccountTypes.CHECKING), Optional.of("CHECKING_ACCOUNT"));
    }

    @Test
    public void ensureTranslate_withOtherStringMappedToChecking_returnsEmpty() {
        final GenericTypeMapper<AccountTypes, String> mapper =
                GenericTypeMapper.<AccountTypes, String>genericBuilder()
                        .put(AccountTypes.CHECKING, "SAVINGS_ACCOUNT")
                        .build();

        Assert.assertFalse(mapper.translate("CHECKING_ACCOUNT").isPresent());
    }

    @Test(expected = IllegalStateException.class)
    public void ensureIgnore_withAlreadyMappedValue_throwsException() {
        final GenericTypeMapper<AccountTypes, String> mapper =
                GenericTypeMapper.<AccountTypes, String>genericBuilder()
                        .put(AccountTypes.CHECKING, "SAVINGS_ACCOUNT")
                        .ignoreKeys("SAVINGS_ACCOUNT")
                        .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureTranslate_withAlreadyMappedValue_throwsException() {
        final GenericTypeMapper<AccountTypes, String> mapper =
                GenericTypeMapper.<AccountTypes, String>genericBuilder()
                        .put(AccountTypes.CHECKING, "SAVINGS_ACCOUNT")
                        .put(AccountTypes.SAVINGS, "SAVINGS_ACCOUNT")
                        .build();
    }

    @Test
    public void ensureTranslate_withNullInput_returnsEmpty() {
        final GenericTypeMapper<AccountTypes, String> mapper =
                GenericTypeMapper.<AccountTypes, String>genericBuilder()
                        .put(AccountTypes.CHECKING, "checking_account")
                        .build();

        Assert.assertEquals(mapper.translate(null), Optional.empty());
    }

    @Test
    public void ensureTranslate_withEmptyInput_returnsEmpty() {
        final GenericTypeMapper<AccountTypes, String> mapper =
                GenericTypeMapper.<AccountTypes, String>genericBuilder()
                        .put(AccountTypes.CHECKING, "checking_account")
                        .build();

        Assert.assertEquals(mapper.translate(""), Optional.empty());
    }

    @Test
    public void isOneOf_isOfGivenType() {
        String checking_account = "checking_account";
        final GenericTypeMapper<AccountTypes, String> mapper =
                GenericTypeMapper.<AccountTypes, String>genericBuilder()
                        .put(AccountTypes.CHECKING, checking_account, "other_checking_account")
                        .build();
        Assert.assertTrue(mapper.isOneOf(checking_account, Arrays.asList(AccountTypes.CHECKING)));
    }

    @Test
    public void isOneOf_isOfDifferentType() {
        String checking_account = "not_checking_account";
        final GenericTypeMapper<AccountTypes, String> mapper =
                GenericTypeMapper.<AccountTypes, String>genericBuilder()
                        .put(AccountTypes.CHECKING, "checking_account")
                        .put(AccountTypes.MORTGAGE, checking_account)
                        .build();
        Assert.assertFalse(mapper.isOneOf(checking_account, Arrays.asList(AccountTypes.CHECKING)));
    }

    @Test
    public void isOneOf_isNotOfAnyType() {
        final GenericTypeMapper<AccountTypes, String> mapper =
                GenericTypeMapper.<AccountTypes, String>genericBuilder()
                        .put(AccountTypes.CHECKING, "checking_account")
                        .build();
        Assert.assertFalse(mapper.isOneOf("other_type", Arrays.asList(AccountTypes.CHECKING)));
    }

    @Test
    public void isOf_isOfGivenType() {
        String checking_account = "checking_account";
        final GenericTypeMapper<AccountTypes, String> mapper =
                GenericTypeMapper.<AccountTypes, String>genericBuilder()
                        .put(AccountTypes.CHECKING, checking_account, "other_checking_account")
                        .build();
        Assert.assertTrue(mapper.isOf(checking_account, AccountTypes.CHECKING));
    }

    @Test
    public void isOf_isOfDifferentType() {
        String checking_account = "not_checking_account";
        final GenericTypeMapper<AccountTypes, String> mapper =
                GenericTypeMapper.<AccountTypes, String>genericBuilder()
                        .put(AccountTypes.CHECKING, "checking_account")
                        .put(AccountTypes.MORTGAGE, checking_account)
                        .build();
        Assert.assertFalse(mapper.isOf(checking_account, AccountTypes.CHECKING));
    }

    @Test
    public void isOf_isNotOfType() {
        final GenericTypeMapper<AccountTypes, String> mapper =
                GenericTypeMapper.<AccountTypes, String>genericBuilder()
                        .put(AccountTypes.CHECKING, "checking_account")
                        .build();
        Assert.assertFalse(mapper.isOf("other_type", AccountTypes.CHECKING));
    }

    @Test
    public void getMappedTypes_noTypes() {
        final GenericTypeMapper<AccountTypes, String> mapper =
                GenericTypeMapper.<AccountTypes, String>genericBuilder()
                        .build();
        Assert.assertTrue(mapper.getMappedTypes().isEmpty());
    }
    @Test
    public void getMappedTypes_multipleTypes() {
        final GenericTypeMapper<AccountTypes, String> mapper =
                GenericTypeMapper.<AccountTypes, String>genericBuilder()
                        .put(AccountTypes.CHECKING, "a")
                        .put(AccountTypes.CREDIT_CARD, "b", "c")
                        .build();

        Assert.assertEquals(2, mapper.getMappedTypes().size());
        Assert.assertTrue(mapper.getMappedTypes().contains(AccountTypes.CHECKING));
        Assert.assertTrue(mapper.getMappedTypes().contains(AccountTypes.CREDIT_CARD));
    }
}
