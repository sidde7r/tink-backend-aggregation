package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SdcAgreement;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.SearchTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.SearchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.parser.SdcTransactionParser;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@RunWith(MockitoJUnitRunner.class)
public class SdcNoTransactionFetcherTest {

    private static final String CURRENT_DATE = "2020-03-31";
    private static final String PREV_YEAR_DATE = "2019-03-31";
    private static final String ACCOUNT_NUMBER = "account number";
    private static final String ACCOUNT_NUMBER_KEY = "account number key";
    private static final String AGREEMENT_NUMBER = "agreement number";

    @Mock private SdcNoApiClient bankClient;

    @Mock private SdcTransactionParser transactionParser;

    private Supplier<Date> dateSupplier =
            () -> {
                try {
                    return new SimpleDateFormat("yyy-MM-dd").parse(CURRENT_DATE);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            };

    @Captor private ArgumentCaptor<SearchTransactionsRequest> captor;

    private SdcNoTransactionFetcher fetcher;

    @Before
    public void setUp() {
        given(bankClient.initWebPage())
                .willReturn(
                        "<a class=\"list__anchor\" data-id=\""
                                + ACCOUNT_NUMBER
                                + "\" data-idkey=\""
                                + ACCOUNT_NUMBER_KEY
                                + "\" />");

        SdcAgreement agreement = sdcAgreement();
        given(bankClient.fetchAgreement()).willReturn(agreement);

        fetcher = new SdcNoTransactionFetcher(bankClient, transactionParser, dateSupplier);
    }

    @Test
    public void fetch() {
        // given
        TransactionalAccount account = account();
        // and
        Transaction transaction = transaction();
        // and
        SearchTransactionsResponse searchTransactionsResponse =
                searchTransactionsResponse(transaction);
        given(bankClient.filterTransactionsFor(any(SearchTransactionsRequest.class)))
                .willReturn(searchTransactionsResponse);

        // when
        List<AggregationTransaction> response = fetcher.fetchTransactionsFor(account);

        // then
        verify(bankClient).initWebPage();
        verify(bankClient).accountFormPage(ACCOUNT_NUMBER_KEY, ACCOUNT_NUMBER);
        verify(bankClient).fetchAgreement();

        // and
        verifySearchTransactionsRequest();

        // and
        verify(searchTransactionsResponse).getTinkTransactions(transactionParser);

        // and
        assertThat(response).containsOnly(transaction);
    }

    private void verifySearchTransactionsRequest() {
        verify(bankClient).filterTransactionsFor(captor.capture());
        SearchTransactionsRequest searchTransactionsRequest = captor.getValue();
        assertThat(searchTransactionsRequest.getAccountId()).isEqualTo(ACCOUNT_NUMBER);
        assertThat(searchTransactionsRequest.getAgreementId()).isEqualTo(AGREEMENT_NUMBER);
        assertThat(searchTransactionsRequest.isIncludeReservations()).isEqualTo(true);
        assertThat(searchTransactionsRequest.getTransactionsFrom()).isEqualTo(PREV_YEAR_DATE);
        assertThat(searchTransactionsRequest.getTransactionsTo()).isEqualTo(CURRENT_DATE);
    }

    private TransactionalAccount account() {
        TransactionalAccount account = mock(TransactionalAccount.class);
        given(account.getAccountNumber()).willReturn(ACCOUNT_NUMBER);
        return account;
    }

    private Transaction transaction() {
        return Transaction.builder()
                .setAmount(new Amount())
                .setDate(dateSupplier.get())
                .setDescription("sample transaction")
                .build();
    }

    private SdcAgreement sdcAgreement() {
        SdcAgreement agreement = mock(SdcAgreement.class, RETURNS_DEEP_STUBS);
        given(agreement.getEntityKey().getAgreementNumber()).willReturn(AGREEMENT_NUMBER);
        return agreement;
    }

    private SearchTransactionsResponse searchTransactionsResponse(Transaction transaction) {
        SearchTransactionsResponse searchTransactionsResponse =
                mock(SearchTransactionsResponse.class);
        given(searchTransactionsResponse.getTinkTransactions(transactionParser))
                .willReturn(Collections.singletonList(transaction));
        return searchTransactionsResponse;
    }
}
