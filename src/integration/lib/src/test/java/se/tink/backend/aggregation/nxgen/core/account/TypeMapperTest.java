package se.tink.backend.aggregation.nxgen.core.account;

import java.util.Arrays;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;

public final class TypeMapperTest {
    @Test
    public void ensureTranslate_withNothingKnown_returnsEmpty() {
        final TypeMapper<AccountTypes> mapper = TypeMapper.<AccountTypes>builder().build();

        Assert.assertFalse(mapper.translate("CHECKING_ACCOUNT").isPresent());
    }

    @Test
    public void ensureTranslate_withIgnored_returnsEmpty() {
        final TypeMapper<AccountTypes> mapper =
                TypeMapper.<AccountTypes>builder().ignoreKeys("CHECKING_ACCOUNT").build();

        Assert.assertFalse(mapper.translate("CHECKING_ACCOUNT").isPresent());
    }

    @Test
    public void ensureTranslate_withCheckingStringMappedToChecking_returnsChecking() {
        final TypeMapper<AccountTypes> mapper =
                TypeMapper.<AccountTypes>builder()
                        .put(AccountTypes.CHECKING, "CHECKING_ACCOUNT")
                        .build();

        Assert.assertEquals(
                mapper.translate("CHECKING_ACCOUNT"), Optional.of(AccountTypes.CHECKING));
    }

    @Test
    public void ensureTranslate_withOtherStringMappedToChecking_returnsEmpty() {
        final TypeMapper<AccountTypes> mapper =
                TypeMapper.<AccountTypes>builder()
                        .put(AccountTypes.CHECKING, "SAVINGS_ACCOUNT")
                        .build();

        Assert.assertFalse(mapper.translate("CHECKING_ACCOUNT").isPresent());
    }

    @Test
    public void ensureTranslate_withDifferentCapitalisation_returnsChecking() {
        final TypeMapper<AccountTypes> mapper =
                TypeMapper.<AccountTypes>builder()
                        .put(AccountTypes.CHECKING, "checking_account")
                        .build();

        Assert.assertEquals(
                mapper.translate("CHECKING_ACCOUNT"), Optional.of(AccountTypes.CHECKING));
    }

    @Test(expected = IllegalStateException.class)
    public void ensureIgnore_withAlreadyMappedValue_throwsException() {
        final TypeMapper<AccountTypes> mapper =
                TypeMapper.<AccountTypes>builder()
                        .put(AccountTypes.CHECKING, "SAVINGS_ACCOUNT")
                        .ignoreKeys("SAVINGS_ACCOUNT")
                        .build();
    }

    @Test(expected = IllegalStateException.class)
    public void ensureIgnore_withAlreadyMappedValueButDifferingCapitalisation_throwsException() {
        final TypeMapper<AccountTypes> mapper =
                TypeMapper.<AccountTypes>builder()
                        .put(AccountTypes.CHECKING, "SAVINGS_ACCOUNT")
                        .ignoreKeys("savings_account")
                        .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureTranslate_withAlreadyMappedValue_throwsException() {
        final TypeMapper<AccountTypes> mapper =
                TypeMapper.<AccountTypes>builder()
                        .put(AccountTypes.CHECKING, "SAVINGS_ACCOUNT")
                        .put(AccountTypes.SAVINGS, "SAVINGS_ACCOUNT")
                        .build();
    }

    @Test
    public void ensureTranslate_withNullInput_returnsEmpty() {
        final TypeMapper<AccountTypes> mapper =
                TypeMapper.<AccountTypes>builder()
                        .put(AccountTypes.CHECKING, "checking_account")
                        .build();

        Assert.assertEquals(mapper.translate(null), Optional.empty());
    }

    @Test
    public void ensureTranslate_withEmptyInput_returnsEmpty() {
        final TypeMapper<AccountTypes> mapper =
                TypeMapper.<AccountTypes>builder()
                        .put(AccountTypes.CHECKING, "checking_account")
                        .build();

        Assert.assertEquals(mapper.translate(""), Optional.empty());
    }

    @Test
    public void isOneOf_isOfGivenType() {
        String checking_account = "checking_account";
        final TypeMapper<AccountTypes> mapper =
                TypeMapper.<AccountTypes>builder()
                        .put(AccountTypes.CHECKING, checking_account, "other_checking_account")
                        .build();
        Assert.assertTrue(mapper.isOneOf(checking_account, Arrays.asList(AccountTypes.CHECKING)));
    }

    @Test
    public void isOneOf_isOfDifferentType() {
        String checking_account = "not_checking_account";
        final TypeMapper<AccountTypes> mapper =
                TypeMapper.<AccountTypes>builder()
                        .put(AccountTypes.CHECKING, "checking_account")
                        .put(AccountTypes.MORTGAGE, checking_account)
                        .build();
        Assert.assertFalse(mapper.isOneOf(checking_account, Arrays.asList(AccountTypes.CHECKING)));
    }

    @Test
    public void isOneOf_isNotOfAnyType() {
        final TypeMapper<AccountTypes> mapper =
                TypeMapper.<AccountTypes>builder()
                        .put(AccountTypes.CHECKING, "checking_account")
                        .build();
        Assert.assertFalse(mapper.isOneOf("other_type", Arrays.asList(AccountTypes.CHECKING)));
    }

    @Test
    public void isOf_isOfGivenType() {
        String checking_account = "checking_account";
        final TypeMapper<AccountTypes> mapper =
                TypeMapper.<AccountTypes>builder()
                        .put(AccountTypes.CHECKING, checking_account, "other_checking_account")
                        .build();
        Assert.assertTrue(mapper.isOf(checking_account, AccountTypes.CHECKING));
    }

    @Test
    public void isOf_isOfDifferentType() {
        String checking_account = "not_checking_account";
        final TypeMapper<AccountTypes> mapper =
                TypeMapper.<AccountTypes>builder()
                        .put(AccountTypes.CHECKING, "checking_account")
                        .put(AccountTypes.MORTGAGE, checking_account)
                        .build();
        Assert.assertFalse(mapper.isOf(checking_account, AccountTypes.CHECKING));
    }

    @Test
    public void isOf_isNotOfType() {
        final TypeMapper<AccountTypes> mapper =
                TypeMapper.<AccountTypes>builder()
                        .put(AccountTypes.CHECKING, "checking_account")
                        .build();
        Assert.assertFalse(mapper.isOf("other_type", AccountTypes.CHECKING));
    }

    @Test
    public void setDefaultReturnValue_defaultValueSet() {
        final TypeMapper<AccountTypes> mapper =
                TypeMapper.<AccountTypes>builder()
                        .put(AccountTypes.CHECKING, "a")
                        .put(AccountTypes.CREDIT_CARD, "b", "c")
                        .setDefaultTranslationValue(AccountTypes.DUMMY)
                        .build();

        Assert.assertEquals(AccountTypes.DUMMY, mapper.translate("z").get());
    }

    @Test
    public void setDefaultReturnValue_noDefaulyValueSet() {
        final TypeMapper<AccountTypes> mapper =
                TypeMapper.<AccountTypes>builder()
                        .put(AccountTypes.CHECKING, "a")
                        .put(AccountTypes.CREDIT_CARD, "b", "c")
                        .build();

        Assert.assertEquals(Optional.empty(), mapper.translate("z"));
    }
}
