package se.tink.backend.aggregation.agents.nxgen.it.bancoposta.fetcher.investment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.it.bancoposta.fetcher.FetcherTestData;
import se.tink.backend.aggregation.agents.nxgen.it.bancoposta.fetcher.FetcherTestHelper;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.Urls.InvestmentUrl;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaStorage;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.investment.BancoPostaInvestmentController;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class BancoPostaInvestmentControllerTest {
    private TinkHttpClient httpClient;
    private BancoPostaInvestmentController fetcher;

    @Before
    public void initSetUp() {
        this.httpClient = mock(TinkHttpClient.class, Mockito.RETURNS_DEEP_STUBS);
        PersistentStorage persistentStorage = FetcherTestHelper.prepareMockedPersistenStorage();
        BancoPostaStorage storage = new BancoPostaStorage(persistentStorage);
        BancoPostaApiClient apiClient = new BancoPostaApiClient(httpClient, storage);
        this.fetcher = new BancoPostaInvestmentController(apiClient);
    }

    @Test
    public void shouldFetchInvestmentData() {
        // given
        // when
        RequestBuilder fetchInvestmentMockRequestBuilder =
                FetcherTestHelper.mockRequestBuilder(InvestmentUrl.FETCH_INVESTMENTS, httpClient);
        when(fetchInvestmentMockRequestBuilder.post(any(), any()))
                .thenReturn(FetcherTestData.getInvestmentResponse());

        Collection<InvestmentAccount> investments = fetcher.fetchAccounts();

        // then
        assertThat(investments).hasSize(2);
        Iterator<InvestmentAccount> iterator = investments.iterator();
        InvestmentAccount investmentAccount1 = iterator.next();
        assertThatInvestmentAccountIsProperlyMapped(
                investmentAccount1,
                "dummydescription1",
                "dummyId1",
                new BigDecimal(5000).movePointLeft(2));

        InvestmentAccount investmentAccount2 = iterator.next();
        assertThatInvestmentAccountIsProperlyMapped(
                investmentAccount2,
                "dummydescription2",
                "dummyId2",
                new BigDecimal(10000).movePointLeft(2));
    }

    private void assertThatInvestmentAccountIsProperlyMapped(
            InvestmentAccount investmentAccount,
            String description,
            String id,
            BigDecimal currentValue) {
        assertThat(investmentAccount.getIdModule().getAccountName()).isEqualTo(description);
        assertThat(investmentAccount.getIdModule().getUniqueId()).isEqualTo(id);
        assertThat(investmentAccount.getExactBalance())
                .isEqualTo(ExactCurrencyAmount.of(currentValue, "EUR"));
    }
}
