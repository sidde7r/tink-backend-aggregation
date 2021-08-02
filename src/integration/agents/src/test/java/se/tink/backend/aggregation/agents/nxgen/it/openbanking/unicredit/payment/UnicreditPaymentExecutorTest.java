package se.tink.backend.aggregation.agents.nxgen.it.openbanking.unicredit.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants.STEP_FINALIZE;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants.STEP_INIT;

import com.github.rholder.retry.RetryException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditApiClientRetryer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.UnicreditPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.rpc.FetchPaymentStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class UnicreditPaymentExecutorTest {

    private UnicreditBaseApiClient unicreditBaseApiClient;
    private UnicreditPaymentExecutor unicreditPaymentExecutor;
    private PaymentMultiStepRequest paymentMultiStepRequest;

    private static final String PENDING_PAYMENT_STATUS_RESPONSE_FILE =
            "pending_payment_response.json";
    private static final String REJECTED_PAYMENT_STATUS_RESPONSE_FILE =
            "rejected_payment_response.json";
    private static final String ACCEPTED_PAYMENT_STATUS_RESPONSE_FILE =
            "accepted_payment_response.json";
    private static final String TEST_DATA_FOLDER_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/openbanking/unicredit/payment/resources/";

    @Before
    public void setup() {
        unicreditBaseApiClient = mock(UnicreditBaseApiClient.class);
        unicreditPaymentExecutor =
                new UnicreditPaymentExecutor(
                        unicreditBaseApiClient, new UnicreditApiClientRetryer());
        Payment payment = new Payment.Builder().build();
        paymentMultiStepRequest =
                new PaymentMultiStepRequest(
                        payment, new Storage(), STEP_INIT, Collections.emptyList());
    }

    @Test
    public void shouldRetryRequestForPaymentStatusWhenStatusIsPending() throws Exception {
        // given
        when(unicreditBaseApiClient.fetchPaymentStatus(paymentMultiStepRequest))
                .thenReturn(paymentStatusResponse(PENDING_PAYMENT_STATUS_RESPONSE_FILE))
                .thenReturn(paymentStatusResponse(PENDING_PAYMENT_STATUS_RESPONSE_FILE))
                .thenReturn(paymentStatusResponse(ACCEPTED_PAYMENT_STATUS_RESPONSE_FILE));

        // when
        PaymentMultiStepResponse response = unicreditPaymentExecutor.sign(paymentMultiStepRequest);

        // then
        verify(unicreditBaseApiClient, times(3)).fetchPaymentStatus(any());
        assertThat(response.getStep()).isEqualTo(STEP_FINALIZE);
    }

    @Test
    public void shouldThrowExceptionIfPaymentIsEventuallyRejected() {
        // given
        when(unicreditBaseApiClient.fetchPaymentStatus(paymentMultiStepRequest))
                .thenReturn(paymentStatusResponse(PENDING_PAYMENT_STATUS_RESPONSE_FILE))
                .thenReturn(paymentStatusResponse(PENDING_PAYMENT_STATUS_RESPONSE_FILE))
                .thenReturn(paymentStatusResponse(REJECTED_PAYMENT_STATUS_RESPONSE_FILE));

        // when
        assertThatThrownBy(() -> unicreditPaymentExecutor.sign(paymentMultiStepRequest))
                .isInstanceOf(PaymentRejectedException.class);

        // then
        verify(unicreditBaseApiClient, times(3)).fetchPaymentStatus(any());
    }

    @Test
    public void shouldNotRetryPaymentStatusCheckIfStatusIsAccepted() throws Exception {
        // given
        when(unicreditBaseApiClient.fetchPaymentStatus(paymentMultiStepRequest))
                .thenReturn(paymentStatusResponse(ACCEPTED_PAYMENT_STATUS_RESPONSE_FILE));

        // when
        PaymentMultiStepResponse response = unicreditPaymentExecutor.sign(paymentMultiStepRequest);

        // then
        verify(unicreditBaseApiClient, times(1)).fetchPaymentStatus(any());
        assertThat(response.getStep()).isEqualTo(STEP_FINALIZE);
    }

    @Test
    public void shouldThrowPaymentExceptionIfErrorDuringCheckingPaymentStatus() throws Exception {
        // given
        UnicreditApiClientRetryer unicreditApiClientRetryerMock =
                mock(UnicreditApiClientRetryer.class);
        unicreditPaymentExecutor =
                new UnicreditPaymentExecutor(unicreditBaseApiClient, unicreditApiClientRetryerMock);

        when(unicreditApiClientRetryerMock.callUntilPaymentStatusIsNotPending(any()))
                .thenThrow(ExecutionException.class);

        // then
        assertThatThrownBy(() -> unicreditPaymentExecutor.sign(paymentMultiStepRequest))
                .isInstanceOf(PaymentException.class);
    }

    @Test
    public void shouldAssumePaymentIsInitializedCorrectlyAfterReachingRetriesLimit()
            throws Exception {
        // given
        UnicreditApiClientRetryer unicreditApiClientRetryerMock =
                mock(UnicreditApiClientRetryer.class);
        unicreditPaymentExecutor =
                new UnicreditPaymentExecutor(unicreditBaseApiClient, unicreditApiClientRetryerMock);

        when(unicreditApiClientRetryerMock.callUntilPaymentStatusIsNotPending(any()))
                .thenThrow(RetryException.class);

        // when
        PaymentMultiStepResponse response = unicreditPaymentExecutor.sign(paymentMultiStepRequest);

        // then
        assertThat(response.getStep()).isEqualTo(STEP_FINALIZE);
    }

    private FetchPaymentStatusResponse paymentStatusResponse(
            String pendingPaymentStatusResponseFile) {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_FOLDER_PATH, pendingPaymentStatusResponseFile).toFile(),
                FetchPaymentStatusResponse.class);
    }
}
