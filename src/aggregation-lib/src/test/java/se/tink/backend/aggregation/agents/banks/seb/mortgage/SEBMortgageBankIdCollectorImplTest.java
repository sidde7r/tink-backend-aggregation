package se.tink.backend.aggregation.agents.banks.seb.mortgage;

import com.google.inject.Provider;
import org.junit.Test;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.GetLoanStatusSignRequest;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.GetLoanStatusSignResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.utils.RateLimitedCountdown;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class SEBMortgageBankIdCollectorImplTest {

    @Test
    public void whenNoCollectStatusIsReturned_returnsStatusError() {
        SEBMortgageBankIdCollectorImpl bankIdCollector = new SEBMortgageBankIdCollectorImpl(
                mock(AggregationLogger.class),
                mock(SEBMortgageApiClient.class),
                COUNTDOWN_RETURNING_FALSE);

        // Check status
        assertThat(bankIdCollector.collect("abc123"))
                .isEqualTo(GetLoanStatusSignResponse.BankIdStatus.ERROR);
    }

    @Test
    public void callsApiClient_withCorrectRequest() {
        SEBMortgageApiClient apiClientMock = mock(SEBMortgageApiClient.class);

        SEBMortgageBankIdCollectorImpl bankIdCollector = new SEBMortgageBankIdCollectorImpl(
                mock(AggregationLogger.class),
                apiClientMock,
                createCountdownReturningTrueThenFalse());

        GetLoanStatusSignRequest request = new GetLoanStatusSignRequest("abc123");
        GetLoanStatusSignResponse response = new GetLoanStatusSignResponse();
        response.setStatus(GetLoanStatusSignResponse.BankIdStatus.ERROR);
        when(apiClientMock.getMortgageStatusSign(eq(request)))
                .thenReturn(response);

        // Call collect
        bankIdCollector.collect("abc123");

        // Check that we sent the correct stuff to SEB
        verify(apiClientMock, times(1)).getMortgageStatusSign(eq(request));
        verifyNoMoreInteractions(apiClientMock);
    }

    @Test
    public void errorResponse_returnsErrorStatusWithoutMoreLooping() {
        SEBMortgageApiClient apiClientMock = mock(SEBMortgageApiClient.class);

        SEBMortgageBankIdCollectorImpl bankIdCollector = new SEBMortgageBankIdCollectorImpl(
                mock(AggregationLogger.class),
                apiClientMock,
                createCountdownReturningTrueThenThrows());

        when(apiClientMock.getMortgageStatusSign(any(GetLoanStatusSignRequest.class)))
                .thenReturn(response(GetLoanStatusSignResponse.BankIdStatus.ERROR));

        // Check status
        assertThat(bankIdCollector.collect("abc123"))
                .isEqualTo(GetLoanStatusSignResponse.BankIdStatus.ERROR);
    }

    @Test
    public void completeResponse_returnsCompleteStatusWithoutMoreLooping() {
        SEBMortgageApiClient apiClientMock = mock(SEBMortgageApiClient.class);

        SEBMortgageBankIdCollectorImpl bankIdCollector = new SEBMortgageBankIdCollectorImpl(
                mock(AggregationLogger.class),
                apiClientMock,
                createCountdownReturningTrueThenThrows());

        when(apiClientMock.getMortgageStatusSign(any(GetLoanStatusSignRequest.class)))
                .thenReturn(response(GetLoanStatusSignResponse.BankIdStatus.COMPLETE));

        // Check status
        assertThat(bankIdCollector.collect("abc123"))
                .isEqualTo(GetLoanStatusSignResponse.BankIdStatus.COMPLETE);
    }

    @Test
    public void outstandingResponse_loops() {
        SEBMortgageApiClient apiClientMock = mock(SEBMortgageApiClient.class);

        SEBMortgageBankIdCollectorImpl bankIdCollector = new SEBMortgageBankIdCollectorImpl(
                mock(AggregationLogger.class),
                apiClientMock,
                createCountdownReturningTrueThenThrows());

        when(apiClientMock.getMortgageStatusSign(any(GetLoanStatusSignRequest.class)))
                .thenReturn(response(GetLoanStatusSignResponse.BankIdStatus.OUTSTANDING_TRANSACTION));

        // Check status
        boolean didLoop = false;
        try {
            bankIdCollector.collect("abc123");
        } catch (IllegalStateException e) {
            // Excpected exception because COUNTDOWN_RETURNING_TRUE_THEN_THROWS throws on second request
            didLoop = true;
        }

        assertThat(didLoop).isTrue();
    }

    @Test
    public void eachCollect_getsOwnCollectCountdown() {
        SEBMortgageApiClient apiClientMock = mock(SEBMortgageApiClient.class);

        CountingProvider countingCountdownReturningFalseProvider = new CountingProvider();
        SEBMortgageBankIdCollectorImpl bankIdCollector = new SEBMortgageBankIdCollectorImpl(
                mock(AggregationLogger.class),
                apiClientMock,
                countingCountdownReturningFalseProvider);

        when(apiClientMock.getMortgageStatusSign(any(GetLoanStatusSignRequest.class)))
                .thenReturn(response(GetLoanStatusSignResponse.BankIdStatus.EXPIRED_TRANSACTION));

        bankIdCollector.collect("abc123");
        bankIdCollector.collect("def456");

        // The provider should have fetched the countdown instance two times with get
        assertThat(countingCountdownReturningFalseProvider.getGetCount())
                .isEqualTo(2);
    }

    private static GetLoanStatusSignResponse response(GetLoanStatusSignResponse.BankIdStatus status) {
        GetLoanStatusSignResponse response = new GetLoanStatusSignResponse();
        response.setStatus(status);
        return response;
    }

    private static final Provider<RateLimitedCountdown> COUNTDOWN_RETURNING_FALSE =
            () -> () -> false;

    private static class CountingProvider implements Provider<RateLimitedCountdown> {
        int getCount;

        @Override
        public RateLimitedCountdown get() {
            getCount++;
            return COUNTDOWN_RETURNING_FALSE.get();
        }

        private int getGetCount() {
            return getCount;
        }
    }

    private static Provider<RateLimitedCountdown> createCountdownReturningTrueThenThrows() {
        return () -> new RateLimitedCountdown() {
            private boolean hasBeenCalled = false;

            @Override
            public boolean acquireIsMore() {
                if (!hasBeenCalled) {
                    hasBeenCalled = true;
                    return true;
                } else {
                    throw new IllegalStateException("This should only be called once.");
                }
            }
        };
    }

    private static Provider<RateLimitedCountdown> createCountdownReturningTrueThenFalse() {
        return () -> new RateLimitedCountdown() {
            private boolean hasBeenCalled = false;

            @Override
            public boolean acquireIsMore() {
                if (!hasBeenCalled) {
                    hasBeenCalled = true;
                    return true;
                } else {
                    return false;
                }
            }
        };
    }
}
