package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.creditcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.SdcNoApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.creditcard.entity.CreditCardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.creditcard.entity.CreditCardTransactionsEntity;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class CreditCardTransactionFetcherTest {
    private SdcNoApiClient sdcNoApiClientMock;
    private CreditCardTransactionFetcher objUnderTest;

    @Before
    public void initSetup() {
        sdcNoApiClientMock = mock(SdcNoApiClient.class);
        objUnderTest = new CreditCardTransactionFetcher(sdcNoApiClientMock);
    }

    @Test
    public void fetchTransactionsForShouldTakeTransactionsFromClient() {
        // given
        CreditCardTransactionsEntity transactions =
                new CreditCardTransactionsEntity(
                        Arrays.asList(createTransaction()), Arrays.asList(createTransaction()));
        given(sdcNoApiClientMock.fetchCreditCardTransactions(any())).willReturn(transactions);

        // when
        List<AggregationTransaction> resultCollection =
                objUnderTest.fetchTransactionsFor(mock(CreditCardAccount.class));
        AggregationTransaction transaction = resultCollection.get(0);

        // then
        assertThat(resultCollection).hasSize(2);
        assertThat(transaction).isInstanceOf(AggregationTransaction.class);
    }

    private CreditCardTransactionEntity createTransaction() {
        return new CreditCardTransactionEntity(
                "DUMMY-DESCRIPTION", new Date(), new Date(), "0", "0", "NOK");
    }
}
