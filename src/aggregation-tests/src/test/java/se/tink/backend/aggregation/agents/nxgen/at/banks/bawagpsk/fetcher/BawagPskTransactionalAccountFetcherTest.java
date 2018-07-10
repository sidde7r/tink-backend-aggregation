package se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.fetcher;

import com.google.common.collect.ImmutableSet;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.AgentTestContext;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.BawagPskApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.TestConfig;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.authenticator.BawagPskPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.fetcher.transactional.BawagPskTransactionalAccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.backend.core.Amount;
import se.tink.libraries.account.identifiers.IbanIdentifier;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BawagPskTransactionalAccountFetcherTest {
    private BawagPskPasswordAuthenticator authenticator;
    private BawagPskTransactionalAccountFetcher accountFetcher;

    @Before
    public void setUp() {
        final Credentials credentials = new Credentials();
        credentials.setUsername(TestConfig.USERNAME);
        credentials.setPassword(TestConfig.PASSWORD);
        final AgentContext context = new AgentTestContext(credentials);

        Provider provider = new Provider();
        provider.setPayload("¨ebanking.bawagpsk.com, BAWAG");

        final BawagPskApiClient apiClient = new BawagPskApiClient(new TinkHttpClient(context, credentials), new SessionStorage(), provider);
        authenticator = new BawagPskPasswordAuthenticator(apiClient);
        accountFetcher = new BawagPskTransactionalAccountFetcher(apiClient);
    }

    @Test
    @Ignore
    public void testFetchAccounts() throws AuthenticationException, AuthorizationException {
        authenticator.authenticate(TestConfig.USERNAME, TestConfig.PASSWORD);

        Collection<TransactionalAccount> accounts = accountFetcher.fetchAccounts();

        // Number of accounts
        Assert.assertEquals(accounts.size(), 2);

        // Account numbers
        Assert.assertEquals(accounts.stream().map(TransactionalAccount::getAccountNumber).collect(Collectors.toSet()),
                ImmutableSet.of(TestConfig.Account1.NUMBER, TestConfig.Account2.NUMBER)
        );

        // Account types
        Assert.assertThat(
                accounts.stream().map(TransactionalAccount::getType).collect(Collectors.toSet()),
                hasItems(AccountTypes.CHECKING)
        );

        // Unique identifier
        Assert.assertThat(accounts.stream().map(Account::getUniqueIdentifier).collect(Collectors.toSet()),
                hasItems(TestConfig.Account1.NUMBER, TestConfig.Account2.NUMBER)
        );

        // Holder name
        Assert.assertThat(
                accounts.stream().map(Account::getHolderName).collect(Collectors.toSet()),
                hasItem(new HolderName(TestConfig.ACCOUNT_HOLDER))
        );

        Set<IbanIdentifier> ibans = accounts.stream()
                .map(Account::getIdentifiers)
                .flatMap(List::stream)
                .filter(identifier -> identifier instanceof IbanIdentifier)
                .map(IbanIdentifier.class::cast)
                .collect(Collectors.toSet());

        // IBAN, size
        Assert.assertEquals(ibans.size(), 2);

        // IBAN, validation
        Assert.assertTrue(ibans.stream().allMatch(IbanIdentifier::isValid));

        // IBAN, contents
        Assert.assertThat(
                accounts.stream().map(Account::getIdentifiers).flatMap(List::stream).collect(Collectors.toSet()),
                hasItems(
                        new IbanIdentifier(TestConfig.BIC, TestConfig.Account1.IBAN),
                        new IbanIdentifier(TestConfig.BIC, TestConfig.Account2.IBAN)
                )
        );

        // Balance
        Assert.assertThat(
                accounts.stream().map(Account::getBalance).collect(Collectors.toSet()),
                hasItems(
                        new Amount("EUR", 10.01), new Amount("EUR", 0.0)
                )
        );

        // Account types
        Assert.assertThat(
                accounts.stream().map(Account::getType).collect(Collectors.toSet()),
                hasItems(AccountTypes.CHECKING, AccountTypes.SAVINGS)
        );
    }
}
