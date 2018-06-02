package se.tink.backend.main.providers.transfer.dto;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import se.tink.backend.core.Account;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.backend.main.providers.transfer.dto.AccountDestinations.FromAccountTransform;
import static org.assertj.core.api.Assertions.assertThat;

public class AccountDestinationsFromAccountTransformTest {
    private static final UUID ACC_UUID = UUID.fromString("e7f8d424-87e9-440f-adeb-f9f26c182954");
    private static final UUID ACC_UUID2 = UUID.fromString("abc12324-87e9-440f-adeb-f9f26c182954");

    private static final AccountIdentifier IDENTIFIER_SSN1 = AccountIdentifier
            .create(AccountIdentifier.Type.SE, "33008607015537");
    private static final AccountIdentifier IDENTIFIER_SSN2 = AccountIdentifier
            .create(AccountIdentifier.Type.SE, "33008401141935");
    private static final AccountIdentifier IDENTIFIER_DANSKE = AccountIdentifier
            .create(AccountIdentifier.Type.SE, "1200112233");
    private static final AccountIdentifier IDENTIFIER_IBAN_SN2 = AccountIdentifier
            .create(AccountIdentifier.Type.IBAN, "NDEASESS/SE6730000000008401141935");

    @Test
    public void testFromAccountsTransform() {
        Account account = createAccount();
        ListMultimap<String, TransferDestinationPattern> patterns = createPatterns(true);
        Iterable<Destination> destinations = createDestinations();

        FromAccountTransform transform = new FromAccountTransform(patterns, destinations);
        AccountDestinations accountDestinations = transform.apply(account);

        assertThat(accountDestinations).isNotNull();
        assertThat(accountDestinations.getAccount()).isEqualTo(account);
        assertThat(accountDestinations.getDestinations()).hasSize(2);

        List<AccountIdentifier> identifiers = Lists.newArrayList();
        for (Destination destination : accountDestinations.getDestinations()) {
            assertThat(destination.getIdentifiers()).hasSize(1);
            identifiers.addAll(destination.getIdentifiers());
        }

        assertThat(identifiers).contains(IDENTIFIER_SSN2, IDENTIFIER_DANSKE);
    }

    @Test
    public void testFromAccountsTransform_EnsureItFiltersWithPatterns() {
        Account account = createAccount();
        ListMultimap<String, TransferDestinationPattern> patterns = createPatterns(false);
        Iterable<Destination> destinations = createDestinations();

        FromAccountTransform transform = new FromAccountTransform(patterns, destinations);
        AccountDestinations accountDestinations = transform.apply(account);

        assertThat(accountDestinations).isNotNull();
        assertThat(accountDestinations.getAccount()).isEqualTo(account);
        assertThat(accountDestinations.getDestinations()).hasSize(1);
        assertThat(Iterables.get(accountDestinations.getDestinations(), 0).getIdentifiers()).hasSize(1);
        assertThat(Iterables.get(accountDestinations.getDestinations(), 0).getPrimaryIdentifier().get())
                .isEqualTo(IDENTIFIER_SSN2);
    }

    @Test
    public void testFromAccountsTransform_EnsureItFiltersTheSameAccount() {
        Account account = createAccount();
        ListMultimap<String, TransferDestinationPattern> patterns = createPatterns(true);
        List<Destination> destinations = Lists.newArrayList(createDestinations());

        DestinationOfAccount accountDestination = createAccountDestination(account);
        destinations.add(accountDestination);

        FromAccountTransform transform = new FromAccountTransform(patterns, destinations);
        AccountDestinations accountDestinations = transform.apply(account);

        assertThat(accountDestinations).isNotNull();
        assertThat(accountDestinations.getAccount()).isEqualTo(account);
        assertThat(accountDestinations.getDestinations()).hasSize(Iterables.size(destinations) - 1);
        assertThat(accountDestinations.getDestinations()).doesNotContain(accountDestination);
    }

    private ListMultimap<String, TransferDestinationPattern> createPatterns(boolean wildcard) {
        List<TransferDestinationPattern> flatPatterns = Lists.newArrayList();

        TransferDestinationPattern pattern = new TransferDestinationPattern();
        pattern.setAccountId(ACC_UUID);
        pattern.setBank("Danske");
        pattern.setName("Name");
        pattern.setType(AccountIdentifier.Type.SE);

        pattern.setMatchesMultiple(wildcard);
        if (wildcard) {
            pattern.setPattern(".+");
        }
        else {
            pattern.setPattern(IDENTIFIER_SSN2.getIdentifier());
        }

        flatPatterns.add(pattern);

        return FluentIterable.from(flatPatterns).index(
                transferDestinationPattern -> transferDestinationPattern.getAccountId().toString());
    }

    private Iterable<Destination> createDestinations() {
        List<Destination> destinations = Lists.newArrayList();
        DestinationOfUserTransferDestination destination1 = new DestinationOfUserTransferDestination(IDENTIFIER_DANSKE);
        destination1.setName("User destination name");
        destinations.add(destination1);

        DestinationOfAccount destination2 = createAccountDestination(createAccount2());
        destinations.add(destination2);

        return destinations;
    }

    private DestinationOfAccount createAccountDestination(Account account) {
        DestinationOfAccount destination = new DestinationOfAccount(account.getIdentifiers());
        destination.setBalance(account.getBalance());
        destination.setCredentialsId(account.getCredentialsId());
        destination.setName(account.getName());
        destination.setType(account.getType());
        return destination;
    }

    private Account createAccount() {
        Account account = new Account();
        account.setId(ACC_UUID.toString());
        account.setBalance(1.0);
        account.setCredentialsId("TheCred");
        account.setName("The name");
        account.setType(AccountTypes.CHECKING);

        account.putIdentifier(IDENTIFIER_SSN1);

        return account;
    }

    private Account createAccount2() {
        Account account = new Account();
        account.setId(ACC_UUID2.toString());
        account.setBalance(1.0);
        account.setCredentialsId("TheCred2");
        account.setName("The name2");
        account.setType(AccountTypes.SAVINGS);

        account.putIdentifier(IDENTIFIER_SSN2);
        account.putIdentifier(IDENTIFIER_IBAN_SN2);

        return account;
    }
}
