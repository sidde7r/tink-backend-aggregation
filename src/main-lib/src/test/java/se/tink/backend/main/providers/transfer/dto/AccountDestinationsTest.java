package se.tink.backend.main.providers.transfer.dto;

import java.util.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.junit.Test;
import se.tink.backend.core.Account;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.account.TransferDestinationPattern;
import static org.assertj.core.api.Assertions.assertThat;

public class AccountDestinationsTest {
    private static final UUID ACC_UUID = UUID.fromString("e7f8d424-87e9-440f-adeb-f9f26c182954");

    @Test
    public void testAccountDestinations_ConstructionRemovesItsOwnAccount() {
        Account account = createAccount();
        Iterable<Destination> destinations = createDestinations(account, true);
        Optional<List<TransferDestinationPattern>> patterns = Optional.empty();

        AccountDestinations accountDestinations = new AccountDestinations(account, destinations, patterns);

        List<Destination> filteredDestinations = Lists.newArrayList(accountDestinations.getDestinations());

        assertThat(filteredDestinations.size()).isEqualTo(2);
    }

    @Test
    public void testAccountDestinations_RemovingDestinationsWithoutIdentifiers() {
        Account account = createAccount();
        Iterable<Destination> destinations = createDestinationsWithoutIdentifiers(account);
        Optional<List<TransferDestinationPattern>> patterns = Optional.empty();

        AccountDestinations accountDestinations = new AccountDestinations(account, destinations, patterns);
        accountDestinations.removeDestinationsWithoutIdentifiers();

        List<Destination> filteredDestinations = Lists.newArrayList(accountDestinations.getDestinations());
        assertThat(filteredDestinations).isEmpty();
    }

    @Test
    public void testAccountDestinations_PatternFiltering_WithoutAnyFilters() {
        Account account = createAccount();
        Iterable<Destination> destinations = createDestinations(account, false);
        Optional<List<TransferDestinationPattern>> patterns = Optional.empty();

        AccountDestinations accountDestinations = new AccountDestinations(account, destinations, patterns);
        accountDestinations.removeDestinationsNotMatchingPatterns();

        List<Destination> filteredDestinations = Lists.newArrayList(accountDestinations.getDestinations());

        assertThat(filteredDestinations).hasSameElementsAs(destinations);
    }

    @Test
    public void testAccountDestinations_PatternFiltering_WithFilters_ExactMatch() {
        Account account = createAccount();
        Iterable<Destination> destinations = createDestinations(account, false);
        List<TransferDestinationPattern> patterns = createPatterns(false);

        AccountDestinations accountDestinations = new AccountDestinations(account, destinations, Optional.of(patterns));
        accountDestinations.removeDestinationsNotMatchingPatterns();

        List<Destination> filteredDestinations = Lists.newArrayList(accountDestinations.getDestinations());

        assertThat(filteredDestinations.size()).isEqualTo(1);
        assertThat(filteredDestinations.get(0).getIdentifiers().size()).isEqualTo(1);
        assertThat(filteredDestinations.get(0).getPrimaryIdentifier().get().toUriAsString()).isEqualTo("se://33008401141935");
    }

    @Test
    public void testAccountDestinations_PatternFiltering_WithFilters_MultiMatch() {
        Account account = createAccount();
        Iterable<Destination> destinations = createDestinations(account, false);
        List<TransferDestinationPattern> patterns = createPatterns(true);

        AccountDestinations accountDestinations = new AccountDestinations(account, destinations, Optional.of(patterns));
        accountDestinations.removeDestinationsNotMatchingPatterns();

        List<Destination> filteredDestinations = Lists.newArrayList(accountDestinations.getDestinations());

        assertThat(filteredDestinations.size()).isEqualTo(2);
        assertThat(Iterables.any(filteredDestinations, destination -> destination.getIdentifiers().size() == 1 &&
                Objects.equals(destination.getPrimaryIdentifier().get().toUriAsString(), "se://5000113355"))).isTrue();
        assertThat(Iterables.any(filteredDestinations, destination -> destination.getIdentifiers().size() == 1 &&
                Objects.equals(destination.getPrimaryIdentifier().get().toUriAsString(), "se://33008401141935"))).isTrue();
    }

    @Test
    public void testAccountDestinations_Account() {
        Account account = createAccount();
        Iterable<Destination> destinations = Lists.newArrayList();
        Optional<List<TransferDestinationPattern>> patterns = Optional.empty();

        AccountDestinations accountDestinations = new AccountDestinations(account, destinations, patterns);

        Account resultingAccount = accountDestinations.getAccount();
        assertThat(resultingAccount.getId()).isEqualTo(ACC_UUID.toString());
    }

    private Account createAccount() {
        Account account = new Account();
        account.setAccountNumber("1234 1234 1234");
        account.setBalance(1.0);
        account.setId(ACC_UUID.toString());
        account.setName("The name");
        account.setCredentialsId("TheCred");
        account.setType(AccountTypes.SAVINGS);
        account.putIdentifier(AccountIdentifier.create(AccountIdentifier.Type.SE, "33008607015537"));
        account.putIdentifier(
                AccountIdentifier.create(AccountIdentifier.Type.IBAN, "NDEASESS/SE4430000000008607015537"));

        return account;
    }

    private List<TransferDestinationPattern> createPatterns(boolean withWildcardForACC_UUID) {
        List<TransferDestinationPattern> patterns = Lists.newArrayList();

        TransferDestinationPattern exactMatch = new TransferDestinationPattern();
        exactMatch.setAccountId(ACC_UUID);
        exactMatch.setBank("Nordea Bank");
        exactMatch.setMatchesMultiple(true);
        exactMatch.setPattern("33008401141935");
        exactMatch.setType(AccountIdentifier.Type.SE);
        exactMatch.setUserId(UUID.randomUUID());
        patterns.add(exactMatch);

        if (withWildcardForACC_UUID) {
            TransferDestinationPattern multiMatch = new TransferDestinationPattern();
            multiMatch.setAccountId(ACC_UUID);
            multiMatch.setBank("Nordea Bank");
            multiMatch.setMatchesMultiple(true);
            multiMatch.setPattern(".+");
            multiMatch.setType(AccountIdentifier.Type.SE);
            multiMatch.setUserId(UUID.randomUUID());
            patterns.add(multiMatch);
        }

        return patterns;
    }

    private static Iterable<Destination> createDestinations(Account account, boolean includeAccountsDestination) {
        List<Destination> destinations = Lists.newArrayList();
        if (includeAccountsDestination) {
            destinations.add(createDestination(account));
        }

        DestinationOfUserTransferDestination destination2 = new DestinationOfUserTransferDestination(createIdentifier2());
        destination2.setName("My SEB");
        destinations.add(destination2);

        DestinationOfPattern destination3 = new DestinationOfPattern(createIdentifier3());
        destination3.setBank("Nordea");
        destination3.setPatternAccountId(UUID.fromString(account.getId()));
        destination3.setName("My Nordea");
        destinations.add(destination3);

        return destinations;
    }

    private Iterable<Destination> createDestinationsWithoutIdentifiers(Account account) {
        List<Destination> destinations = Lists.newArrayList();
        DestinationOfUserTransferDestination destination2 = new DestinationOfUserTransferDestination(null);
        destination2.setName("My SEB");
        destinations.add(destination2);

        DestinationOfPattern destination3 = new DestinationOfPattern(null);
        destination3.setBank("Nordea");
        destination3.setPatternAccountId(UUID.fromString(account.getId()));
        destination3.setName("My Nordea");
        destinations.add(destination3);

        return destinations;
    }

    private static Destination createDestination(Account account) {
        DestinationOfAccount destination = new DestinationOfAccount(account.getIdentifiers());
        destination.setBalance(1.0);
        destination.setCredentialsId("cred1");
        destination.setType(AccountTypes.SAVINGS);
        destination.setName(account.getName());
        return destination;
    }

    private static AccountIdentifier createIdentifier2() {
        return AccountIdentifier.create(AccountIdentifier.Type.SE, "5000113355");
    }

    private static AccountIdentifier createIdentifier3() {
        return AccountIdentifier.create(AccountIdentifier.Type.SE, "33008401141935");
    }
}
