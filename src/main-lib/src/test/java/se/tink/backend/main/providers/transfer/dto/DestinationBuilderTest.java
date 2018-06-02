package se.tink.backend.main.providers.transfer.dto;

import java.util.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import se.tink.backend.core.Account;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.backend.core.account.UserTransferDestination;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;
import se.tink.backend.main.providers.transfer.dto.utils.DestinationBuilder;
import static org.assertj.core.api.Assertions.assertThat;

public class DestinationBuilderTest {
    @Test
    public void testWithAccounts() {
        Iterable<Account> accounts = createAccounts();

        Iterable<Destination> destinations = DestinationBuilder
                .create()
                .withAccounts(accounts)
                .build();

        List<Destination> destinationList = Lists.newArrayList(destinations);

        assertThat(destinationList.size()).isEqualTo(2);

        assertThat(destinationList.get(0).is(DestinationOfAccount.class)).isTrue();

        DestinationOfAccount destinationOfAccount = destinationList.get(0).to(DestinationOfAccount.class);
        assertThat(destinationOfAccount.getIdentifiers().size()).isEqualTo(1);
        assertThat(destinationOfAccount.getIdentifiers().get(0))
                .isEqualTo(AccountIdentifier.create(AccountIdentifier.Type.SE, "1200112233"));
        assertThat(destinationOfAccount.getDisplayIdentifier().get().getIdentifier(new DisplayAccountIdentifierFormatter())).isEqualTo("1200-112233");
        assertThat(destinationOfAccount.getName().get()).isEqualTo("The name");
        assertThat(destinationOfAccount.getBalance()).isEqualTo(1.0);
        assertThat(destinationOfAccount.getCredentialsId()).isEqualTo("TheCred");
        assertThat(destinationOfAccount.getType()).isEqualTo(AccountTypes.SAVINGS);
    }

    @Test
    public void testWithPatterns() {
        ListMultimap<String, TransferDestinationPattern> patternsByAccount = createPatterns();

        Iterable<Destination> destinations = DestinationBuilder
                .create()
                .withAccountsPatterns(patternsByAccount)
                .build();

        List<Destination> destinationList = Lists.newArrayList(destinations);

        assertThat(destinationList.size()).isEqualTo(1);

        assertThat(destinationList.get(0).is(DestinationOfPattern.class)).isTrue();

        DestinationOfPattern destinationOfPattern = destinationList.get(0).to(DestinationOfPattern.class);
        assertThat(destinationOfPattern.getIdentifiers().size()).isEqualTo(1);
        assertThat(destinationOfPattern.getIdentifiers().get(0))
                .isEqualTo(AccountIdentifier.create(AccountIdentifier.Type.SE, "1200112233"));
        assertThat(destinationOfPattern.getDisplayIdentifier().get().getIdentifier(new DisplayAccountIdentifierFormatter())).isEqualTo("1200-112233");
        assertThat(destinationOfPattern.getName().get()).isEqualTo("SEB Name1");
        assertThat(destinationOfPattern.getBank()).isEqualTo("SEB Bank");
        assertThat(destinationOfPattern.getType()).isEqualTo(AccountTypes.EXTERNAL);
    }

    @Test
    public void testWithUserDestinations() {
        List<UserTransferDestination> userDestinations = createUserTransferDestinations();

        Iterable<Destination> destinations = DestinationBuilder
                .create()
                .withDestinations(userDestinations)
                .build();

        List<Destination> destinationList = Lists.newArrayList(destinations);

        assertThat(destinationList.size()).isEqualTo(5);

        assertThat(destinationList.get(0).is(DestinationOfUserTransferDestination.class)).isTrue();

        DestinationOfUserTransferDestination destinationOfPattern =
                destinationList.get(0).to(DestinationOfUserTransferDestination.class);
        assertThat(destinationOfPattern.getIdentifiers().size()).isEqualTo(1);
        assertThat(destinationOfPattern.getIdentifiers().get(0))
                .isEqualTo(AccountIdentifier.create(AccountIdentifier.Type.SE, "50001112222"));
        assertThat(destinationOfPattern.getDisplayIdentifier().get().getIdentifier(new DisplayAccountIdentifierFormatter())).isEqualTo("5000-1112222");
        assertThat(destinationOfPattern.getName().get()).isEqualTo("SEB Name1");
        assertThat(destinationOfPattern.getDisplayBankName().get()).isEqualTo("SEB");
        assertThat(destinationOfPattern.getType()).isEqualTo(AccountTypes.EXTERNAL);
    }

    @Test
    public void testIdentifierFilter() {
        final List<Account> accounts = createAccounts();
        final ListMultimap<String, TransferDestinationPattern> patternsByAccount = createPatterns();

        Optional<ImmutableSet<AccountIdentifier>> allowedIdentifiers = Optional.of(ImmutableSet.of(
                AccountIdentifier.create(URI.create("se://5000112200")),
                AccountIdentifier.create(URI.create("se://5000112299"))
        ));

        Iterable<Destination> destinations = DestinationBuilder
                .create()
                .withAccounts(accounts)
                .withAccountsPatterns(patternsByAccount)
                .filterExplicitIdentifiersIfPresent(allowedIdentifiers)
                .build();

        List<Destination> destinationList = Lists.newArrayList(destinations);

        assertThat(destinationList.size()).isEqualTo(1);
    }

    @Test
    public void testDeDuplicate() {
        final List<Account> accounts = createAccounts();
        final ListMultimap<String, TransferDestinationPattern> patternsByAccount = createPatterns();

        Iterable<Destination> destinations = DestinationBuilder
                .create()
                .withAccounts(accounts)
                .withAccountsPatterns(patternsByAccount)
                .removeDuplicates()
                .build();

        List<Destination> destinationList = Lists.newArrayList(destinations);

        assertThat(destinationList.size()).isEqualTo(2);
    }

    @Test
    public void testEmpty() {
        Iterable<Destination> destinations = DestinationBuilder
                .create()
                .removeDuplicates()
                .build();

        List<Destination> destinationList = Lists.newArrayList(destinations);

        assertThat(destinationList.size()).isEqualTo(0);
    }

    @Test
    public void testTypeFilter() {
        final List<Account> accounts = createAccounts();
        List<UserTransferDestination> userTransferDestinations = createUserTransferDestinations();
        final ListMultimap<String, TransferDestinationPattern> patternsByAccount = createPatterns();

        Optional<ImmutableSet<AccountIdentifier.Type>> allowedTypes = Optional.of(ImmutableSet.of(
                AccountIdentifier.Type.SE_BG, AccountIdentifier.Type.SE_PG
        ));

        Iterable<Destination> destinations = DestinationBuilder
                .create()
                .withAccounts(accounts)
                .withAccountsPatterns(patternsByAccount)
                .withDestinations(userTransferDestinations)
                .filterExplicitTypesIfPresent(allowedTypes)
                .build();

        List<Destination> destinationList = Lists.newArrayList(destinations);

        assertThat(destinationList.size()).isEqualTo(2);
        for (Destination destination : destinationList) {
            for (AccountIdentifier identifier : destination.getIdentifiers()) {
                assertThat(identifier.getType()).isIn(AccountIdentifier.Type.SE_BG, AccountIdentifier.Type.SE_PG);
            }
        }
    }

    private List<UserTransferDestination> createUserTransferDestinations() {
        UserTransferDestination destination1 = new UserTransferDestination();
        destination1.setIdentifier("50001112222");
        destination1.setName("SEB Name1");
        destination1.setType(AccountIdentifier.Type.SE);
        destination1.setUserId(UUID.randomUUID());

        UserTransferDestination destination2 = new UserTransferDestination();
        destination2.setIdentifier("5000123123");
        destination2.setName("SEB Name2");
        destination2.setType(AccountIdentifier.Type.SE);
        destination2.setUserId(UUID.randomUUID());

        UserTransferDestination destination3 = new UserTransferDestination();
        destination3.setIdentifier("1200112233");
        destination3.setName("Danske Name1");
        destination3.setType(AccountIdentifier.Type.SE);
        destination3.setUserId(UUID.randomUUID());

        UserTransferDestination destination4 = new UserTransferDestination();
        destination4.setIdentifier("1234-5678");
        destination4.setName("PG Name");
        destination4.setType(AccountIdentifier.Type.SE_PG);
        destination4.setUserId(UUID.randomUUID());

        UserTransferDestination destination5 = new UserTransferDestination();
        destination5.setIdentifier("4321-8765");
        destination5.setName("BG Name");
        destination5.setType(AccountIdentifier.Type.SE_BG);
        destination5.setUserId(UUID.randomUUID());

        return ImmutableList.of(destination1, destination2, destination3, destination4, destination5);
    }

    private ListMultimap<String, TransferDestinationPattern> createPatterns() {
        TransferDestinationPattern pattern1 = new TransferDestinationPattern();
        pattern1.setAccountId(UUID.randomUUID());
        pattern1.setBank("SEB Bank");
        pattern1.setMatchesMultiple(true);
        pattern1.setPattern(".+");
        pattern1.setType(AccountIdentifier.Type.SE);
        pattern1.setUserId(UUID.randomUUID());

        TransferDestinationPattern pattern2 = new TransferDestinationPattern();
        pattern2.setAccountId(UUID.randomUUID());
        pattern2.setBank("SEB Bank");
        pattern2.setMatchesMultiple(false);
        pattern2.setName("SEB Name1");
        pattern2.setPattern("1200112233");
        pattern2.setType(AccountIdentifier.Type.SE);
        pattern2.setUserId(UUID.randomUUID());

        TransferDestinationPattern pattern3 = new TransferDestinationPattern();
        pattern3.setAccountId(UUID.randomUUID());
        pattern3.setBank("Danske Bank");
        pattern3.setMatchesMultiple(true);
        pattern3.setPattern(".+");
        pattern3.setType(AccountIdentifier.Type.SE);
        pattern3.setUserId(UUID.randomUUID());

        ImmutableList<TransferDestinationPattern> patterns = ImmutableList.of(pattern1, pattern2, pattern3);
        return FluentIterable.from(patterns)
                .index(transferDestinationPattern -> transferDestinationPattern.getAccountId().toString());
    }

    public static List<Account> createAccounts() {
        Account account1 = new Account();
        account1.setAccountNumber("1234 1234 1234");
        account1.setBalance(1.0);
        account1.setId("1234");
        account1.setName("The name");
        account1.setCredentialsId("TheCred");
        account1.setType(AccountTypes.SAVINGS);
        account1.putIdentifier(AccountIdentifier.create(AccountIdentifier.Type.SE, "1200112233"));

        Account account2 = new Account();
        account2.setAccountNumber("1234 1111 1234");
        account2.setBalance(2.0);
        account2.setId("4556");
        account2.setName("Other name");
        account2.setType(AccountTypes.LOAN);
        account2.putIdentifier(AccountIdentifier.create(AccountIdentifier.Type.SE, "5000112200"));

        return ImmutableList.of(account1, account2);
    }
}
