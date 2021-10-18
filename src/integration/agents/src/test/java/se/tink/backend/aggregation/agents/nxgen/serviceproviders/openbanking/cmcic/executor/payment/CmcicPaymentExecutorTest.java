package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.apiclient.CmcicApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.HalPaymentRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.PaymentInformationStatusCodeEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.PaymentResponseEntity;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.nxgen.storage.Storage;

public class CmcicPaymentExecutorTest {

    private CmcicApiClient apiClient;
    private SessionStorage sessionStorage;
    private AgentConfiguration agentConfiguration;
    private SupplementalInformationHelper supplementalInformationHelper;
    private StrongAuthenticationState strongAuthenticationState;
    private CmcicPaymentRequestFactory paymentRequestFactory;
    private CmcicPaymentResponseMapper responseMapper;
    private CmcicPaymentExecutor paymentExecutor;

    @Before
    public void setUp() {
        apiClient = Mockito.mock(CmcicApiClient.class);
        sessionStorage = Mockito.mock(SessionStorage.class);
        agentConfiguration = Mockito.mock(AgentConfiguration.class);
        supplementalInformationHelper = Mockito.mock(SupplementalInformationHelper.class);
        strongAuthenticationState = Mockito.mock(StrongAuthenticationState.class);
        paymentRequestFactory = Mockito.mock(CmcicPaymentRequestFactory.class);
        responseMapper = Mockito.mock(CmcicPaymentResponseMapper.class);
        paymentExecutor =
                new CmcicPaymentExecutor(
                        apiClient,
                        sessionStorage,
                        agentConfiguration,
                        supplementalInformationHelper,
                        strongAuthenticationState,
                        paymentRequestFactory,
                        responseMapper);
    }

    @Test
    public void shouldNotRetryFetchingPaymentIfNotInPendingStatus() throws PaymentException {
        // given
        String paymentId = "13333";
        PaymentMultiStepRequest request = Mockito.mock(PaymentMultiStepRequest.class);
        given(sessionStorage.get(StorageKeys.AUTH_URL)).willReturn("=" + paymentId);
        given(request.getStep()).willReturn("post_sign_state");

        HalPaymentRequestEntity paymentRequest = Mockito.mock(HalPaymentRequestEntity.class);
        given(paymentRequest.isPending()).willReturn(false);

        PaymentResponseEntity paymentResponse = Mockito.mock(PaymentResponseEntity.class);

        given(paymentResponse.getPaymentInformationStatusCode())
                .willReturn(PaymentInformationStatusCodeEntity.ACCP);

        PaymentResponse paymentReponse = Mockito.mock(PaymentResponse.class);
        given(paymentReponse.getStorage()).willReturn(Mockito.mock(Storage.class));
        given(responseMapper.map(any(PaymentResponseEntity.class))).willReturn(paymentReponse);
        given(paymentRequest.getPaymentRequest()).willReturn(paymentResponse);

        given(apiClient.fetchPayment(paymentId)).willReturn(paymentRequest);

        // when
        paymentExecutor.sign(request);

        // then
        verify(apiClient, times(1)).fetchPayment(paymentId);
    }

    @Test
    public void shouldRetryFetchingPaymentIfInPendingStatus() throws PaymentException {
        // given
        String paymentId = "13333";
        PaymentMultiStepRequest request = Mockito.mock(PaymentMultiStepRequest.class);
        given(sessionStorage.get(StorageKeys.AUTH_URL)).willReturn("=" + paymentId);
        given(request.getStep()).willReturn("post_sign_state");

        HalPaymentRequestEntity paymentRequest = Mockito.mock(HalPaymentRequestEntity.class);
        given(paymentRequest.isPending()).willReturn(true);

        HalPaymentRequestEntity paymentRequest2 = Mockito.mock(HalPaymentRequestEntity.class);
        given(paymentRequest2.isPending()).willReturn(false);
        PaymentResponseEntity paymentResponse = Mockito.mock(PaymentResponseEntity.class);

        given(paymentResponse.getPaymentInformationStatusCode())
                .willReturn(PaymentInformationStatusCodeEntity.ACCP);
        given(paymentRequest2.getPaymentRequest()).willReturn(paymentResponse);

        PaymentResponse paymentReponse = Mockito.mock(PaymentResponse.class);
        given(paymentReponse.getStorage()).willReturn(Mockito.mock(Storage.class));
        given(responseMapper.map(any(PaymentResponseEntity.class))).willReturn(paymentReponse);

        given(apiClient.fetchPayment(paymentId)).willReturn(paymentRequest, paymentRequest2);

        // when
        paymentExecutor.sign(request);

        // then
        verify(apiClient, times(2)).fetchPayment(paymentId);
    }

    @Test
    public void shouldRetryFetchingPaymentIfNullResponse() throws PaymentException {
        // given
        String paymentId = "13333";
        PaymentMultiStepRequest request = Mockito.mock(PaymentMultiStepRequest.class);
        given(sessionStorage.get(StorageKeys.AUTH_URL)).willReturn("=" + paymentId);
        given(request.getStep()).willReturn("post_sign_state");

        HalPaymentRequestEntity paymentRequest = null;

        HalPaymentRequestEntity paymentRequest2 = Mockito.mock(HalPaymentRequestEntity.class);
        given(paymentRequest2.isPending()).willReturn(false);
        PaymentResponseEntity paymentResponse = Mockito.mock(PaymentResponseEntity.class);

        given(paymentResponse.getPaymentInformationStatusCode())
                .willReturn(PaymentInformationStatusCodeEntity.ACCP);
        given(paymentRequest2.getPaymentRequest()).willReturn(paymentResponse);

        PaymentResponse paymentReponse = Mockito.mock(PaymentResponse.class);
        given(paymentReponse.getStorage()).willReturn(Mockito.mock(Storage.class));
        given(responseMapper.map(any(PaymentResponseEntity.class))).willReturn(paymentReponse);

        given(apiClient.fetchPayment(paymentId)).willReturn(paymentRequest, paymentRequest2);

        // when
        paymentExecutor.sign(request);

        // then
        verify(apiClient, times(2)).fetchPayment(paymentId);
    }
}
