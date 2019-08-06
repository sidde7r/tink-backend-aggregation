package se.tink.backend.aggregation.nxgen.core.account;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;

public class GenericTypeMapperTest {
    @Test
    public void ensureTranslate_withNothingKnown_returnsEmpty() {
        final GenericTypeMapper<String, Integer> mapper =
                GenericTypeMapper.<String, Integer>genericBuilder().build();

        assertFalse(mapper.translate(1).isPresent());
    }

    @Test
    public void ensureTranslate_withIgnored_returnsEmpty() {
        final GenericTypeMapper<String, Integer> mapper =
                GenericTypeMapper.<String, Integer>genericBuilder().ignoreKeys(7, null).build();

        assertFalse(mapper.translate(7).isPresent());
    }

    @Test
    public void ensureTranslate_withOnlyOneValue_returnsProper() {
        final GenericTypeMapper<String, AccountTypes> mapper =
                GenericTypeMapper.<String, AccountTypes>genericBuilder()
                        .put("CHECKING_ACCOUNT", AccountTypes.CHECKING)
                        .build();

        assertEquals(mapper.translate(AccountTypes.CHECKING), Optional.of("CHECKING_ACCOUNT"));
    }

    @Test
    public void ensureTranslate_withOtherStringMappedToChecking_returnsEmpty() {
        final GenericTypeMapper<AccountTypes, String> mapper =
                GenericTypeMapper.<AccountTypes, String>genericBuilder()
                        .put(AccountTypes.CHECKING, "SAVINGS_ACCOUNT")
                        .build();

        assertFalse(mapper.translate("CHECKING_ACCOUNT").isPresent());
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

        assertEquals(mapper.translate(null), Optional.empty());
    }

    @Test
    public void ensureTranslate_withEmptyInput_returnsEmpty() {
        final GenericTypeMapper<AccountTypes, String> mapper =
                GenericTypeMapper.<AccountTypes, String>genericBuilder()
                        .put(AccountTypes.CHECKING, "checking_account")
                        .build();

        assertEquals(mapper.translate(""), Optional.empty());
    }

    @Test
    public void isOneOf_isOfGivenType() {
        String checking_account = "checking_account";
        final GenericTypeMapper<AccountTypes, String> mapper =
                GenericTypeMapper.<AccountTypes, String>genericBuilder()
                        .put(AccountTypes.CHECKING, checking_account, "other_checking_account")
                        .build();
        assertTrue(
                mapper.isOneOf(checking_account, Collections.singletonList(AccountTypes.CHECKING)));
    }

    @Test
    public void isOneOf_isOfDifferentType() {
        String checking_account = "not_checking_account";
        final GenericTypeMapper<AccountTypes, String> mapper =
                GenericTypeMapper.<AccountTypes, String>genericBuilder()
                        .put(AccountTypes.CHECKING, "checking_account")
                        .put(AccountTypes.MORTGAGE, checking_account)
                        .build();
        assertFalse(
                mapper.isOneOf(checking_account, Collections.singletonList(AccountTypes.CHECKING)));
    }

    @Test
    public void isOneOf_isNotOfAnyType() {
        final GenericTypeMapper<AccountTypes, String> mapper =
                GenericTypeMapper.<AccountTypes, String>genericBuilder()
                        .put(AccountTypes.CHECKING, "checking_account")
                        .build();
        assertFalse(mapper.isOneOf("other_type", Collections.singletonList(AccountTypes.CHECKING)));
    }

    @Test
    public void isOf_isOfGivenType() {
        String checking_account = "checking_account";
        final GenericTypeMapper<AccountTypes, String> mapper =
                GenericTypeMapper.<AccountTypes, String>genericBuilder()
                        .put(AccountTypes.CHECKING, checking_account, "other_checking_account")
                        .build();
        assertTrue(mapper.isOf(checking_account, AccountTypes.CHECKING));
    }

    @Test
    public void isOf_isOfDifferentType() {
        String checking_account = "not_checking_account";
        final GenericTypeMapper<AccountTypes, String> mapper =
                GenericTypeMapper.<AccountTypes, String>genericBuilder()
                        .put(AccountTypes.CHECKING, "checking_account")
                        .put(AccountTypes.MORTGAGE, checking_account)
                        .build();
        assertFalse(mapper.isOf(checking_account, AccountTypes.CHECKING));
    }

    @Test
    public void isOf_isNotOfType() {
        final GenericTypeMapper<AccountTypes, String> mapper =
                GenericTypeMapper.<AccountTypes, String>genericBuilder()
                        .put(AccountTypes.CHECKING, "checking_account")
                        .build();
        assertFalse(mapper.isOf("other_type", AccountTypes.CHECKING));
    }

    @Test
    public void getMappedTypes_noTypes() {
        final GenericTypeMapper<AccountTypes, String> mapper =
                GenericTypeMapper.<AccountTypes, String>genericBuilder().build();
        assertTrue(mapper.getMappedTypes().isEmpty());
    }

    @Test
    public void getMappedTypes_multipleTypes() {
        final GenericTypeMapper<AccountTypes, String> mapper =
                GenericTypeMapper.<AccountTypes, String>genericBuilder()
                        .put(AccountTypes.CHECKING, "a")
                        .put(AccountTypes.CREDIT_CARD, "b", "c")
                        .build();

        assertEquals(2, mapper.getMappedTypes().size());
        assertTrue(mapper.getMappedTypes().contains(AccountTypes.CHECKING));
        assertTrue(mapper.getMappedTypes().contains(AccountTypes.CREDIT_CARD));
    }

    @Test
    public void setDefaultReturnValue_defaultValueSet() {
        final GenericTypeMapper<AccountTypes, String> mapper =
                GenericTypeMapper.<AccountTypes, String>genericBuilder()
                        .put(AccountTypes.CHECKING, "a")
                        .put(AccountTypes.CREDIT_CARD, "b", "c")
                        .setDefaultTranslationValue(AccountTypes.DUMMY)
                        .build();

        assertEquals(AccountTypes.DUMMY, mapper.translate("z").get());
    }

    @Test
    public void setDefaultReturnValue_noDefaulyValueSet() {
        final GenericTypeMapper<AccountTypes, String> mapper =
                GenericTypeMapper.<AccountTypes, String>genericBuilder()
                        .put(AccountTypes.CHECKING, "a")
                        .put(AccountTypes.CREDIT_CARD, "b", "c")
                        .build();

        assertEquals(Optional.empty(), mapper.translate("z"));
    }
}
