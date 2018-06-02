package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher;

import java.util.Collection;
import java.util.stream.Collectors;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEAuthenticatedTest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.HandelsbankenTransactionalAccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class HandelsbankenSETransactionalAccountFetcherTest extends HandelsbankenSEAuthenticatedTest {

    @Test
    public void accountsAreFetched() throws Exception {
        autoAuthenticator.autoAuthenticate();

        Collection<TransactionalAccount> accounts = new HandelsbankenTransactionalAccountFetcher(client, sessionStorage).fetchAccounts();

        assertThat(accounts, notNullValue());
        assertFalse(accounts.isEmpty());
        accounts.forEach(account -> {
            assertThat(account.getIdentifiers().stream()
                    .map(AccountIdentifier::getType)
                            .collect(Collectors.toList()),
                    hasItems(AccountIdentifier.Type.SE, AccountIdentifier.Type.SE_SHB_INTERNAL)
            );
        });
    }
}
