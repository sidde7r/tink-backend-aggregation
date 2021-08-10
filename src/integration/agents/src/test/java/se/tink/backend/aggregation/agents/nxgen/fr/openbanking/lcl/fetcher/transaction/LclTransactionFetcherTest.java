package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fetcher.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.RESOURCE_ID;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.TRANSACTION_DESCRIPTION;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.createExactCurrencyAmount;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.createTransactionResourceDtoMock;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.createTransactionalAccountMock;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.createTransactionsResponseDto;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.LclApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.transaction.CreditDebitIndicator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.transaction.TransactionResourceDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.transaction.TransactionStatus;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.transaction.TransactionsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fecther.transaction.LclTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.LinksDto;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class LclTransactionFetcherTest {

    private LclTransactionFetcher lclTransactionFetcher;

    private LclApiClient apiClientMock;

    @Before
    public void setUp() {
        apiClientMock = mock(LclApiClient.class);

        lclTransactionFetcher = new LclTransactionFetcher(apiClientMock);
    }

    @Test
    public void shouldGetTransactionsForPage1() {
        // given
        final TransactionsResponseDto transactionsResponseDtoMock = createTransactionsResponseDto();
        setUpTransactionsResponseDtoFirstPage(
                transactionsResponseDtoMock, CreditDebitIndicator.CRDT, TransactionStatus.BOOK);

        final TransactionalAccount transactionalAccountMock = createTransactionalAccountMock();

        when(apiClientMock.getTransactionsResponse(RESOURCE_ID, 1))
                .thenReturn(transactionsResponseDtoMock);

        // when
        final PaginatorResponse returnedResult =
                lclTransactionFetcher.getTransactionsFor(transactionalAccountMock, 1);

        // then
        assertThat(returnedResult.getTinkTransactions()).hasSize(1);
        assertThat(returnedResult.canFetchMore().isPresent()).isTrue();
        returnedResult.canFetchMore().ifPresent(canFetchMore -> assertThat(canFetchMore).isTrue());

        returnedResult
                .getTinkTransactions()
                .forEach(
                        transaction -> {
                            assertThat(transaction.getDescription())
                                    .isEqualTo(TRANSACTION_DESCRIPTION);
                            assertThat(transaction.getExactAmount())
                                    .isEqualTo(createExactCurrencyAmount());
                            assertThat(transaction.isPending()).isFalse();
                        });
    }

    @Test
    public void shouldGetTransactionsForPage2() {
        // given
        final TransactionsResponseDto transactionsResponseDtoMock = createTransactionsResponseDto();
        setUpTransactionsResponseDtoLastPage(transactionsResponseDtoMock);

        final TransactionalAccount transactionalAccountMock = createTransactionalAccountMock();

        when(apiClientMock.getTransactionsResponse(RESOURCE_ID, 2))
                .thenReturn(transactionsResponseDtoMock);

        // when
        final PaginatorResponse returnedResult =
                lclTransactionFetcher.getTransactionsFor(transactionalAccountMock, 2);

        // then
        assertThat(returnedResult.getTinkTransactions()).hasSize(1);
        assertThat(returnedResult.canFetchMore().isPresent()).isTrue();
        returnedResult.canFetchMore().ifPresent(canFetchMore -> assertThat(canFetchMore).isFalse());

        returnedResult
                .getTinkTransactions()
                .forEach(
                        transaction -> {
                            assertThat(transaction.getDescription())
                                    .isEqualTo(TRANSACTION_DESCRIPTION);
                            assertThat(transaction.getExactAmount())
                                    .isEqualTo(createExactCurrencyAmount());
                            assertThat(transaction.isPending()).isFalse();
                        });
    }

    @Test
    public void shouldGetTransactionsForPendingTransaction() {
        // given
        final TransactionsResponseDto transactionsResponseDtoMock = createTransactionsResponseDto();
        setUpTransactionsResponseDtoFirstPage(
                transactionsResponseDtoMock, CreditDebitIndicator.CRDT, TransactionStatus.PDNG);

        final TransactionalAccount transactionalAccountMock = createTransactionalAccountMock();

        when(apiClientMock.getTransactionsResponse(RESOURCE_ID, 1))
                .thenReturn(transactionsResponseDtoMock);

        // when
        final PaginatorResponse returnedResult =
                lclTransactionFetcher.getTransactionsFor(transactionalAccountMock, 1);

        // then
        assertThat(returnedResult.getTinkTransactions()).hasSize(1);
        assertThat(returnedResult.canFetchMore().isPresent()).isTrue();
        returnedResult.canFetchMore().ifPresent(canFetchMore -> assertThat(canFetchMore).isTrue());

        returnedResult
                .getTinkTransactions()
                .forEach(
                        transaction -> {
                            assertThat(transaction.getDescription())
                                    .isEqualTo(TRANSACTION_DESCRIPTION);
                            assertThat(transaction.getExactAmount())
                                    .isEqualTo(createExactCurrencyAmount());
                            assertThat(transaction.isPending()).isTrue();
                        });
    }

    @Test
    public void shouldGetTransactionsForDebitTransaction() {
        // given
        final TransactionsResponseDto transactionsResponseDtoMock = createTransactionsResponseDto();
        setUpTransactionsResponseDtoFirstPage(
                transactionsResponseDtoMock, CreditDebitIndicator.DBIT, TransactionStatus.BOOK);

        final TransactionalAccount transactionalAccountMock = createTransactionalAccountMock();

        when(apiClientMock.getTransactionsResponse(RESOURCE_ID, 1))
                .thenReturn(transactionsResponseDtoMock);

        // when
        final PaginatorResponse returnedResult =
                lclTransactionFetcher.getTransactionsFor(transactionalAccountMock, 1);

        // then
        assertThat(returnedResult.getTinkTransactions()).hasSize(1);
        assertThat(returnedResult.canFetchMore().isPresent()).isTrue();
        returnedResult.canFetchMore().ifPresent(canFetchMore -> assertThat(canFetchMore).isTrue());

        returnedResult
                .getTinkTransactions()
                .forEach(
                        transaction -> {
                            assertThat(transaction.getDescription())
                                    .isEqualTo(TRANSACTION_DESCRIPTION);
                            assertThat(transaction.getExactAmount())
                                    .isEqualTo(createExactCurrencyAmount());
                            assertThat(transaction.isPending()).isFalse();
                        });
    }

    private static void setUpTransactionsResponseDtoFirstPage(
            TransactionsResponseDto transactionsResponseDtoMock,
            CreditDebitIndicator creditDebitIndicator,
            TransactionStatus transactionStatus) {
        final TransactionResourceDto transactionResourceDtoMock =
                createTransactionResourceDtoMock(creditDebitIndicator, transactionStatus);

        when(transactionsResponseDtoMock.getTransactions())
                .thenReturn(ImmutableList.of(transactionResourceDtoMock));

        final LinksDto linksDtoMock = mock(LinksDto.class);
        when(linksDtoMock.getNext()).thenReturn(new Href());

        when(transactionsResponseDtoMock.getLinks()).thenReturn(linksDtoMock);
    }

    private static void setUpTransactionsResponseDtoLastPage(
            TransactionsResponseDto transactionsResponseDtoMock) {
        final TransactionResourceDto transactionResourceDtoMock =
                createTransactionResourceDtoMock(CreditDebitIndicator.CRDT, TransactionStatus.BOOK);

        when(transactionsResponseDtoMock.getTransactions())
                .thenReturn(ImmutableList.of(transactionResourceDtoMock));

        final LinksDto linksDtoMock = mock(LinksDto.class);
        when(linksDtoMock.getNext()).thenReturn(null);

        when(transactionsResponseDtoMock.getLinks()).thenReturn(linksDtoMock);
    }
}
