package se.tink.backend.main.providers.transfer.dto;

import com.google.common.collect.Lists;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import se.tink.backend.core.Account;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.SwedishSHBInternalIdentifier;
import static org.assertj.core.api.Assertions.assertThat;

public class DestinationTest {
    private static final UUID ACC_UUID = UUID.fromString("e7f8d424-87e9-440f-adeb-f9f26c182954");
    private static final UUID OTHER_UUID = UUID.fromString("abcdef12-87e9-440f-adeb-f9f26c182954");

    @Test
    public void testDestinationIdentifierExclusive() {
        List<Account> accounts = createAccounts();
        Destination destination = createDestination(accounts.get(0));

        List<AccountIdentifier> identifiers = destination.getIdentifiers();
        assertThat(identifiers.size()).isEqualTo(2);

        destination.setIdentifierExclusive(accounts.get(0).getIdentifiers().get(0));
        identifiers = destination.getIdentifiers();
        assertThat(identifiers.size()).isEqualTo(1);
    }

    @Test
    public void testDestinationFilterNoMatches() {
        List<Account> accounts = createAccounts();
        Destination destination = createDestination(accounts.get(0));
        ListMultimap<String, TransferDestinationPattern> accountPatterns = createPatterns(false, false);

        destination.filterIdentifiersWithPatterns(accountPatterns.get(ACC_UUID.toString()));

        assertThat(destination.getIdentifiers().size()).isEqualTo(0);
        assertThat(destination.getPrimaryIdentifier().isPresent()).isFalse();
        assertThat(destination.getDisplayIdentifier().isPresent()).isFalse();
    }

    @Test
    public void testDestinationFilterWithoutWildcard() {
        List<Account> accounts = createAccounts();
        Destination destination = createDestination(accounts.get(0));
        ListMultimap<String, TransferDestinationPattern> accountPatterns = createPatterns(true, false);

        destination.filterIdentifiersWithPatterns(accountPatterns.get(ACC_UUID.toString()));

        assertThat(destination.getIdentifiers().size()).isEqualTo(1);
        assertThat(destination.getPrimaryIdentifier().get().toUriAsString()).isEqualTo("se://33008607015537");
        assertThat(destination.getDisplayIdentifier().get().toUriAsString()).isEqualTo("se://33008607015537");
    }

    @Test
    public void testDestinationFilterWithWildcard() {
        List<Account> accounts = createAccounts();
        Destination destination = createDestination(accounts.get(0));
        ListMultimap<String, TransferDestinationPattern> accountPatterns = createPatterns(false, true);

        destination.filterIdentifiersWithPatterns(accountPatterns.get(ACC_UUID.toString()));

        // Only matches for swedish identifiers, see creation
        assertThat(destination.getIdentifiers().size()).isEqualTo(1);
        assertThat(destination.getPrimaryIdentifier().get().toUriAsString()).isEqualTo("se://33008607015537");
        assertThat(destination.getDisplayIdentifier().get().toUriAsString()).isEqualTo("se://33008607015537");
    }

    @Test
    public void testDestinationFilterWithWildcardAndExactMatch() {
        List<Account> accounts = createAccounts();
        Destination destination = createDestination(accounts.get(0));
        ListMultimap<String, TransferDestinationPattern> accountPatterns = createPatterns(true, true);

        destination.filterIdentifiersWithPatterns(accountPatterns.get(ACC_UUID.toString()));

        assertThat(destination.getIdentifiers().size()).isEqualTo(1);
        assertThat(destination.getPrimaryIdentifier().get().toUriAsString()).isEqualTo("se://33008607015537");
        assertThat(destination.getDisplayIdentifier().get().toUriAsString()).isEqualTo("se://33008607015537");
    }

    @Test
    public void testSelectsSwedishIdentifierBeforeSHBInternal() {
        SwedishIdentifier seIdentifier = new SwedishIdentifier("6769952279428");
        SwedishSHBInternalIdentifier seSHBInternalIdentifier = new SwedishSHBInternalIdentifier("952279428");

        Account accounts = createBaseAccount();
        accounts.putIdentifier(seSHBInternalIdentifier);
        accounts.putIdentifier(seIdentifier);

        Destination destination = createDestination(accounts);
        ListMultimap<String, TransferDestinationPattern> accountPatterns = createPatterns(true, true);

        destination.filterIdentifiersWithPatterns(accountPatterns.get(ACC_UUID.toString()));

        assertThat(destination.getIdentifiers().size()).isEqualTo(2);
        assertThat(destination.getPrimaryIdentifier().get().toUriAsString()).isEqualTo("se://6769952279428");
        assertThat(destination.getDisplayIdentifier().get().toUriAsString()).isEqualTo("se://6769952279428");
    }

    @Test
    public void testSelectsSHBInternalIdentifierIfNoOtherIsFound() {
        SwedishSHBInternalIdentifier seSHBInternalIdentifier = new SwedishSHBInternalIdentifier("952279428");

        Account accounts = createBaseAccount();
        accounts.putIdentifier(seSHBInternalIdentifier);

        Destination destination = createDestination(accounts);
        ListMultimap<String, TransferDestinationPattern> accountPatterns = createPatterns(true, true);

        destination.filterIdentifiersWithPatterns(accountPatterns.get(ACC_UUID.toString()));

        assertThat(destination.getIdentifiers().size()).isEqualTo(1);
        assertThat(destination.getPrimaryIdentifier().get().toUriAsString()).isEqualTo("se-internal://952279428");
    }

    @Test
    public void testDestinationTypeFilter() {
        List<Account> accounts = createAccounts();
        Account firstAccount = accounts.get(0);

        // Add a PG identifier for the test setup and create destination from the account
        AccountIdentifier pgIdentifier = AccountIdentifier.create(AccountIdentifier.Type.SE_PG, "1234-5678");
        firstAccount.putIdentifier(pgIdentifier);
        Destination destination = createDestination(firstAccount);

        // Check that initial state is as expected
        List<AccountIdentifier> identifiers = destination.getIdentifiers();
        assertThat(identifiers.size()).isEqualTo(3);

        // Filter types
        ImmutableSet<AccountIdentifier.Type> typesExclusive = ImmutableSet
                .of(AccountIdentifier.Type.SE_BG, AccountIdentifier.Type.SE_PG);
        destination.setTypesExclusive(typesExclusive);

        // Only PG identifier should be left now
        identifiers = destination.getIdentifiers();
        assertThat(identifiers.size()).isEqualTo(1);
        assertThat(identifiers.get(0)).isEqualTo(pgIdentifier);
    }

    private ListMultimap<String, TransferDestinationPattern> createPatterns(boolean withExactMatchForACC_UUID, boolean withWildcardForACC_UUID) {
        List<TransferDestinationPattern> patterns = Lists.newArrayList();

        TransferDestinationPattern pattern1 = new TransferDestinationPattern();
        pattern1.setAccountId(OTHER_UUID);
        pattern1.setBank("SEB Bank");
        pattern1.setMatchesMultiple(true);
        pattern1.setPattern(".+");
        pattern1.setType(AccountIdentifier.Type.SE);
        pattern1.setUserId(UUID.randomUUID());
        patterns.add(pattern1);

        TransferDestinationPattern pattern2 = new TransferDestinationPattern();
        pattern2.setAccountId(OTHER_UUID);
        pattern2.setBank("SEB Bank");
        pattern2.setMatchesMultiple(false);
        pattern2.setName("SEB Name1");
        pattern2.setPattern("1200112233");
        pattern2.setType(AccountIdentifier.Type.SE);
        pattern2.setUserId(UUID.randomUUID());
        patterns.add(pattern2);

        if (withWildcardForACC_UUID) {
            TransferDestinationPattern pattern3 = new TransferDestinationPattern();
            pattern3.setAccountId(ACC_UUID);
            pattern3.setBank("Nordea Bank");
            pattern3.setMatchesMultiple(true);
            pattern3.setPattern(".+");
            pattern3.setType(AccountIdentifier.Type.SE);
            pattern3.setUserId(UUID.randomUUID());
            patterns.add(pattern3);

            TransferDestinationPattern pattern4 = new TransferDestinationPattern();
            pattern4.setAccountId(ACC_UUID);
            pattern4.setBank("Nordea Bank");
            pattern4.setMatchesMultiple(true);
            pattern4.setPattern(".+");
            pattern4.setType(AccountIdentifier.Type.SE_SHB_INTERNAL);
            pattern4.setUserId(UUID.randomUUID());
            patterns.add(pattern4);
        }

        if (withExactMatchForACC_UUID) {
            TransferDestinationPattern pattern4 = new TransferDestinationPattern();
            pattern4.setAccountId(ACC_UUID);
            pattern4.setBank("Nordea Bank");
            pattern4.setMatchesMultiple(true);
            pattern4.setPattern("33008607015537");
            pattern4.setType(AccountIdentifier.Type.SE);
            pattern4.setUserId(UUID.randomUUID());
            patterns.add(pattern4);
        }

        return FluentIterable.from(patterns)
                .index(transferDestinationPattern -> transferDestinationPattern.getAccountId().toString());
    }

    private static Destination createDestination(Account account) {
        DestinationOfAccount destination = new DestinationOfAccount(account.getIdentifiers());
        destination.setBalance(account.getBalance());
        destination.setCredentialsId(account.getCredentialsId());
        destination.setType(account.getType());
        destination.setName(account.getName());
        return destination;
    }

    private static List<Account> createAccounts() {
        Account account = createBaseAccount();
        account.putIdentifier(AccountIdentifier.create(AccountIdentifier.Type.SE, "33008607015537"));
        account.putIdentifier(
                AccountIdentifier.create(AccountIdentifier.Type.IBAN, "NDEASESS/SE4430000000008607015537"));
        return ImmutableList.of(account);
    }

    private static Account createBaseAccount() {
        Account account1 = new Account();
        account1.setAccountNumber("1234 1234 1234");
        account1.setBalance(1.0);
        account1.setId(ACC_UUID.toString());
        account1.setName("The name");
        account1.setCredentialsId("TheCred");
        account1.setType(AccountTypes.SAVINGS);

        return account1;
    }
}
