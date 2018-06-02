package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Test;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEAuthenticatedTest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.HandelsbankenTransactionalAccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.core.account.TransferDestinationPattern;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class HandelsbankenSETransferDestinationFetcherTest extends HandelsbankenSEAuthenticatedTest {

    @Test
    public void fetchesTransferDestinations() throws Exception {
        autoAuthenticator.autoAuthenticate();

        Collection<TransactionalAccount> accounts = new HandelsbankenTransactionalAccountFetcher(client, sessionStorage).fetchAccounts();

        TransferDestinationsResponse transferDestinations = new HandelsbankenSETransferDestinationFetcher(
                client, sessionStorage).fetchTransferDestinationsFor(
                        accounts.stream().map(TransactionalAccount::toSystemAccount).collect(Collectors.toList())
        );
        assertThat(transferDestinations, notNullValue());
        Map<Account, List<TransferDestinationPattern>> destinations = transferDestinations.getDestinations();
        assertThat(destinations, notNullValue());
        assertThat(destinations.isEmpty(), is(false));
        assertThat(destinations.size(), is(accounts.size()));

        destinations.forEach((account, destinationPatterns) -> {
            assertAccountCorrect(account, accounts);
            assertDestinationPatternsCorrect(destinationPatterns);
        });

    }

    private void assertAccountCorrect(Account account, Collection<TransactionalAccount> fetchedAccounts) {
        assertThat(fetchedAccounts.stream().anyMatch(fetchedAccount ->
                fetchedAccount.getAccountNumber().equals(account.getAccountNumber()) &&
                        fetchedAccount.getName().equals(account.getName())
        ), is(true));

    }

    private void assertDestinationPatternsCorrect(List<TransferDestinationPattern> destinationPatterns) {
        assertThat(destinationPatterns, notNullValue());
        assertThat(destinationPatterns.isEmpty(), is(false));
    }
}
