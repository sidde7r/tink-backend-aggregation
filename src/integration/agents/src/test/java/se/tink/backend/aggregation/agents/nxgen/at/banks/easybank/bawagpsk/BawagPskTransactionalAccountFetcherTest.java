package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk;

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
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;
import se.tink.backend.aggregation.agents.framework.context.AgentTestContext;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl.LoggingMode;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.LegacyTinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public final class BawagPskTransactionalAccountFetcherTest {

    private enum TransferArg implements ArgumentManagerEnum {
        ACCOUNT_HOLDER,
        BIC,
        ACCOUNT1_NUMBER,
        ACCOUNT1_IBAN,
        ACCOUNT1_BALANCE,
        ACCOUNT2_NUMBER,
        ACCOUNT2_IBAN,
        ACCOUNT2_BALANCE;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    private final ArgumentManager<TransferArg> transferHelper =
            new ArgumentManager<>(TransferArg.values());
    private final ArgumentManager<UsernamePasswordArgumentEnum> usernamePasswordHelper =
            new ArgumentManager<>(UsernamePasswordArgumentEnum.values());

    private BawagPskPasswordAuthenticator authenticator;
    private BawagPskTransactionalAccountFetcher accountFetcher;

    @Before
    public void setUp() {
        transferHelper.before();
        usernamePasswordHelper.before();
        final Credentials credentials = new Credentials();
        credentials.setUsername(usernamePasswordHelper.get(UsernamePasswordArgumentEnum.USERNAME));
        credentials.setPassword(usernamePasswordHelper.get(UsernamePasswordArgumentEnum.PASSWORD));
        final AgentContext context = new AgentTestContext(credentials);

        Provider provider = new Provider();
        provider.setPayload("ebanking.bawagpsk.com, BAWAG");

        final BawagPskApiClient apiClient =
                new BawagPskApiClient(
                        new LegacyTinkHttpClient(
                                context.getAggregatorInfo(),
                                context.getMetricRegistry(),
                                context.getLogOutputStream(),
                                null,
                                null,
                                context.getLogMasker(),
                                LoggingMode.LOGGING_MASKER_COVERS_SECRETS),
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
        authenticator.authenticate(
                usernamePasswordHelper.get(UsernamePasswordArgumentEnum.USERNAME),
                usernamePasswordHelper.get(UsernamePasswordArgumentEnum.PASSWORD));

        Collection<TransactionalAccount> accounts = accountFetcher.fetchAccounts();

        // Number of accounts
        Assert.assertEquals(accounts.size(), 2);

        // Account numbers
        Assert.assertEquals(
                accounts.stream()
                        .map(TransactionalAccount::getAccountNumber)
                        .collect(Collectors.toSet()),
                ImmutableSet.of(
                        transferHelper.get(TransferArg.ACCOUNT1_NUMBER),
                        transferHelper.get(TransferArg.ACCOUNT2_NUMBER)));

        // Account types
        Assert.assertThat(
                accounts.stream().map(TransactionalAccount::getType).collect(Collectors.toSet()),
                hasItems(AccountTypes.CHECKING));

        // Holder name
        Assert.assertThat(
                accounts.stream().map(Account::getHolderName).collect(Collectors.toSet()),
                hasItem(new HolderName(transferHelper.get(TransferArg.ACCOUNT_HOLDER))));

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
                        new IbanIdentifier(
                                transferHelper.get(TransferArg.BIC),
                                transferHelper.get(TransferArg.ACCOUNT1_IBAN)),
                        new IbanIdentifier(
                                transferHelper.get(TransferArg.BIC),
                                transferHelper.get(TransferArg.ACCOUNT2_IBAN))));

        // Balance
        Assert.assertThat(
                accounts.stream().map(Account::getExactBalance).collect(Collectors.toSet()),
                hasItems(
                        ExactCurrencyAmount.of(
                                transferHelper.get(TransferArg.ACCOUNT1_BALANCE), "EUR"),
                        ExactCurrencyAmount.of(
                                transferHelper.get(TransferArg.ACCOUNT2_BALANCE), "EUR")));

        // Account types
        Assert.assertThat(
                accounts.stream().map(Account::getType).collect(Collectors.toSet()),
                hasItems(AccountTypes.CHECKING, AccountTypes.SAVINGS));
    }
}
