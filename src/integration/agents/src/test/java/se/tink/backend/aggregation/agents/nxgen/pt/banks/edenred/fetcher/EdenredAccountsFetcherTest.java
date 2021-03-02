package se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.EdenredApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.entities.ProductEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.entities.TransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.rpc.CardListResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.storage.EdenredStorage;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RunWith(MockitoJUnitRunner.class)
public class EdenredAccountsFetcherTest {

    @Mock private EdenredApiClient edenredApiClient;

    @Mock private EdenredStorage edenredStorage;

    @InjectMocks private EdenredAccountsFetcher fetcher;

    @Test
    public void shouldFetchAccountsAndSave() {
        when(edenredApiClient.getCards()).thenReturn(mockCardsResponse());
        when(edenredApiClient.getTransactions(eq(123L))).thenReturn(mockTransactionsResponse());

        Collection<TransactionalAccount> transactionalAccounts = fetcher.fetchAccounts();

        assertThat(transactionalAccounts).isNotNull().hasSize(1);
        TransactionalAccount transactionalAccount = transactionalAccounts.stream().findAny().get();
        assertThat(transactionalAccount.getExactBalance()).isNotNull();
        assertThat(transactionalAccount.getExactBalance().getDoubleValue()).isEqualTo(132.45);
        assertThat(transactionalAccount.getExactAvailableBalance()).isNotNull();
        assertThat(transactionalAccount.getExactAvailableBalance().getDoubleValue())
                .isEqualTo(132.45);
        assertThat(transactionalAccount.getApiIdentifier()).isEqualTo("123");
        assertThat(transactionalAccount.getIdModule().getAccountName()).isEqualTo("Test Card");
        assertThat(transactionalAccount.getIdModule().getAccountNumber()).isEqualTo("1234567890");

        verify(edenredStorage).storeTransactions(eq(123L), any());
    }

    @Test
    public void shouldHandleCanceledCard() {
        CardListResponse response = new CardListResponse();
        response.setData(
                Collections.singletonList(
                        CardEntity.builder()
                                .id(123L)
                                .number("1234567890")
                                .status("CANCELED")
                                .product(ProductEntity.builder().name("Test Card").build())
                                .build()));
        when(edenredApiClient.getCards()).thenReturn(response);

        Collection<TransactionalAccount> transactionalAccounts = fetcher.fetchAccounts();
        assertThat(transactionalAccounts).isEmpty();
    }

    private CardListResponse mockCardsResponse() {
        CardListResponse response = new CardListResponse();
        response.setData(
                Collections.singletonList(
                        CardEntity.builder()
                                .id(123L)
                                .number("1234567890")
                                .status("ACTIVE")
                                .product(ProductEntity.builder().name("Test Card").build())
                                .build()));

        return response;
    }

    private TransactionsResponse mockTransactionsResponse() {
        TransactionsResponse transactionsResponse = new TransactionsResponse();
        transactionsResponse.setData(
                TransactionsEntity.builder()
                        .account(AccountEntity.builder().availableBalance(132.45).build())
                        .build());
        return transactionsResponse;
    }
}
