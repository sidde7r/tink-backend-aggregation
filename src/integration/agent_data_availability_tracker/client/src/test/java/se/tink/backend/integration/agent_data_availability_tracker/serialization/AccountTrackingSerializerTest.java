package se.tink.backend.integration.agent_data_availability_tracker.serialization;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountFlag;

public class AccountTrackingSerializerTest {

    private static final String SECRET_VALUE = "SecretValue";
    private static final String EXAMPLE_IBAN = "DE89370400440532013000";
    private static final String EXAMPLE_SORTCODE = "40478470872490";
    private static final String VALUE_NOT_LISTED = TrackingList.Builder.VALUE_NOT_LISTED;

    @Test
    public void ensureSecretAccountFields_areRedacted() {

        ImmutableSet<String> secretFieldKeys =
                ImmutableSet.<String>builder()
                        .add(
                                "Account<null>.accountNumber",
                                "Account<null>.bankId",
                                "Account<null>.name",
                                "Account<null>.holderName")
                        .build();

        Account account = new Account();
        account.setAccountNumber(SECRET_VALUE);
        account.setName(SECRET_VALUE);
        account.setBankId(SECRET_VALUE);
        account.setHolderName(SECRET_VALUE);

        List<FieldEntry> entries = new AccountTrackingSerializer(account).buildList();

        Assert.assertTrue(
                "Failed: all entries in secretFieldKeys set is unlisted",
                TrackingSerializationTestHelper.isAllUnlisted(secretFieldKeys, entries));
    }

    @Test
    public void ensureAccountType_isTracked_andIncludedInKey() {

        Account account = new Account();
        account.setType(AccountTypes.CHECKING);

        List<FieldEntry> entries = new AccountTrackingSerializer(account).buildList();

        Assert.assertTrue(
                "Failed: has entry 'Account<CHECKING>.type' with value == CHECKING",
                TrackingSerializationTestHelper.hasFieldWithValue(
                        "Account<CHECKING>.type", AccountTypes.CHECKING.toString(), entries));
    }

    @Test
    public void ensureIdentifierTypes_areTracked() {

        ImmutableSet<String> expectedIdentifiers =
                ImmutableSet.<String>builder()
                        .add(
                                AccountIdentifier.Type.SORT_CODE.toString(),
                                AccountIdentifier.Type.IBAN.toString())
                        .build();

        Account account = new Account();

        account.putIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, EXAMPLE_IBAN));
        account.putIdentifier(
                AccountIdentifier.create(AccountIdentifier.Type.SORT_CODE, EXAMPLE_SORTCODE));

        List<FieldEntry> entries = new AccountTrackingSerializer(account).buildList();

        Assert.assertTrue(
                "Failed: has entries 'identifiers' with values [iban, sort-code]",
                TrackingSerializationTestHelper.hasFieldWithValues(
                        "Account<null>.identifiers", expectedIdentifiers, entries));
    }

    @Test
    public void ensureEmptyIdentifiersList_resultInNullValue() {

        ImmutableSet<String> expectedIdentifiers =
                ImmutableSet.<String>builder().add("null").build();

        Account account = new Account();
        List<FieldEntry> entries = new AccountTrackingSerializer(account).buildList();

        Assert.assertTrue(
                "Failed: has entries 'identifiers' with values [null]",
                TrackingSerializationTestHelper.hasFieldWithValues(
                        "Account<null>.identifiers", expectedIdentifiers, entries));
    }

    @Test
    public void ensureFlags_areTracked() {

        ImmutableSet<String> expectedFlags =
                ImmutableSet.<String>builder()
                        .add(AccountFlag.BUSINESS.toString(), AccountFlag.MANDATE.toString())
                        .build();

        Account account = new Account();
        account.putFlag(AccountFlag.BUSINESS);
        account.putFlag(AccountFlag.MANDATE);

        List<FieldEntry> entries = new AccountTrackingSerializer(account).buildList();

        Assert.assertTrue(
                "Failed: has entries 'flags' with values [business, mandate]",
                TrackingSerializationTestHelper.hasFieldWithValues(
                        "Account<null>.flags", expectedFlags, entries));
    }

    @Test
    public void ensureFieldsWith_infinitePossibleValues_areNotTracked() {
        ImmutableSet<String> unlistedFieldKeys =
                ImmutableSet.<String>builder()
                        .add("Account<null>.balance", "Account<null>.availableCredit")
                        .build();

        Account account = new Account();
        account.setBalance(100.0);
        account.setAvailableCredit(100.0);

        List<FieldEntry> entries = new AccountTrackingSerializer(account).buildList();

        Assert.assertTrue(
                "Failed: all entries in secretFieldKeys set is unlisted",
                TrackingSerializationTestHelper.isAllUnlisted(unlistedFieldKeys, entries));
    }
}
