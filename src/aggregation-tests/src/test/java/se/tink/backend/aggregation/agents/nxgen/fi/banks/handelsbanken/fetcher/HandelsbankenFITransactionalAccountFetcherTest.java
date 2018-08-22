package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher;

import java.util.Collection;
import java.util.stream.Collectors;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.HandelsbankenFIAuthenticatedTest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.HandelsbankenFITestConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount.HandelsbankenTransactionalAccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class HandelsbankenFITransactionalAccountFetcherTest extends HandelsbankenFIAuthenticatedTest {

    @Test
    public void accountsAreFetched() throws Exception {
        autoAuthenticator.autoAuthenticate();

        Collection<TransactionalAccount> accounts = new HandelsbankenTransactionalAccountFetcher(client,
                sessionStorage).fetchAccounts();

        assertThat(accounts, notNullValue());
        assertFalse(accounts.isEmpty());
        accounts.forEach(account -> {
            assertThat(account.getIdentifiers().stream()
                    .map(AccountIdentifier::getType)
                            .collect(Collectors.toList()),
                    hasItems(AccountIdentifier.Type.FI)
            );
        });
    }

    @Override
    protected HandelsbankenFITestConfig getTestConfig() {
        return HandelsbankenFITestConfig.USER_1;
    }
}
