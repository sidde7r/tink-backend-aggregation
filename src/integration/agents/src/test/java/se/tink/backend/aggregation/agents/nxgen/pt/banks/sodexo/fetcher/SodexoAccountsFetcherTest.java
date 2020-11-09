package se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.SodexoApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.SodexoStorage;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.fetchers.SodexoAccountsFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.rpc.BalanceResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RunWith(MockitoJUnitRunner.class)
public class SodexoAccountsFetcherTest {

    @Mock private SodexoStorage sodexoStorage;

    @Mock private SodexoApiClient sodexoApiClient;

    @InjectMocks private SodexoAccountsFetcher sodexoAccountsFetcher;

    @Test
    public void shouldFetchAccounts() {
        // given
        when(sodexoApiClient.getBalanceResponse()).thenReturn(mockBalanceResponse());
        when(sodexoStorage.getName()).thenReturn("John");
        when(sodexoStorage.getSurname()).thenReturn("Smith");
        when(sodexoStorage.getCardNumber()).thenReturn("420");

        // when
        Collection<TransactionalAccount> transactionalAccounts =
                sodexoAccountsFetcher.fetchAccounts();
        TransactionalAccount transactionalAccount = transactionalAccounts.stream().findAny().get();
        String id =
                sodexoStorage
                        .getName()
                        .concat(sodexoStorage.getSurname())
                        .concat(sodexoStorage.getCardNumber());

        // then
        assertThat(transactionalAccounts).isNotNull().hasSize(1);
        assertThat(transactionalAccount.getIdModule().getAccountName()).isEqualTo(id);
        assertThat(transactionalAccount.getIdModule().getUniqueId()).isEqualTo(id);
        assertThat(transactionalAccount.getIdModule().getAccountNumber()).isEqualTo(id);
        assertThat(
                        transactionalAccount
                                .getIdModule()
                                .getIdentifiers()
                                .iterator()
                                .next()
                                .getIdentifier())
                .isEqualTo(id);
    }

    private BalanceResponse mockBalanceResponse() {
        BalanceResponse balanceResponse = new BalanceResponse();
        balanceResponse.setBalance(100);
        balanceResponse.setBenefitDate("1 JAN");
        balanceResponse.setRealtime("00:00:00");
        balanceResponse.setStamp("stamp1");
        balanceResponse.setUsage("usage1");

        return balanceResponse;
    }
}
