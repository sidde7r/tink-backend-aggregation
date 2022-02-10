package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.IngPaymentTestFixtures.getAgentPisCapability;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import se.tink.backend.aggregation.agents.agentcapabilities.PisCapability;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.entities.IngPaymentsLinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.IngCreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.IngCreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.IngCreateRecurringPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.IngPaymentStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.rpc.PaymentServiceType;

@RunWith(JUnitParamsRunner.class)
public class IngPaymentExecutorTest {

    private SessionStorage sessionStorage;
    private IngPaymentApiClient paymentApiClient;
    private IngPaymentAuthenticator paymentAuthenticator;
    private IngPaymentMapper paymentMapper;
    private InOrder mocksInOrder;

    private IngPaymentExecutor paymentExecutor;

    @Before
    public void setup() {
        sessionStorage = mock(SessionStorage.class);
        paymentApiClient = mock(IngPaymentApiClient.class);
        paymentAuthenticator = mock(IngPaymentAuthenticator.class);
        paymentMapper = mock(IngPaymentMapper.class);

        mocksInOrder =
                inOrder(sessionStorage, paymentApiClient, paymentAuthenticator, paymentMapper);

        paymentExecutor = createIngPaymentExecutor(true);
    }

    @SneakyThrows
    @Test
    @Parameters(method = "oneTimePaymentsWithAllPossibleResponseStatuses")
    public void createOneTimePaymentShouldCallApiClientAndReturnPaymentResponse(
            Payment payment, PaymentStatus resultPaymentStatus) {
        // given
        String authUrl = "http://something.com/redirect/XX";
        IngCreatePaymentRequest createPaymentRequest = mock(IngCreatePaymentRequest.class);
        when(paymentMapper.toIngCreatePaymentRequest(any())).thenReturn(createPaymentRequest);

        // and
        when(paymentApiClient.createPayment(any(), any()))
                .thenReturn(
                        new IngCreatePaymentResponse(
                                "SAMPLE_PAYMENT_ID",
                                "SAMPLE_TRANSACTION_STATUS",
                                new IngPaymentsLinksEntity(authUrl)));
        when(paymentApiClient.getMarketCode()).thenReturn("FR");
        when(paymentMapper.getPaymentStatus(anyString())).thenReturn(resultPaymentStatus);

        // when
        PaymentResponse tinkResponse = paymentExecutor.create(new PaymentRequest(payment));

        // then
        assertThat(tinkResponse.getPayment()).isEqualTo(payment);
        assertThat(tinkResponse.getPayment().getUniqueId()).isEqualTo("SAMPLE_PAYMENT_ID");
        assertThat(tinkResponse.getPayment().getStatus()).isEqualTo(resultPaymentStatus);

        mocksInOrder.verify(paymentMapper).toIngCreatePaymentRequest(payment);
        mocksInOrder
                .verify(paymentApiClient)
                .createPayment(createPaymentRequest, payment.getPaymentServiceType());
        mocksInOrder
                .verify(sessionStorage)
                .put(StorageKeys.PAYMENT_AUTHORIZATION_URL, "http://something.com/redirect/FR");
        mocksInOrder.verify(paymentMapper).getPaymentStatus("SAMPLE_TRANSACTION_STATUS");
        mocksInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private static Object[] oneTimePaymentsWithAllPossibleResponseStatuses() {
        List<Payment> oneTimePayments =
                asList(
                        emptyPayment(PaymentScheme.SEPA_CREDIT_TRANSFER),
                        emptyPayment(PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER),
                        emptyPayment(PaymentServiceType.SINGLE));
        PaymentStatus[] paymentStatuses = PaymentStatus.values();

        List<Object[]> params = new ArrayList<>();
        for (Payment payment : oneTimePayments) {
            for (PaymentStatus paymentStatus : paymentStatuses) {
                params.add(new Object[] {payment, paymentStatus});
            }
        }
        return params.toArray();
    }

    @SneakyThrows
    @Test
    @Parameters(method = "recurringPaymentsWithAllPossibleCreatePaymentStatuses")
    public void createRecurringPaymentShouldCallApiClientAndReturnPaymentResponse(
            Payment payment, PaymentStatus createPaymentStatus) {
        // given
        String authUrl = "http://something.com/redirect?id=123456";
        IngCreateRecurringPaymentRequest createPaymentRequest =
                mock(IngCreateRecurringPaymentRequest.class);
        when(paymentMapper.toIngCreateRecurringPaymentRequest(any()))
                .thenReturn(createPaymentRequest);

        // and
        when(paymentApiClient.createPayment(any(), any()))
                .thenReturn(
                        new IngCreatePaymentResponse(
                                "SAMPLE_PAYMENT_ID_123",
                                "SAMPLE_TRANSACTION_STATUS_123",
                                new IngPaymentsLinksEntity(authUrl)));

        // and
        when(paymentApiClient.getMarketCode()).thenReturn("DE");
        when(paymentMapper.getPaymentStatus(anyString())).thenReturn(createPaymentStatus);

        // when
        PaymentResponse tinkResponse = paymentExecutor.create(new PaymentRequest(payment));

        // then
        assertThat(tinkResponse.getPayment()).isEqualTo(payment);
        assertThat(tinkResponse.getPayment().getUniqueId()).isEqualTo("SAMPLE_PAYMENT_ID_123");
        assertThat(tinkResponse.getPayment().getStatus()).isEqualTo(createPaymentStatus);

        mocksInOrder.verify(paymentMapper).toIngCreateRecurringPaymentRequest(payment);
        mocksInOrder
                .verify(paymentApiClient)
                .createPayment(createPaymentRequest, payment.getPaymentServiceType());
        mocksInOrder
                .verify(sessionStorage)
                .put(
                        StorageKeys.PAYMENT_AUTHORIZATION_URL,
                        "http://something.com/redirect?id=123456");
        mocksInOrder.verify(paymentMapper).getPaymentStatus("SAMPLE_TRANSACTION_STATUS_123");
        mocksInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private static Object[] recurringPaymentsWithAllPossibleCreatePaymentStatuses() {
        Payment recurringPayment = emptyPayment(PaymentServiceType.PERIODIC);

        List<Object[]> params = new ArrayList<>();
        for (PaymentStatus createPaymentStatus : PaymentStatus.values()) {
            params.add(new Object[] {recurringPayment, createPaymentStatus});
        }
        return params.toArray();
    }

    @Test
    @Parameters(method = "signTestParams")
    @SneakyThrows
    public void signShouldCallAuthenticatorAndVerifyPaymentStatus(
            boolean callbackReceived,
            PaymentStatus paymentStatus,
            PaymentException expectedException) {
        // given
        Payment payment = emptyPayment("SAMPLE_PAYMENT_ID_123");
        PaymentMultiStepRequest paymentRequest =
                new PaymentMultiStepRequest(
                        payment,
                        sessionStorage,
                        AuthenticationStepConstants.STEP_INIT,
                        Collections.emptyList());

        // and
        when(sessionStorage.get(StorageKeys.PAYMENT_AUTHORIZATION_URL))
                .thenReturn("SAMPLE_AUTHORIZATION_URL");

        // and
        IngPaymentStatusResponse statusResponse = new IngPaymentStatusResponse("SAMPLE_STATUS");
        when(paymentApiClient.getPaymentStatus(anyString(), any())).thenReturn(statusResponse);

        // and
        when(paymentAuthenticator.waitForUserConfirmation(any())).thenReturn(callbackReceived);

        // and
        when(paymentMapper.getPaymentStatus(anyString())).thenReturn(paymentStatus);

        // when
        PaymentMultiStepResponse response = null;
        Throwable throwable = null;
        try {
            response = paymentExecutor.sign(paymentRequest);
        } catch (Throwable e) {
            throwable = e;
        }

        // then
        if (expectedException != null) {
            assertThat(throwable).isNotNull();
            assertThat(throwable).isInstanceOf(PaymentException.class);
            assertThat(throwable.getMessage()).isEqualTo(expectedException.getMessage());

        } else {
            assertThat(response).isNotNull();
            assertThat(response.getPayment()).isNotNull();
            assertThat(response.getPayment().getStatus()).isEqualTo(paymentStatus);
            assertThat(response.getStep()).isEqualTo(AuthenticationStepConstants.STEP_FINALIZE);
        }

        mocksInOrder.verify(sessionStorage).get(StorageKeys.PAYMENT_AUTHORIZATION_URL);
        mocksInOrder
                .verify(paymentAuthenticator)
                .waitForUserConfirmation("SAMPLE_AUTHORIZATION_URL");
        mocksInOrder
                .verify(paymentApiClient)
                .getPaymentStatus("SAMPLE_PAYMENT_ID_123", payment.getPaymentServiceType());
        mocksInOrder.verify(paymentMapper).getPaymentStatus("SAMPLE_STATUS");
        if (paymentStatus == PaymentStatus.PENDING) {
            mocksInOrder
                    .verify(paymentApiClient)
                    .cancelPayment("SAMPLE_PAYMENT_ID_123", payment.getPaymentServiceType());
        }
        mocksInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private Object[] signTestParams() {
        Stream<SignTestParams> expectedStatuses =
                Stream.of(
                        SignTestParams.builder().paymentStatus(PaymentStatus.SIGNED).build(),
                        SignTestParams.builder().paymentStatus(PaymentStatus.PAID).build(),
                        SignTestParams.builder()
                                .paymentStatus(PaymentStatus.REJECTED)
                                .expectedException(
                                        new PaymentRejectedException(
                                                "[ING] Payment rejected by Bank"))
                                .build(),
                        SignTestParams.builder()
                                .paymentStatus(PaymentStatus.CANCELLED)
                                .expectedException(
                                        new PaymentCancelledException(
                                                "[ING] Payment cancelled by PSU"))
                                .build(),
                        SignTestParams.builder()
                                .callbackReceived(true)
                                .paymentStatus(PaymentStatus.PENDING)
                                .expectedException(
                                        new PaymentCancelledException(
                                                "[ING] User left authorization page without approving request"))
                                .build(),
                        SignTestParams.builder()
                                .callbackReceived(false)
                                .paymentStatus(PaymentStatus.PENDING)
                                .expectedException(
                                        new PaymentCancelledException(
                                                "[ING] No callback received - payment cancelled or ignored"))
                                .build(),
                        SignTestParams.builder()
                                .callbackReceived(false)
                                .paymentStatus(PaymentStatus.USER_APPROVAL_FAILED)
                                .expectedException(
                                        new PaymentCancelledException(
                                                "[ING] No callback received - payment cancelled or ignored"))
                                .build());
        Stream<SignTestParams> unexpectedStatuses =
                Stream.of(
                                PaymentStatus.UNDEFINED,
                                PaymentStatus.CREATED,
                                PaymentStatus.SETTLEMENT_COMPLETED)
                        .map(
                                status ->
                                        SignTestParams.builder()
                                                .paymentStatus(status)
                                                .expectedException(
                                                        new PaymentAuthorizationException(
                                                                "[ING] Payment was not signed even after SCA, status: "
                                                                        + status))
                                                .build());

        List<SignTestParams> allTestParams =
                Stream.concat(expectedStatuses, unexpectedStatuses).collect(Collectors.toList());

        // sanity check
        Set<PaymentStatus> allTestedStatuses =
                allTestParams.stream()
                        .map(SignTestParams::getPaymentStatus)
                        .collect(Collectors.toSet());
        assertThat(allTestedStatuses).containsExactlyInAnyOrder(PaymentStatus.values());

        return allTestParams.stream().map(SignTestParams::toMethodParams).toArray();
    }

    @Test
    public void shouldThrowPaymentValidationExceptionWhenInstantPaymentSchemaIsNotSupported() {
        // given
        paymentExecutor = createIngPaymentExecutor(false);

        // and
        Payment payment =
                new Payment.Builder()
                        .withPaymentScheme(PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER)
                        .build();
        PaymentRequest paymentRequest = new PaymentRequest(payment);

        // when
        Throwable throwable = catchThrowable(() -> paymentExecutor.create(paymentRequest));

        // then
        assertThat(throwable)
                .isInstanceOf(PaymentValidationException.class)
                .hasMessage("Instant payment is not supported");
    }

    private IngPaymentExecutor createIngPaymentExecutor(boolean instantSepaIsSupported) {
        Annotation[] agentAnnotations = new Annotation[1];
        if (instantSepaIsSupported) {
            agentAnnotations[0] = getAgentPisCapability(PisCapability.SEPA_INSTANT_CREDIT_TRANSFER);
        } else {
            agentAnnotations[0] = getAgentPisCapability(PisCapability.SEPA_CREDIT_TRANSFER);
        }
        return new IngPaymentExecutor(
                sessionStorage,
                paymentApiClient,
                paymentAuthenticator,
                paymentMapper,
                agentAnnotations);
    }

    @Getter
    @Builder
    private static class SignTestParams {
        private final boolean callbackReceived;
        private final PaymentStatus paymentStatus;
        private final PaymentException expectedException;

        private Object[] toMethodParams() {
            return new Object[] {callbackReceived, paymentStatus, expectedException};
        }
    }

    private static Payment emptyPayment(PaymentScheme paymentScheme) {
        return new Payment.Builder().withPaymentScheme(paymentScheme).build();
    }

    @SuppressWarnings("SameParameterValue")
    private static Payment emptyPayment(PaymentServiceType paymentServiceType) {
        return new Payment.Builder().withPaymentServiceType(paymentServiceType).build();
    }

    @SuppressWarnings("SameParameterValue")
    private static Payment emptyPayment(String paymentId) {
        return new Payment.Builder().withUniqueId(paymentId).build();
    }
}
