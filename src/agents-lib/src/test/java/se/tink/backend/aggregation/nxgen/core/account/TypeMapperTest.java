package se.tink.backend.aggregation.nxgen.core.account;

import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.rpc.AccountTypes;

public final class TypeMapperTest {
    @Test
    public void ensureTranslate_withNothingKnown_returnsEmpty() {
        final TypeMapper<AccountTypes> mapper = TypeMapper.<AccountTypes>builder().build();

        Assert.assertFalse(mapper.translate("CHECKING_ACCOUNT").isPresent());
    }

    @Test
    public void ensureTranslate_withIgnored_returnsEmpty() {
        final TypeMapper<AccountTypes> mapper = TypeMapper.<AccountTypes>builder()
                .ignoreKeys("CHECKING_ACCOUNT")
                .build();

        Assert.assertFalse(mapper.translate("CHECKING_ACCOUNT").isPresent());
    }

    @Test
    public void ensureTranslate_withCheckingStringMappedToChecking_returnsChecking() {
        final TypeMapper<AccountTypes> mapper = TypeMapper.<AccountTypes>builder()
                .put(AccountTypes.CHECKING, "CHECKING_ACCOUNT")
                .build();

        Assert.assertEquals(mapper.translate("CHECKING_ACCOUNT"), Optional.of(AccountTypes.CHECKING));
    }

    @Test
    public void ensureTranslate_withOtherStringMappedToChecking_returnsEmpty() {
        final TypeMapper<AccountTypes> mapper = TypeMapper.<AccountTypes>builder()
                .put(AccountTypes.CHECKING, "SAVINGS_ACCOUNT")
                .build();

        Assert.assertFalse(mapper.translate("CHECKING_ACCOUNT").isPresent());
    }

    @Test
    public void ensureTranslate_withDifferentCapitalisation_returnsChecking() {
        final TypeMapper<AccountTypes> mapper = TypeMapper.<AccountTypes>builder()
                .put(AccountTypes.CHECKING, "checking_account")
                .build();

        Assert.assertEquals(mapper.translate("CHECKING_ACCOUNT"), Optional.of(AccountTypes.CHECKING));
    }

    @Test(expected = IllegalStateException.class)
    public void ensureIgnore_withAlreadyMappedValue_throwsException() {
        final TypeMapper<AccountTypes> mapper = TypeMapper.<AccountTypes>builder()
                .put(AccountTypes.CHECKING, "SAVINGS_ACCOUNT")
                .ignoreKeys("SAVINGS_ACCOUNT")
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void ensureIgnore_withAlreadyMappedValueButDifferingCapitalisation_throwsException() {
        final TypeMapper<AccountTypes> mapper = TypeMapper.<AccountTypes>builder()
                .put(AccountTypes.CHECKING, "SAVINGS_ACCOUNT")
                .ignoreKeys("savings_account")
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureTranslate_withAlreadyMappedValue_throwsException() {
        final TypeMapper<AccountTypes> mapper = TypeMapper.<AccountTypes>builder()
                .put(AccountTypes.CHECKING, "SAVINGS_ACCOUNT")
                .put(AccountTypes.SAVINGS, "SAVINGS_ACCOUNT")
                .build();
    }

}
