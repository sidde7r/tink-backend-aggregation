package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.fetcher.transferdestinations;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transferdestinations.DemobankTransferDestinationFetcher;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;

public class DemobankTransferDestinationFetcherTest {

    // UK
    private final AccountIdentifier ukAccountIdentifier1 =
            AccountIdentifier.create(URI.create("sort-code://31245678901234"));
    private final TransferDestinationPattern ukPattern1 =
            TransferDestinationPattern.createForMultiMatchAll(ukAccountIdentifier1.getType());
    private final AccountIdentifier ukAccountIdentifier2 =
            AccountIdentifier.create(URI.create("sort-code://31245678901235"));
    private final TransferDestinationPattern ukPattern2 =
            TransferDestinationPattern.createForMultiMatchAll(ukAccountIdentifier2.getType());

    // SE
    private final AccountIdentifier seAccountIdentifier1 =
            AccountIdentifier.create(AccountIdentifierType.SE, "4578-374856", "test");

    private final AccountIdentifier seAccountIdentifier2 =
            AccountIdentifier.create(URI.create("se-pg://4578-3748"));
    private final TransferDestinationPattern sePattern =
            TransferDestinationPattern.createForMultiMatchAll(seAccountIdentifier2.getType());

    // IT
    private final AccountIdentifier itAccountIdentifier =
            AccountIdentifier.create(URI.create("iban://IT60X0542811101000000123456"));
    private final TransferDestinationPattern itPattern =
            TransferDestinationPattern.createForMultiMatchAll(itAccountIdentifier.getType());

    private final List<TransferDestinationPattern> seDestinationPatterns =
            unmodifiableList(
                    asList(
                            TransferDestinationPattern.createForMultiMatchAll(
                                    AccountIdentifierType.SE),
                            TransferDestinationPattern.createForMultiMatchAll(
                                    AccountIdentifierType.SE_BG),
                            TransferDestinationPattern.createForMultiMatchAll(
                                    AccountIdentifierType.SE_PG)));

    private Account testAccount1;
    private Account testAccount2;
    private List<Account> accounts;

    private final TransferDestinationPattern defaultPattern =
            TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.IBAN);

    private final DemobankTransferDestinationFetcher fetcher =
            new DemobankTransferDestinationFetcher();

    @Test
    public void shouldReturnProperUkDestinationPatterns() {
        getAccountListForIdentifiers(ukAccountIdentifier1, ukAccountIdentifier2);

        Map<Account, List<TransferDestinationPattern>> transferDestinationsResponse =
                fetcher.fetchTransferDestinationsFor(accounts).getDestinations();

        assertTrue(transferDestinationsResponse.get(testAccount1).contains(ukPattern1));
        assertTrue(transferDestinationsResponse.get(testAccount2).contains(ukPattern2));
    }

    @Test
    public void shouldReturnProperDestinationPatternsForAccountsWithDifferentIdentifiers() {
        getAccountListForIdentifiers(ukAccountIdentifier1, itAccountIdentifier);

        Map<Account, List<TransferDestinationPattern>> transferDestinationsResponse =
                fetcher.fetchTransferDestinationsFor(accounts).getDestinations();

        assertTrue(transferDestinationsResponse.get(testAccount1).contains(ukPattern1));
        assertTrue(transferDestinationsResponse.get(testAccount2).contains(itPattern));
    }

    @Test
    public void shouldReturnSePgDestinationPattern() {
        getAccountListForIdentifiers(seAccountIdentifier2, seAccountIdentifier2);

        Map<Account, List<TransferDestinationPattern>> transferDestinationsResponse =
                fetcher.fetchTransferDestinationsFor(accounts).getDestinations();

        assertTrue(transferDestinationsResponse.get(testAccount1).size() == 1);
        assertTrue(transferDestinationsResponse.get(testAccount1).contains(sePattern));
    }

    @Test
    public void shouldReturnAllSeDestinationPatterns() {
        getAccountListForIdentifiers(seAccountIdentifier1, seAccountIdentifier1);

        Map<Account, List<TransferDestinationPattern>> transferDestinationsResponse =
                fetcher.fetchTransferDestinationsFor(accounts).getDestinations();

        assertTrue(
                transferDestinationsResponse.get(testAccount1).containsAll(seDestinationPatterns));
    }

    @Test
    public void shouldReturnOnlyIBANForNoAccountIdentifiers() {
        testAccount1 = new Account();
        testAccount1.setType(AccountTypes.CHECKING);
        Map<Account, List<TransferDestinationPattern>> transferDestinationsResponse =
                fetcher.fetchTransferDestinationsFor(singletonList(testAccount1)).getDestinations();

        assertTrue(transferDestinationsResponse.get(testAccount1).contains(defaultPattern));
    }

    private void getAccountListForIdentifiers(AccountIdentifier first, AccountIdentifier second) {
        accounts = new ArrayList<>();
        testAccount1 = new Account();
        testAccount1.setIdentifiers(Collections.singleton(first));
        testAccount1.setType(AccountTypes.CHECKING);
        testAccount1.setBankId("123");
        testAccount2 = new Account();
        testAccount2.setIdentifiers(Collections.singleton(second));
        testAccount2.setType(AccountTypes.CHECKING);
        testAccount2.setBankId("234");
        accounts.add(testAccount1);
        accounts.add(testAccount2);
    }
}
