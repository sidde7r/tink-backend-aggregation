package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.fetcher;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.AgentTestContext;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.authenticator.BawagPskPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.fetcher.transactional.BawagPskTransactionalAccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.Amount;

public final class BawagPskTransactionalAccountFetcherTest {

    private enum Arg {
        USERNAME,
        PASSWORD,
        ACCOUNT_HOLDER,
        BIC,
        ACCOUNT1_NUMBER,
        ACCOUNT1_IBAN,
        ACCOUNT1_BALANCE,
        ACCOUNT2_NUMBER,
        ACCOUNT2_IBAN,
        ACCOUNT2_BALANCE,
    }

    private final ArgumentManager<Arg> helper = new ArgumentManager<>(Arg.values());

    private BawagPskPasswordAuthenticator authenticator;
    private BawagPskTransactionalAccountFetcher accountFetcher;

    @Before
    public void setUp() {
        helper.before();
        final Credentials credentials = new Credentials();
        credentials.setUsername(helper.get(Arg.USERNAME));
        credentials.setPassword(helper.get(Arg.PASSWORD));
        final AgentContext context = new AgentTestContext(credentials);

        Provider provider = new Provider();
        provider.setPayload("ebanking.bawagpsk.com, BAWAG");

        final BawagPskApiClient apiClient =
                new BawagPskApiClient(
                        new TinkHttpClient(
                                context.getAggregatorInfo(),
                                context.getMetricRegistry(),
                                context.getLogOutputStream(),
                                null,
                                null),
                        new SessionStorage(),
                        new PersistentStorage(),
                        provider);
        authenticator = new BawagPskPasswordAuthenticator(apiClient);
        accountFetcher = new BawagPskTransactionalAccountFetcher(apiClient);
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testFetchAccounts() throws AuthenticationException, AuthorizationException {
        authenticator.authenticate(helper.get(Arg.USERNAME), helper.get(Arg.PASSWORD));

        Collection<TransactionalAccount> accounts = accountFetcher.fetchAccounts();

        // Number of accounts
        Assert.assertEquals(accounts.size(), 2);

        // Account numbers
        Assert.assertEquals(
                accounts.stream()
                        .map(TransactionalAccount::getAccountNumber)
                        .collect(Collectors.toSet()),
                ImmutableSet.of(helper.get(Arg.ACCOUNT1_NUMBER), helper.get(Arg.ACCOUNT2_NUMBER)));

        // Account types
        Assert.assertThat(
                accounts.stream().map(TransactionalAccount::getType).collect(Collectors.toSet()),
                hasItems(AccountTypes.CHECKING));

        // Holder name
        Assert.assertThat(
                accounts.stream().map(Account::getHolderName).collect(Collectors.toSet()),
                hasItem(new HolderName(helper.get(Arg.ACCOUNT_HOLDER))));

        Set<IbanIdentifier> ibans =
                accounts.stream()
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
                accounts.stream()
                        .map(Account::getIdentifiers)
                        .flatMap(List::stream)
                        .collect(Collectors.toSet()),
                hasItems(
                        new IbanIdentifier(helper.get(Arg.BIC), helper.get(Arg.ACCOUNT1_IBAN)),
                        new IbanIdentifier(helper.get(Arg.BIC), helper.get(Arg.ACCOUNT2_IBAN))));

        // Balance
        Assert.assertThat(
                accounts.stream().map(Account::getBalance).collect(Collectors.toSet()),
                hasItems(
                        new Amount("EUR", Double.parseDouble(helper.get(Arg.ACCOUNT1_BALANCE))),
                        new Amount("EUR", Double.parseDouble(helper.get(Arg.ACCOUNT2_BALANCE)))));

        // Account types
        Assert.assertThat(
                accounts.stream().map(Account::getType).collect(Collectors.toSet()),
                hasItems(AccountTypes.CHECKING, AccountTypes.SAVINGS));
    }
}
