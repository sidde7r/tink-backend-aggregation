package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.CONSENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.END_TO_END_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.INSTRUCTION_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.PAYMENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDomesticPaymentRequestForAlreadyExecutedPayment;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDomesticPaymentRequestForNotExecutedPayment;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDomesticScheduledPaymentRequestForAlreadyExecutedPayment;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDomesticScheduledPaymentRequestForNotExecutedPayment;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createFundsConfirmationResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createPaymentResponse;

import com.google.common.collect.ImmutableMap;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.common.FundsConfirmationResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentType;

public class UkOpenBankingPaymentHelperTest {

    private static final Instant NOW = Instant.now();

    private UkOpenBankingPaymentHelper ukOpenBankingPaymentHelper;

    private Clock clockMock;
    private DomesticPaymentApiClientWrapper domesticPaymentApiClientWrapperMock;
    private DomesticScheduledPaymentApiClientWrapper domesticScheduledPaymentApiClientWrapper;

    @Before
    public void setUp() {
        domesticPaymentApiClientWrapperMock = mock(DomesticPaymentApiClientWrapper.class);
        domesticScheduledPaymentApiClientWrapper =
                mock(DomesticScheduledPaymentApiClientWrapper.class);

        final Map<PaymentType, ApiClientWrapper> apiClientWrapperMap =
                ImmutableMap.of(
                        PaymentType.DOMESTIC,
                        domesticPaymentApiClientWrapperMock,
                        PaymentType.DOMESTIC_FUTURE,
                        domesticScheduledPaymentApiClientWrapper);

        clockMock = createClockMock();

        ukOpenBankingPaymentHelper = new UkOpenBankingPaymentHelper(apiClientWrapperMap, clockMock);
    }

    @Test
    public void shouldCreateConsentForDomesticPayment() {
        // given
        final PaymentRequest paymentRequestMock =
                createDomesticPaymentRequestForNotExecutedPayment(this.clockMock);
        final PaymentResponse paymentResponseMock = createPaymentResponse();

        when(domesticPaymentApiClientWrapperMock.createPaymentConsent(paymentRequestMock))
                .thenReturn(paymentResponseMock);

        // when
        final PaymentResponse returnedResponse =
                ukOpenBankingPaymentHelper.createConsent(paymentRequestMock);

        // then
        assertThat(returnedResponse).isEqualTo(paymentResponseMock);
        verify(domesticPaymentApiClientWrapperMock).createPaymentConsent(paymentRequestMock);
        verifyZeroInteractions(domesticScheduledPaymentApiClientWrapper);
    }

    @Test
    public void shouldCreateConsentForDomesticScheduledPayment() {
        // given
        final PaymentRequest paymentRequestMock =
                createDomesticScheduledPaymentRequestForNotExecutedPayment(this.clockMock);
        final PaymentResponse paymentResponseMock = createPaymentResponse();

        when(domesticScheduledPaymentApiClientWrapper.createPaymentConsent(paymentRequestMock))
                .thenReturn(paymentResponseMock);

        // when
        final PaymentResponse returnedResponse =
                ukOpenBankingPaymentHelper.createConsent(paymentRequestMock);

        // then
        assertThat(returnedResponse).isEqualTo(paymentResponseMock);
        verify(domesticScheduledPaymentApiClientWrapper).createPaymentConsent(paymentRequestMock);
        verifyZeroInteractions(domesticPaymentApiClientWrapperMock);
    }

    @Test
    public void shouldFetchAlreadyExecutedDomesticPayment() {
        // given
        final PaymentRequest paymentRequestMock =
                createDomesticPaymentRequestForAlreadyExecutedPayment(this.clockMock);
        final PaymentResponse paymentResponseMock = createPaymentResponse();

        when(domesticPaymentApiClientWrapperMock.getPayment(PAYMENT_ID))
                .thenReturn(paymentResponseMock);

        // when
        final PaymentResponse returnedResponse =
                ukOpenBankingPaymentHelper.fetchPaymentIfAlreadyExecutedOrGetConsent(
                        paymentRequestMock);

        // then
        assertThat(returnedResponse).isEqualTo(paymentResponseMock);
        verify(domesticPaymentApiClientWrapperMock).getPayment(PAYMENT_ID);
        verifyZeroInteractions(domesticPaymentApiClientWrapperMock);
    }

    @Test
    public void shouldFetchAlreadyExecutedDomesticScheduledPayment() {
        // given
        final PaymentRequest paymentRequestMock =
                createDomesticScheduledPaymentRequestForAlreadyExecutedPayment(this.clockMock);
        final PaymentResponse paymentResponseMock = createPaymentResponse();

        when(domesticScheduledPaymentApiClientWrapper.getPayment(PAYMENT_ID))
                .thenReturn(paymentResponseMock);

        // when
        final PaymentResponse returnedResponse =
                ukOpenBankingPaymentHelper.fetchPaymentIfAlreadyExecutedOrGetConsent(
                        paymentRequestMock);

        // then
        assertThat(returnedResponse).isEqualTo(paymentResponseMock);
        verify(domesticScheduledPaymentApiClientWrapper).getPayment(PAYMENT_ID);
        verifyZeroInteractions(domesticPaymentApiClientWrapperMock);
    }

    @Test
    public void shouldGetDomesticPaymentConsent() {
        // given
        final PaymentRequest paymentRequestMock =
                createDomesticPaymentRequestForNotExecutedPayment(this.clockMock);
        final PaymentResponse paymentResponseMock = createPaymentResponse();

        when(domesticPaymentApiClientWrapperMock.getPaymentConsent(CONSENT_ID))
                .thenReturn(paymentResponseMock);

        // when
        final PaymentResponse returnedResponse =
                ukOpenBankingPaymentHelper.fetchPaymentIfAlreadyExecutedOrGetConsent(
                        paymentRequestMock);

        // then
        assertThat(returnedResponse).isEqualTo(paymentResponseMock);
        verify(domesticPaymentApiClientWrapperMock).getPaymentConsent(CONSENT_ID);
        verifyZeroInteractions(domesticPaymentApiClientWrapperMock);
    }

    @Test
    public void shouldGetDomesticScheduledPaymentConsent() {
        // given
        final PaymentRequest paymentRequestMock =
                createDomesticScheduledPaymentRequestForNotExecutedPayment(this.clockMock);
        final PaymentResponse paymentResponseMock = createPaymentResponse();

        when(domesticScheduledPaymentApiClientWrapper.getPaymentConsent(CONSENT_ID))
                .thenReturn(paymentResponseMock);

        // when
        final PaymentResponse returnedResponse =
                ukOpenBankingPaymentHelper.fetchPaymentIfAlreadyExecutedOrGetConsent(
                        paymentRequestMock);

        // then
        assertThat(returnedResponse).isEqualTo(paymentResponseMock);
        verify(domesticScheduledPaymentApiClientWrapper).getPaymentConsent(CONSENT_ID);
        verifyZeroInteractions(domesticPaymentApiClientWrapperMock);
    }

    @Test
    public void shouldFetchFundsConfirmationForDomesticPayment() {
        // given
        final PaymentRequest paymentRequestMock =
                createDomesticPaymentRequestForNotExecutedPayment(this.clockMock);
        final FundsConfirmationResponse confirmationResponseMock =
                createFundsConfirmationResponse();

        when(domesticPaymentApiClientWrapperMock.getFundsConfirmation(CONSENT_ID))
                .thenReturn(Optional.of(confirmationResponseMock));

        // when
        final Optional<FundsConfirmationResponse> returnedResponse =
                ukOpenBankingPaymentHelper.fetchFundsConfirmation(paymentRequestMock);

        // then
        assertThat(returnedResponse.isPresent()).isTrue();
        returnedResponse.ifPresent(
                response -> assertThat(response).isEqualTo(confirmationResponseMock));

        verify(domesticPaymentApiClientWrapperMock).getFundsConfirmation(CONSENT_ID);
        verifyZeroInteractions(domesticScheduledPaymentApiClientWrapper);
    }

    @Test
    public void shouldNotFetchFundsConfirmationForDomesticScheduledPayment() {
        // given
        final PaymentRequest paymentRequestMock =
                createDomesticScheduledPaymentRequestForNotExecutedPayment(this.clockMock);

        when(domesticScheduledPaymentApiClientWrapper.getFundsConfirmation(CONSENT_ID))
                .thenReturn(Optional.empty());

        // when
        final Optional<FundsConfirmationResponse> returnedResponse =
                ukOpenBankingPaymentHelper.fetchFundsConfirmation(paymentRequestMock);

        // then
        assertThat(returnedResponse.isPresent()).isFalse();

        verify(domesticScheduledPaymentApiClientWrapper).getFundsConfirmation(CONSENT_ID);
        verifyZeroInteractions(domesticPaymentApiClientWrapperMock);
    }

    @Test
    public void shouldExecuteDomesticPayment() {
        // given
        final PaymentRequest paymentRequestMock =
                createDomesticPaymentRequestForNotExecutedPayment(this.clockMock);
        final PaymentResponse paymentResponseMock = createPaymentResponse();

        when(domesticPaymentApiClientWrapperMock.executePayment(
                        paymentRequestMock, CONSENT_ID, END_TO_END_ID, INSTRUCTION_ID))
                .thenReturn(paymentResponseMock);

        // when
        final PaymentResponse returnedResponse =
                ukOpenBankingPaymentHelper.executePayment(
                        paymentRequestMock, END_TO_END_ID, INSTRUCTION_ID);

        // then
        assertThat(returnedResponse).isEqualTo(paymentResponseMock);
        verify(domesticPaymentApiClientWrapperMock)
                .executePayment(paymentRequestMock, CONSENT_ID, END_TO_END_ID, INSTRUCTION_ID);
        verifyZeroInteractions(domesticScheduledPaymentApiClientWrapper);
    }

    @Test
    public void shouldExecuteDomesticScheduledPayment() {
        // given
        final PaymentRequest paymentRequestMock =
                createDomesticScheduledPaymentRequestForNotExecutedPayment(this.clockMock);
        final PaymentResponse paymentResponseMock = createPaymentResponse();

        when(domesticScheduledPaymentApiClientWrapper.executePayment(
                        paymentRequestMock, CONSENT_ID, END_TO_END_ID, INSTRUCTION_ID))
                .thenReturn(paymentResponseMock);

        // when
        final PaymentResponse returnedResponse =
                ukOpenBankingPaymentHelper.executePayment(
                        paymentRequestMock, END_TO_END_ID, INSTRUCTION_ID);

        // then
        assertThat(returnedResponse).isEqualTo(paymentResponseMock);
        verify(domesticScheduledPaymentApiClientWrapper)
                .executePayment(paymentRequestMock, CONSENT_ID, END_TO_END_ID, INSTRUCTION_ID);
        verifyZeroInteractions(domesticPaymentApiClientWrapperMock);
    }

    private static Clock createClockMock() {
        final Clock clockMock = mock(Clock.class);

        when(clockMock.instant()).thenReturn(NOW);
        when(clockMock.getZone()).thenReturn(ZoneOffset.UTC);

        return clockMock;
    }
}
