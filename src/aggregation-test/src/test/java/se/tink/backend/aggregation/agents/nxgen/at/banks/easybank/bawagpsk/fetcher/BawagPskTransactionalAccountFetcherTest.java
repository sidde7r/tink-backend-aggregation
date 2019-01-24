package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.fetcher;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.AgentTestContext;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.framework.ArgumentHelper;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.authenticator.BawagPskPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.fetcher.transactional.BawagPskTransactionalAccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.backend.core.Amount;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

public final class BawagPskTransactionalAccountFetcherTest {
    private final ArgumentHelper helper = new ArgumentHelper(
            "tink.username",
            "tink.password",
            "tink.account_holder",
            "tink.bic",
            "tink.account1.number",
            "tink.account1.iban",
            "tink.account1.balance",
            "tink.account2.number",
            "tink.account2.iban",
            "tink.account2.balance"
    );

    private BawagPskPasswordAuthenticator authenticator;
    private BawagPskTransactionalAccountFetcher accountFetcher;

    @Before
    public void setUp() {
        helper.before();
        final Credentials credentials = new Credentials();
        credentials.setUsername(helper.get("tink.username"));
        credentials.setPassword(helper.get("tink.password"));
        final AgentContext context = new AgentTestContext(credentials);

        Provider provider = new Provider();
        provider.setPayload("ebanking.bawagpsk.com, BAWAG");

        final BawagPskApiClient apiClient = new BawagPskApiClient(new TinkHttpClient(context, credentials),
                new SessionStorage(), new PersistentStorage(), provider);
        authenticator = new BawagPskPasswordAuthenticator(apiClient);
        accountFetcher = new BawagPskTransactionalAccountFetcher(apiClient);
    }

    @AfterClass
    public static void afterClass() {
        ArgumentHelper.afterClass();
    }

    @Test
    public void testFetchAccounts() throws AuthenticationException, AuthorizationException {
        authenticator.authenticate(helper.get("tink.username"), helper.get("tink.password"));

        Collection<TransactionalAccount> accounts = accountFetcher.fetchAccounts();

        // Number of accounts
        Assert.assertEquals(accounts.size(), 2);

        // Account numbers
        Assert.assertEquals(accounts.stream().map(TransactionalAccount::getAccountNumber).collect(Collectors.toSet()),
                ImmutableSet.of(helper.get("tink.account1.number"), helper.get("tink.account2.number"))
        );

        // Account types
        Assert.assertThat(
                accounts.stream().map(TransactionalAccount::getType).collect(Collectors.toSet()),
                hasItems(AccountTypes.CHECKING)
        );

        // Holder name
        Assert.assertThat(
                accounts.stream().map(Account::getHolderName).collect(Collectors.toSet()),
                hasItem(new HolderName(helper.get("tink.account_holder")))
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
                        new IbanIdentifier(helper.get("tink.bic"), helper.get("tink.account1.iban")),
                        new IbanIdentifier(helper.get("tink.bic"), helper.get("tink.account2.iban"))
                )
        );

        // Balance
        Assert.assertThat(
                accounts.stream().map(Account::getBalance).collect(Collectors.toSet()),
                hasItems(
                        new Amount("EUR", Double.parseDouble(helper.get("tink.account1.balance"))),
                        new Amount("EUR", Double.parseDouble(helper.get("tink.account2.balance")))
                )
        );

        // Account types
        Assert.assertThat(
                accounts.stream().map(Account::getType).collect(Collectors.toSet()),
                hasItems(AccountTypes.CHECKING, AccountTypes.SAVINGS)
        );
    }
}
