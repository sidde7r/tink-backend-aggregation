package se.tink.backend.integration.agent_data_availability_tracker.serialization;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountHolder;
import se.tink.backend.agents.rpc.AccountHolderType;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.HolderIdentity;
import se.tink.backend.agents.rpc.HolderRole;
import se.tink.backend.integration.agent_data_availability_tracker.common.serialization.FieldEntry;
import se.tink.backend.integration.agent_data_availability_tracker.common.serialization.TrackingList;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.enums.AccountIdentifierType;

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
        List<FieldEntry> entries = new AccountTrackingSerializer(account, 0).buildList();

        Assert.assertTrue(
                "Failed: all entries in secretFieldKeys set is unlisted",
                TrackingSerializationTestHelper.allFieldsAreTracked(secretFieldKeys, entries));
    }

    @Test
    public void ensureAccountHoldersAreTracked() {

        ImmutableSet<String> secretFieldKeys =
                ImmutableSet.<String>builder()
                        .add(
                                "Account<null>.accountHolder<BUSINESS>.identities<AUTHORIZED_USER>.name",
                                "Account<null>.accountHolder<BUSINESS>.identities",
                                "Account<null>.accountHolder<BUSINESS>.accountId",
                                "Account<null>.accountHolder")
                        .build();

        Account account = new Account();
        AccountHolder accountHolder = new AccountHolder();
        List<HolderIdentity> identities = new ArrayList<>();
        HolderIdentity holderIdentity = new HolderIdentity();
        holderIdentity.setName("dummy-name");
        holderIdentity.setRole(HolderRole.AUTHORIZED_USER);
        accountHolder.setAccountId("dummy-accountId");
        accountHolder.setType(AccountHolderType.BUSINESS);
        identities.add(holderIdentity);
        accountHolder.setIdentities(identities);
        account.setAccountHolder(accountHolder);

        List<FieldEntry> entries = new AccountTrackingSerializer(account, 0).buildList();

        Assert.assertTrue(
                TrackingSerializationTestHelper.allFieldsAreTracked(secretFieldKeys, entries));
    }

    @Test
    public void ensureAccountType_isTracked() {

        Account account = new Account();
        account.setType(AccountTypes.CHECKING);

        List<FieldEntry> entries = new AccountTrackingSerializer(account, 0).buildList();

        Assert.assertTrue(
                "Failed: has entry 'Account<CHECKING>.type' with value == VALUE_NOT_LISTED",
                TrackingSerializationTestHelper.hasFieldWithValue(
                        "Account<CHECKING>.type", VALUE_NOT_LISTED, entries));
    }

    @Test
    public void ensureIdentifierTypes_areTracked() {

        Account account = new Account();

        account.putIdentifier(AccountIdentifier.create(AccountIdentifierType.IBAN, EXAMPLE_IBAN));
        account.putIdentifier(
                AccountIdentifier.create(AccountIdentifierType.SORT_CODE, EXAMPLE_SORTCODE));

        List<FieldEntry> entries = new AccountTrackingSerializer(account, 0).buildList();

        Assert.assertTrue(
                "Failed: has entries 'identifiers.iban' with non-listed-value",
                TrackingSerializationTestHelper.hasFieldWithValue(
                        "Account<null>.identifiers.iban", VALUE_NOT_LISTED, entries));

        Assert.assertTrue(
                "Failed: has entries 'identifiers.sort-code' with non-listed-value",
                TrackingSerializationTestHelper.hasFieldWithValue(
                        "Account<null>.identifiers.iban", VALUE_NOT_LISTED, entries));
    }

    @Test
    public void ensureIbanIdentifierIsAlwaysTracked() {

        Account account = new Account();

        // this account has no iban
        account.putIdentifier(
                AccountIdentifier.create(AccountIdentifierType.SORT_CODE, EXAMPLE_SORTCODE));

        List<FieldEntry> entries = new AccountTrackingSerializer(account, 0).buildList();

        Assert.assertTrue(
                "Failed: should have null 'identifiers.iban'",
                TrackingSerializationTestHelper.hasFieldWithValue(
                        "Account<null>.identifiers.iban", "null", entries));
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

        List<FieldEntry> entries = new AccountTrackingSerializer(account, 0).buildList();

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

        List<FieldEntry> entries = new AccountTrackingSerializer(account, 0).buildList();

        Assert.assertTrue(
                "Failed: all entries in secretFieldKeys set is unlisted",
                TrackingSerializationTestHelper.allFieldsAreTracked(unlistedFieldKeys, entries));
    }
}
