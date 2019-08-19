package se.tink.backend.aggregation.nxgen.core.account;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.account.enums.AccountFlag;

public class TransactionalAccountTypeMapperTest {

    @Test
    public void ensureTranslate_withNothingKnown_returnsEmpty() {
        final AccountTypeMapper mapper = AccountTypeMapper.builder().build();

        assertFalse(mapper.translate("1").isPresent());
    }

    @Test
    public void ensureTranslate_withIgnored_returnsEmpty() {
        final AccountTypeMapper mapper = AccountTypeMapper.builder().ignoreKeys("7").build();

        assertFalse(mapper.translate("7").isPresent());
    }

    @Test
    public void ensureTranslate_withOnlyOneValue_returnsProper() {
        final AccountTypeMapper mapper =
                AccountTypeMapper.builder().put(AccountTypes.CHECKING, "CHECKING_ACCOUNT").build();

        assertEquals(mapper.translate("CHECKING_ACCOUNT"), Optional.of(AccountTypes.CHECKING));
    }

    @Test
    public void ensureTranslate_isCaseInsensitive() {
        final AccountTypeMapper mapper =
                AccountTypeMapper.builder().put(AccountTypes.CHECKING, "CHeCKiNG_aCCoUNT").build();

        assertEquals(mapper.translate("CHECKING_ACCOUNT"), Optional.of(AccountTypes.CHECKING));
    }

    @Test
    public void ensureTranslate_withOtherStringMappedToChecking_returnsEmpty() {
        final AccountTypeMapper mapper =
                AccountTypeMapper.builder().put(AccountTypes.CHECKING, "SAVINGS_ACCOUNT").build();

        assertFalse(mapper.translate("CHECKING_ACCOUNT").isPresent());
    }

    @Test(expected = IllegalStateException.class)
    public void ensureIgnore_withAlreadyMappedValue_throwsException() {
        final AccountTypeMapper mapper =
                AccountTypeMapper.builder()
                        .put(AccountTypes.CHECKING, "SAVINGS_ACCOUNT")
                        .ignoreKeys("SAVINGS_ACCOUNT")
                        .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureTranslate_withAlreadyMappedValue_throwsException() {
        final AccountTypeMapper mapper =
                AccountTypeMapper.builder()
                        .put(AccountTypes.CHECKING, "SAVINGS_ACCOUNT")
                        .put(AccountTypes.SAVINGS, "SAVINGS_ACCOUNT")
                        .build();
    }

    @Test
    public void ensureTranslate_withNullInput_returnsEmpty() {
        final AccountTypeMapper mapper =
                AccountTypeMapper.builder().put(AccountTypes.CHECKING, "checking_account").build();

        assertEquals(mapper.translate(null), Optional.empty());
    }

    @Test
    public void ensureTranslate_withEmptyInput_returnsEmpty() {
        final AccountTypeMapper mapper =
                AccountTypeMapper.builder().put(AccountTypes.CHECKING, "checking_account").build();

        assertEquals(mapper.translate(""), Optional.empty());
    }

    @Test
    public void isOneOf_isOfGivenType() {
        String checking_account = "checking_account";
        final AccountTypeMapper mapper =
                AccountTypeMapper.builder()
                        .put(AccountTypes.CHECKING, checking_account, "other_checking_account")
                        .build();
        assertTrue(
                mapper.isOneOf(checking_account, Collections.singletonList(AccountTypes.CHECKING)));
    }

    @Test
    public void isOneOf_isCaseInsensitive() {
        String checking_account = "checkING_account";
        final AccountTypeMapper mapper =
                AccountTypeMapper.builder()
                        .put(AccountTypes.CHECKING, checking_account, "other_checking_account")
                        .build();

        assertTrue(
                mapper.isOneOf(checking_account, Collections.singletonList(AccountTypes.CHECKING)));
    }

    @Test
    public void isOneOf_isOfDifferentType() {
        String checking_account = "not_checking_account";
        final AccountTypeMapper mapper =
                AccountTypeMapper.builder()
                        .put(AccountTypes.CHECKING, "checking_account")
                        .put(AccountTypes.MORTGAGE, checking_account)
                        .build();
        assertFalse(
                mapper.isOneOf(checking_account, Collections.singletonList(AccountTypes.CHECKING)));
    }

    @Test
    public void isOneOf_isNotOfAnyType() {
        final AccountTypeMapper mapper =
                AccountTypeMapper.builder().put(AccountTypes.CHECKING, "checking_account").build();
        assertFalse(mapper.isOneOf("other_type", Collections.singletonList(AccountTypes.CHECKING)));
    }

    @Test
    public void isOf_isOfGivenType() {
        String checking_account = "checking_account";
        final AccountTypeMapper mapper =
                AccountTypeMapper.builder()
                        .put(AccountTypes.CHECKING, checking_account, "other_checking_account")
                        .build();
        assertTrue(mapper.isOf(checking_account, AccountTypes.CHECKING));
    }

    @Test
    public void isOf_isOfDifferentType() {
        String checking_account = "not_checking_account";
        final AccountTypeMapper mapper =
                AccountTypeMapper.builder()
                        .put(AccountTypes.CHECKING, "checking_account")
                        .put(AccountTypes.MORTGAGE, checking_account)
                        .build();
        assertFalse(mapper.isOf(checking_account, AccountTypes.CHECKING));
    }

    @Test
    public void isOf_isNotOfType() {
        final AccountTypeMapper mapper =
                AccountTypeMapper.builder().put(AccountTypes.CHECKING, "checking_account").build();
        assertFalse(mapper.isOf("other_type", AccountTypes.CHECKING));
    }

    @Test
    public void setDefaultReturnValue_noDefaultValueSet() {
        final AccountTypeMapper mapper =
                AccountTypeMapper.builder()
                        .put(AccountTypes.CHECKING, "a")
                        .put(AccountTypes.CREDIT_CARD, "b", "c")
                        .build();

        assertEquals(Optional.empty(), mapper.translate("z"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void duplicateMapping() {
        final AccountTypeMapper mapper =
                AccountTypeMapper.builder()
                        .put(AccountTypes.CHECKING, AccountFlag.PSD2_PAYMENT_ACCOUNT, "aA")
                        .put(
                                AccountTypes.CHECKING,
                                Lists.newArrayList(
                                        AccountFlag.PSD2_PAYMENT_ACCOUNT, AccountFlag.BUSINESS),
                                "aA")
                        .put(AccountTypes.CREDIT_CARD, "b", "c")
                        .build();
    }

    @Test(expected = IllegalStateException.class)
    public void ignoredAndMapped() {
        final AccountTypeMapper mapper =
                AccountTypeMapper.builder()
                        .put(AccountTypes.CHECKING, AccountFlag.PSD2_PAYMENT_ACCOUNT, "aA")
                        .ignoreKeys("aa")
                        .build();
    }

    @Test
    public void testGetItems() {
        final AccountTypeMapper mapper =
                AccountTypeMapper.builder()
                        .put(AccountTypes.CHECKING, AccountFlag.PSD2_PAYMENT_ACCOUNT, "aA")
                        .put(
                                AccountTypes.CHECKING,
                                Lists.newArrayList(
                                        AccountFlag.PSD2_PAYMENT_ACCOUNT, AccountFlag.BUSINESS),
                                "AZ")
                        .put(AccountTypes.CREDIT_CARD, "b", "c")
                        .put(AccountTypes.CHECKING, "d")
                        .build();

        assertEquals(Collections.emptyList(), mapper.getItems("b"));
        assertEquals(Lists.newArrayList(AccountFlag.PSD2_PAYMENT_ACCOUNT), mapper.getItems("AA"));
        assertEquals(Collections.emptyList(), mapper.getItems("d"));
        assertEquals(Optional.of(AccountTypes.CHECKING), mapper.translate("d"));
        assertEquals(
                Lists.newArrayList(AccountFlag.PSD2_PAYMENT_ACCOUNT, AccountFlag.BUSINESS),
                mapper.getItems("aZ"));
    }
}
