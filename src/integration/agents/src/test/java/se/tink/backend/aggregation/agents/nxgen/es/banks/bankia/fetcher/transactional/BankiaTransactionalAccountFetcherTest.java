package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.vavr.control.Either;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.rpc.AccountDetailsErrorCode;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.rpc.AccountDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RunWith(MockitoJUnitRunner.class)
public class BankiaTransactionalAccountFetcherTest {

    private static final String FAKE_IBAN = "ES9121000418450200051332";
    @Mock private BankiaApiClient apiClient;

    @InjectMocks private BankiaTransactionalAccountFetcher fetcher;

    @Test
    public void shouldFetchAccountWithHolder() {
        // given
        AccountEntity accountEntity =
                BankiaTransactionalAccountFixtures.DUMMY_ACCOUNTS.json(AccountEntity.class);
        List<AccountEntity> accounts = Collections.singletonList(accountEntity);
        when(apiClient.getAccounts()).thenReturn(accounts);

        Either<AccountDetailsErrorCode, AccountDetailsResponse> accountDetailsResponseEither =
                Either.right(
                        BankiaTransactionalAccountFixtures.DUMMY_ACCOUNT_DETAIL.json(
                                AccountDetailsResponse.class));
        when(apiClient.getAccountDetails(new AccountDetailsRequest(FAKE_IBAN)))
                .thenReturn(accountDetailsResponseEither);

        // when
        Collection<TransactionalAccount> result = fetcher.fetchAccounts();

        // then
        assertThat(result).isNotEmpty();
        TransactionalAccount account = result.iterator().next();
        assertThat(account.getParties()).isNotEmpty();
        assertThat(account.hashCode()).isEqualTo(FAKE_IBAN.toLowerCase().hashCode());
    }
}
