package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.hybrid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants.Time.DEFAULT_OFFSET;

import java.time.OffsetDateTime;
import java.time.Period;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.Account;

@RunWith(MockitoJUnitRunner.class)
public class UkObTransactionPaginationControllerTest {

    private UkObTransactionPaginationController<Account> controller;

    @Mock
    private UkObTransactionPaginator<TransactionKeyPaginatorResponse<String>, Account> paginator;

    @Mock private UkObDateCalculator<Account> ukCalculator;
    @Mock private Account account;

    private Period maxPeriodForSingleCycle;
    private OffsetDateTime toDateTime =
            new ConstantLocalDateTimeSource().now().atOffset(DEFAULT_OFFSET);
    private OffsetDateTime finalFromDateTime;
    private OffsetDateTime fromDateTime;

    @Test
    public void shouldNotBeAbleToFetchMoreWhenMaxFinalFromDateIsReached() {
        // given
        given(paginator.getTransactionsFor(any(), any(), any()))
                .willReturn(
                        new TransactionKeyPaginatorResponseImpl<>(Collections.emptyList(), null));

        toDateTime = new ConstantLocalDateTimeSource().now().atOffset(DEFAULT_OFFSET);
        finalFromDateTime = toDateTime.minusDays(2);
        maxPeriodForSingleCycle = Period.ofDays(90);

        given(ukCalculator.calculateTo(any())).willReturn(toDateTime);
        given(ukCalculator.calculateFinalFromDate(any(), any())).willReturn(finalFromDateTime);
        given(ukCalculator.calculateFromAsStartOfTheDayWithLimit(any(), any(), any()))
                .willReturn(finalFromDateTime);

        controller =
                new UkObTransactionPaginationController<>(
                        paginator, ukCalculator, maxPeriodForSingleCycle);

        // when
        PaginatorResponse response = controller.fetchTransactionsFor(account);

        // then
        verify(paginator, times(1))
                .getTransactionsFor(any(), eq(finalFromDateTime), eq(toDateTime));
        verifyNoMoreInteractions(paginator);
        assertThat(response.canFetchMore().get()).isFalse();
    }

    @Test
    public void shouldBeAbleToFetchMoreWhenMaxFinalFromDateIsNotReached() {
        // given
        given(paginator.getTransactionsFor(any(), any(), any()))
                .willReturn(
                        new TransactionKeyPaginatorResponseImpl<>(Collections.emptyList(), null));

        maxPeriodForSingleCycle = Period.ofDays(90);
        toDateTime = new ConstantLocalDateTimeSource().now().atOffset(DEFAULT_OFFSET);
        finalFromDateTime = toDateTime.minusDays(4);
        fromDateTime = toDateTime.minus(maxPeriodForSingleCycle);

        given(ukCalculator.calculateTo(any())).willReturn(toDateTime);
        given(ukCalculator.calculateFinalFromDate(any(), any())).willReturn(finalFromDateTime);
        given(ukCalculator.calculateFromAsStartOfTheDayWithLimit(any(), any(), any()))
                .willReturn(fromDateTime);

        controller =
                new UkObTransactionPaginationController<>(
                        paginator, ukCalculator, maxPeriodForSingleCycle);

        // when
        PaginatorResponse response = controller.fetchTransactionsFor(account);

        // then
        verify(paginator, times(1)).getTransactionsFor(any(), eq(fromDateTime), eq(toDateTime));
        verifyNoMoreInteractions(paginator);
        assertThat(response.canFetchMore().get()).isTrue();
    }

    @Test
    public void shouldEndFetchingCycleWhenNoKey() {
        // given
        String DUMMY_KEY_1 = "DUMMY_KEY_URL_1";
        String DUMMY_KEY_2 = "DUMMY_KEY_URL_2";
        given(paginator.getTransactionsFor(any(), any(), any()))
                .willReturn(
                        new TransactionKeyPaginatorResponseImpl<>(
                                Collections.emptyList(), DUMMY_KEY_1));
        given(paginator.getTransactionsFor(any(), any()))
                .willReturn(
                        new TransactionKeyPaginatorResponseImpl<>(
                                Collections.emptyList(), DUMMY_KEY_2))
                .willReturn(
                        new TransactionKeyPaginatorResponseImpl<>(Collections.emptyList(), null));

        maxPeriodForSingleCycle = Period.ofDays(90);
        toDateTime = new ConstantLocalDateTimeSource().now().atOffset(DEFAULT_OFFSET);
        finalFromDateTime = toDateTime.minusYears(2);
        fromDateTime = toDateTime.minus(maxPeriodForSingleCycle);

        given(ukCalculator.calculateTo(any())).willReturn(toDateTime);
        given(ukCalculator.calculateFinalFromDate(any(), any())).willReturn(finalFromDateTime);
        given(ukCalculator.calculateFromAsStartOfTheDayWithLimit(any(), any(), any()))
                .willReturn(fromDateTime);

        controller =
                new UkObTransactionPaginationController<>(
                        paginator, ukCalculator, maxPeriodForSingleCycle);

        // when
        controller.fetchTransactionsFor(account);

        // then
        verify(paginator, times(1)).getTransactionsFor(any(), eq(fromDateTime), eq(toDateTime));
        verify(paginator, times(1)).getTransactionsFor(any(), eq(DUMMY_KEY_1));
        verify(paginator, times(1)).getTransactionsFor(any(), eq(DUMMY_KEY_2));
    }
}
