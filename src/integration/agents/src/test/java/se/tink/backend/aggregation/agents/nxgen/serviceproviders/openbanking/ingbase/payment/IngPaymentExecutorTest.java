package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.entities.PaymentsLinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.payment.rpc.Payment;

public class IngPaymentExecutorTest {

    private static final String AUTHORIZATION_URL = "http://something.com/redirect?id=123";
    private static final String SUPPLEMENTAL_KEY = "some_supplemental_key";

    private IngPaymentApiClient paymentApiClient;
    private IngPaymentMapper paymentMapper;
    private SessionStorage sessionStorage;
    private SupplementalInformationHelper supplementalInformationHelper;

    private InOrder mocksInOrder;

    private IngPaymentExecutor paymentExecutor;

    @Before
    public void setup() {
        paymentApiClient = mock(IngPaymentApiClient.class);
        paymentMapper = mock(IngPaymentMapper.class);
        sessionStorage = mock(SessionStorage.class);
        StrongAuthenticationState strongAuthenticationState = mock(StrongAuthenticationState.class);
        when(strongAuthenticationState.getSupplementalKey()).thenReturn(SUPPLEMENTAL_KEY);
        supplementalInformationHelper = mock(SupplementalInformationHelper.class);

        mocksInOrder =
                inOrder(
                        paymentApiClient,
                        paymentMapper,
                        sessionStorage,
                        supplementalInformationHelper);

        paymentExecutor =
                new IngPaymentExecutor(
                        paymentApiClient,
                        paymentMapper,
                        sessionStorage,
                        strongAuthenticationState,
                        supplementalInformationHelper);
    }

    @Test
    @SneakyThrows
    public void createShouldCallApiClientAndReturnPaymentResponse() {
        // given
        PaymentRequest tinkRequest = mock(PaymentRequest.class);
        CreatePaymentRequest createPaymentRequest = mock(CreatePaymentRequest.class);
        when(paymentMapper.toIngPaymentRequest(tinkRequest)).thenReturn(createPaymentRequest);

        CreatePaymentResponse createPaymentResponse =
                new CreatePaymentResponse("paymentId", new PaymentsLinksEntity(AUTHORIZATION_URL));
        when(paymentApiClient.createPayment(createPaymentRequest))
                .thenReturn(createPaymentResponse);

        PaymentResponse tinkResponse = mock(PaymentResponse.class);
        when(paymentMapper.toTinkPaymentResponse(tinkRequest, createPaymentResponse))
                .thenReturn(tinkResponse);

        // when
        PaymentResponse actualTinkResponse = paymentExecutor.create(tinkRequest);

        // then
        assertThat(actualTinkResponse).isEqualTo(tinkResponse);
        mocksInOrder.verify(paymentMapper).toIngPaymentRequest(tinkRequest);
        mocksInOrder.verify(paymentApiClient).createPayment(createPaymentRequest);
        mocksInOrder
                .verify(sessionStorage)
                .put(IngPaymentExecutor.PAYMENT_AUTHORIZATION_URL, AUTHORIZATION_URL);
        mocksInOrder
                .verify(paymentMapper)
                .toTinkPaymentResponse(tinkRequest, createPaymentResponse);
        mocksInOrder.verifyNoMoreInteractions();
    }

    @Test
    @SneakyThrows
    public void signShouldOpenThirdPartyAppOnInitWithCallbackParams() {
        // given
        PaymentMultiStepRequest paymentRequest =
                new PaymentMultiStepRequest(
                        mock(Payment.class),
                        sessionStorage,
                        AuthenticationStepConstants.STEP_INIT,
                        Collections.emptyList(),
                        Collections.emptyList());

        when(sessionStorage.get(IngPaymentExecutor.PAYMENT_AUTHORIZATION_URL))
                .thenReturn(AUTHORIZATION_URL);

        mockEmptySupplementalInfoResponse();

        // when
        PaymentMultiStepResponse response = paymentExecutor.sign(paymentRequest);

        // then
        assertThat(response.getStep()).isEqualTo(IngPaymentExecutor.VALIDATE_PAYMENT);
        mocksInOrder.verify(sessionStorage).get(IngPaymentExecutor.PAYMENT_AUTHORIZATION_URL);
        mocksInOrder.verify(supplementalInformationHelper).openThirdPartyApp(any());
        mocksInOrder
                .verify(supplementalInformationHelper)
                .waitForSupplementalInformation(SUPPLEMENTAL_KEY, 9L, TimeUnit.MINUTES);
        mocksInOrder.verifyNoMoreInteractions();
    }

    @Test
    @SneakyThrows
    public void signShouldVerifyPaymentStatusOnPostSign() {
        // given
        PaymentMultiStepRequest paymentRequest =
                new PaymentMultiStepRequest(
                        mock(Payment.class),
                        sessionStorage,
                        IngPaymentExecutor.VALIDATE_PAYMENT,
                        Collections.emptyList(),
                        Collections.emptyList());

        when(paymentApiClient.getPayment(any())).thenReturn(new GetPaymentResponse("ACSC"));

        // when
        PaymentMultiStepResponse response = paymentExecutor.sign(paymentRequest);

        // then
        assertThat(response.getStep()).isEqualTo(AuthenticationStepConstants.STEP_FINALIZE);
        mocksInOrder.verify(paymentApiClient).getPayment(any());
        mocksInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void signShouldThrowExceptionIfPaymentIsPending() {
        // given
        PaymentMultiStepRequest paymentRequest =
                new PaymentMultiStepRequest(
                        mock(Payment.class),
                        sessionStorage,
                        IngPaymentExecutor.VALIDATE_PAYMENT,
                        Collections.emptyList(),
                        Collections.emptyList());

        when(paymentApiClient.getPayment(any())).thenReturn(new GetPaymentResponse("RCVD"));

        // when
        Throwable thrown = catchThrowable(() -> paymentExecutor.sign(paymentRequest));

        // then
        assertThat(thrown).isInstanceOf(PaymentRejectedException.class);
        mocksInOrder.verify(paymentApiClient).getPayment(any());
        mocksInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void signShouldThrowExceptionIfPaymentIsRejected() {
        // given
        PaymentMultiStepRequest paymentRequest =
                new PaymentMultiStepRequest(
                        mock(Payment.class),
                        sessionStorage,
                        IngPaymentExecutor.VALIDATE_PAYMENT,
                        Collections.emptyList(),
                        Collections.emptyList());

        when(paymentApiClient.getPayment(any())).thenReturn(new GetPaymentResponse("RCVD"));

        // when
        Throwable thrown = catchThrowable(() -> paymentExecutor.sign(paymentRequest));

        // then
        assertThat(thrown).isInstanceOf(PaymentRejectedException.class);
        mocksInOrder.verify(paymentApiClient).getPayment(any());
        mocksInOrder.verifyNoMoreInteractions();
    }

    private void mockEmptySupplementalInfoResponse() {
        when(supplementalInformationHelper.waitForSupplementalInformation(any(), anyLong(), any()))
                .thenReturn(Optional.of(new HashMap<>()));
    }
}
