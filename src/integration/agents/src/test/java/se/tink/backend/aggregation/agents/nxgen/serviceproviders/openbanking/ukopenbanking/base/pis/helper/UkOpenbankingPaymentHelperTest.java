package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.helper.UkOpenbankingPaymentTestFixtures.CONSENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.helper.UkOpenbankingPaymentTestFixtures.END_TO_END_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.helper.UkOpenbankingPaymentTestFixtures.INSTRUCTION_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.helper.UkOpenbankingPaymentTestFixtures.PAYMENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.helper.UkOpenbankingPaymentTestFixtures.createDomesticPaymentConsentResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.helper.UkOpenbankingPaymentTestFixtures.createDomesticPaymentRequestForAlreadyExecutedPayment;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.helper.UkOpenbankingPaymentTestFixtures.createDomesticPaymentRequestForNotExecutedPayment;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.helper.UkOpenbankingPaymentTestFixtures.createDomesticPaymentResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.helper.UkOpenbankingPaymentTestFixtures.createDomesticScheduledPaymentConsentResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.helper.UkOpenbankingPaymentTestFixtures.createDomesticScheduledPaymentRequestForAlreadyExecutedPayment;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.helper.UkOpenbankingPaymentTestFixtures.createDomesticScheduledPaymentRequestForNotExecutedPayment;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.helper.UkOpenbankingPaymentTestFixtures.createDomesticScheduledPaymentResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.helper.UkOpenbankingPaymentTestFixtures.createFundsConfirmationResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.helper.UkOpenbankingPaymentTestFixtures.createPaymentResponse;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.rpc.domestic.DomesticPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.rpc.domestic.DomesticPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.rpc.domestic.DomesticScheduledPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.rpc.domestic.DomesticScheduledPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.rpc.international.FundsConfirmationResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;

public class UkOpenbankingPaymentHelperTest {

    private static final Instant NOW = Instant.now();

    private UkOpenbankingPaymentHelper ukOpenbankingPaymentHelper;

    private UkOpenBankingApiClient apiClientMock;
    private Clock clockMock;

    @Before
    public void setUp() {
        apiClientMock = mock(UkOpenBankingApiClient.class);
        clockMock = createClockMock();

        ukOpenbankingPaymentHelper = new UkOpenbankingPaymentHelper(apiClientMock, clockMock);
    }

    @Test
    public void shouldCreateConsentForDomesticScheduledPayment() {
        // given
        final PaymentRequest paymentRequestMock =
                createDomesticScheduledPaymentRequestForNotExecutedPayment(this.clockMock);
        final PaymentResponse paymentResponseMock = createPaymentResponse();
        final DomesticScheduledPaymentConsentResponse domesticScheduledPaymentConsentResponseMock =
                createDomesticScheduledPaymentConsentResponse(paymentResponseMock);

        when(apiClientMock.createDomesticScheduledPaymentConsent(any(), any()))
                .thenReturn(domesticScheduledPaymentConsentResponseMock);

        // when
        final PaymentResponse returnedResponse =
                ukOpenbankingPaymentHelper.createConsent(paymentRequestMock);

        // then
        assertThat(returnedResponse).isEqualTo(paymentResponseMock);
        verify(apiClientMock).createDomesticScheduledPaymentConsent(any(), any());
    }

    @Test
    public void shouldFetchAlreadyExecutedDomesticPayment() {
        // given
        final PaymentRequest paymentRequestMock =
                createDomesticPaymentRequestForAlreadyExecutedPayment(this.clockMock);
        final PaymentResponse paymentResponseMock = createPaymentResponse();
        final DomesticPaymentResponse domesticPaymentResponseMock =
                createDomesticPaymentResponse(paymentResponseMock);

        when(apiClientMock.getDomesticPayment(PAYMENT_ID, DomesticPaymentResponse.class))
                .thenReturn(domesticPaymentResponseMock);

        // when
        final PaymentResponse returnedResponse =
                ukOpenbankingPaymentHelper.fetchPaymentIfAlreadyExecutedOrGetConsent(
                        paymentRequestMock);

        // then
        assertThat(returnedResponse).isEqualTo(paymentResponseMock);
        verify(apiClientMock).getDomesticPayment(PAYMENT_ID, DomesticPaymentResponse.class);
        verify(apiClientMock, never()).getDomesticPaymentConsent(anyString(), any());
    }

    @Test
    public void shouldFetchAlreadyExecutedDomesticScheduledPayment() {
        // given
        final PaymentRequest paymentRequestMock =
                createDomesticScheduledPaymentRequestForAlreadyExecutedPayment(this.clockMock);
        final PaymentResponse paymentResponseMock = createPaymentResponse();
        final DomesticScheduledPaymentResponse domesticScheduledPaymentResponseMock =
                createDomesticScheduledPaymentResponse(paymentResponseMock);

        when(apiClientMock.getDomesticScheduledPayment(
                        PAYMENT_ID, DomesticScheduledPaymentResponse.class))
                .thenReturn(domesticScheduledPaymentResponseMock);

        // when
        final PaymentResponse returnedResponse =
                ukOpenbankingPaymentHelper.fetchPaymentIfAlreadyExecutedOrGetConsent(
                        paymentRequestMock);

        // then
        assertThat(returnedResponse).isEqualTo(paymentResponseMock);
        verify(apiClientMock)
                .getDomesticScheduledPayment(PAYMENT_ID, DomesticScheduledPaymentResponse.class);
        verify(apiClientMock, never()).getDomesticScheduledPaymentConsent(anyString(), any());
    }

    @Test
    public void shouldGetDomesticPaymentConsent() {
        // given
        final PaymentRequest paymentRequestMock =
                createDomesticPaymentRequestForNotExecutedPayment(this.clockMock);
        final PaymentResponse paymentResponseMock = createPaymentResponse();
        final DomesticPaymentConsentResponse domesticPaymentConsentResponseMock =
                createDomesticPaymentConsentResponse(paymentResponseMock);

        when(apiClientMock.getDomesticPaymentConsent(
                        CONSENT_ID, DomesticPaymentConsentResponse.class))
                .thenReturn(domesticPaymentConsentResponseMock);

        // when
        final PaymentResponse returnedResponse =
                ukOpenbankingPaymentHelper.fetchPaymentIfAlreadyExecutedOrGetConsent(
                        paymentRequestMock);

        // then
        assertThat(returnedResponse).isEqualTo(paymentResponseMock);
        verify(apiClientMock, never()).getDomesticPayment(anyString(), any());
        verify(apiClientMock)
                .getDomesticPaymentConsent(CONSENT_ID, DomesticPaymentConsentResponse.class);
    }

    @Test
    public void shouldGetDomesticScheduledPaymentConsent() {
        // given
        final PaymentRequest paymentRequestMock =
                createDomesticScheduledPaymentRequestForNotExecutedPayment(this.clockMock);
        final PaymentResponse paymentResponseMock = createPaymentResponse();
        final DomesticScheduledPaymentConsentResponse domesticScheduledPaymentConsentResponseMock =
                createDomesticScheduledPaymentConsentResponse(paymentResponseMock);

        when(apiClientMock.getDomesticScheduledPaymentConsent(
                        CONSENT_ID, DomesticScheduledPaymentConsentResponse.class))
                .thenReturn(domesticScheduledPaymentConsentResponseMock);

        // when
        final PaymentResponse returnedResponse =
                ukOpenbankingPaymentHelper.fetchPaymentIfAlreadyExecutedOrGetConsent(
                        paymentRequestMock);

        // then
        assertThat(returnedResponse).isEqualTo(paymentResponseMock);
        verify(apiClientMock, never()).getDomesticScheduledPayment(anyString(), any());
        verify(apiClientMock)
                .getDomesticScheduledPaymentConsent(
                        CONSENT_ID, DomesticScheduledPaymentConsentResponse.class);
    }

    @Test
    public void shouldFetchFundsConfirmationForDomesticPayment() {
        // given
        final PaymentRequest paymentRequestMock =
                createDomesticPaymentRequestForNotExecutedPayment(this.clockMock);
        final FundsConfirmationResponse confirmationResponseMock =
                createFundsConfirmationResponse();

        when(apiClientMock.getDomesticFundsConfirmation(
                        CONSENT_ID, FundsConfirmationResponse.class))
                .thenReturn(confirmationResponseMock);

        // when
        final Optional<FundsConfirmationResponse> returnedResponse =
                ukOpenbankingPaymentHelper.fetchFundsConfirmation(paymentRequestMock);

        // then
        assertThat(returnedResponse.isPresent()).isTrue();
        returnedResponse.ifPresent(
                response -> assertThat(response).isEqualTo(confirmationResponseMock));

        verify(apiClientMock)
                .getDomesticFundsConfirmation(CONSENT_ID, FundsConfirmationResponse.class);
    }

    @Test
    public void shouldNotFetchFundsConfirmationForDomesticScheduledPayment() {
        // given
        final PaymentRequest paymentRequestMock =
                createDomesticScheduledPaymentRequestForNotExecutedPayment(this.clockMock);

        // when
        final Optional<FundsConfirmationResponse> returnedResponse =
                ukOpenbankingPaymentHelper.fetchFundsConfirmation(paymentRequestMock);

        // then
        assertThat(returnedResponse.isPresent()).isFalse();

        verifyZeroInteractions(apiClientMock);
    }

    @Test
    public void shouldExecuteDomesticPayment() {
        // given
        final PaymentRequest paymentRequestMock =
                createDomesticPaymentRequestForNotExecutedPayment(this.clockMock);
        final PaymentResponse paymentResponseMock = createPaymentResponse();
        final DomesticPaymentResponse domesticPaymentResponseMock =
                createDomesticPaymentResponse(paymentResponseMock);

        when(apiClientMock.executeDomesticPayment(any(), any()))
                .thenReturn(domesticPaymentResponseMock);

        // when
        final PaymentResponse returnedResponse =
                ukOpenbankingPaymentHelper.executePayment(
                        paymentRequestMock, END_TO_END_ID, INSTRUCTION_ID);

        // then
        assertThat(returnedResponse).isEqualTo(paymentResponseMock);
        verify(apiClientMock).executeDomesticPayment(any(), any());
    }

    @Test
    public void shouldExecuteDomesticScheduledPayment() {
        // given
        final PaymentRequest paymentRequestMock =
                createDomesticScheduledPaymentRequestForNotExecutedPayment(this.clockMock);
        final PaymentResponse paymentResponseMock = createPaymentResponse();
        final DomesticScheduledPaymentResponse domesticScheduledPaymentResponseMock =
                createDomesticScheduledPaymentResponse(paymentResponseMock);

        when(apiClientMock.executeDomesticScheduledPayment(any(), any()))
                .thenReturn(domesticScheduledPaymentResponseMock);

        // when
        final PaymentResponse returnedResponse =
                ukOpenbankingPaymentHelper.executePayment(
                        paymentRequestMock, END_TO_END_ID, INSTRUCTION_ID);

        // then
        assertThat(returnedResponse).isEqualTo(paymentResponseMock);
        verify(apiClientMock).executeDomesticScheduledPayment(any(), any());
    }

    private static Clock createClockMock() {
        final Clock clockMock = mock(Clock.class);

        when(clockMock.instant()).thenReturn(NOW);
        when(clockMock.getZone()).thenReturn(ZoneOffset.UTC);

        return clockMock;
    }
}
