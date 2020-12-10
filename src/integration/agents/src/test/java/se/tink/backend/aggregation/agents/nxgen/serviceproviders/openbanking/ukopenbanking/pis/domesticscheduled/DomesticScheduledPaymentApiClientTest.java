package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentConstants.CONSENT_ID_KEY;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentConstants.PAYMENT_ID_KEY;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.CONSENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.END_TO_END_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.INSTRUCTION_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.PAYMENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createClockMock;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDomesticScheduledPaymentConsentResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDomesticScheduledPaymentRequestForNotExecutedPayment;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDomesticScheduledPaymentResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.DomesticScheduledPaymentApiClient.PAYMENT;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.DomesticScheduledPaymentApiClient.PAYMENT_CONSENT;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.DomesticScheduledPaymentApiClient.PAYMENT_CONSENT_STATUS;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.DomesticScheduledPaymentApiClient.PAYMENT_STATUS;

import java.time.Clock;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.converter.DomesticScheduledPaymentConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.dto.DomesticScheduledPaymentConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.dto.DomesticScheduledPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.dto.DomesticScheduledPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.dto.DomesticScheduledPaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class DomesticScheduledPaymentApiClientTest {

    private static final String API_BASE_URL = "/api/base/url";

    private DomesticScheduledPaymentApiClient apiClient;
    private UkOpenBankingRequestBuilder ukOpenBankingRequestBuilder;
    private DomesticScheduledPaymentConverter paymentConverter;
    private Clock clockMock;

    @Before
    public void setUp() {
        ukOpenBankingRequestBuilder = mock(UkOpenBankingRequestBuilder.class);
        paymentConverter = mock(DomesticScheduledPaymentConverter.class);
        apiClient =
                new DomesticScheduledPaymentApiClient(
                        ukOpenBankingRequestBuilder, paymentConverter, API_BASE_URL);

        clockMock = createClockMock();
    }

    @Test
    public void shouldGetPayment() {
        // given
        final DomesticScheduledPaymentResponse response = createDomesticScheduledPaymentResponse();

        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);
        when(requestBuilderMock.get(eq(DomesticScheduledPaymentResponse.class)))
                .thenReturn(response);

        final URL url =
                new URL(API_BASE_URL + PAYMENT_STATUS).parameter(PAYMENT_ID_KEY, PAYMENT_ID);
        when(ukOpenBankingRequestBuilder.createPisRequest(eq(url))).thenReturn(requestBuilderMock);

        // when
        apiClient.getPayment(PAYMENT_ID);

        // then
        verify(requestBuilderMock).get(eq(DomesticScheduledPaymentResponse.class));
        verify(ukOpenBankingRequestBuilder).createPisRequest(eq(url));
        verify(paymentConverter).convertResponseDtoToPaymentResponse(response);
    }

    @Test
    public void shouldCreatePaymentConsent() {
        // given
        final PaymentRequest paymentRequestMock =
                createDomesticScheduledPaymentRequestForNotExecutedPayment(this.clockMock);
        final DomesticScheduledPaymentConsentResponse response =
                createDomesticScheduledPaymentConsentResponse();

        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);
        when(requestBuilderMock.post(
                        eq(DomesticScheduledPaymentConsentResponse.class),
                        any(DomesticScheduledPaymentConsentRequest.class)))
                .thenReturn(response);

        final URL url = new URL(API_BASE_URL + PAYMENT_CONSENT);
        when(ukOpenBankingRequestBuilder.createPisRequestWithJwsHeader(
                        eq(url), any(DomesticScheduledPaymentConsentRequest.class)))
                .thenReturn(requestBuilderMock);

        // when
        apiClient.createPaymentConsent(paymentRequestMock);

        // then
        verify(requestBuilderMock)
                .post(
                        eq(DomesticScheduledPaymentConsentResponse.class),
                        any(DomesticScheduledPaymentConsentRequest.class));
        verify(ukOpenBankingRequestBuilder)
                .createPisRequestWithJwsHeader(
                        eq(url), any(DomesticScheduledPaymentConsentRequest.class));
        verify(paymentConverter).convertConsentResponseDtoToTinkPaymentResponse(response);
    }

    @Test
    public void shouldGetPaymentConsent() {
        // given
        final DomesticScheduledPaymentConsentResponse response =
                createDomesticScheduledPaymentConsentResponse();

        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);
        when(requestBuilderMock.get(eq(DomesticScheduledPaymentConsentResponse.class)))
                .thenReturn(response);

        final URL url =
                new URL(API_BASE_URL + PAYMENT_CONSENT_STATUS)
                        .parameter(CONSENT_ID_KEY, CONSENT_ID);
        when(ukOpenBankingRequestBuilder.createPisRequest(eq(url))).thenReturn(requestBuilderMock);

        // when
        apiClient.getPaymentConsent(CONSENT_ID);

        // then
        verify(requestBuilderMock).get(eq(DomesticScheduledPaymentConsentResponse.class));
        verify(ukOpenBankingRequestBuilder).createPisRequest(eq(url));
        verify(paymentConverter).convertConsentResponseDtoToTinkPaymentResponse(response);
    }

    @Test
    public void shouldExecutePayment() {
        // given
        final PaymentRequest paymentRequestMock =
                createDomesticScheduledPaymentRequestForNotExecutedPayment(this.clockMock);
        final DomesticScheduledPaymentResponse response = createDomesticScheduledPaymentResponse();

        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);
        when(requestBuilderMock.post(
                        eq(DomesticScheduledPaymentResponse.class),
                        any(DomesticScheduledPaymentRequest.class)))
                .thenReturn(response);

        final URL url = new URL(API_BASE_URL + PAYMENT);
        when(ukOpenBankingRequestBuilder.createPisRequestWithJwsHeader(
                        eq(url), any(DomesticScheduledPaymentRequest.class)))
                .thenReturn(requestBuilderMock);

        // when
        apiClient.executePayment(paymentRequestMock, CONSENT_ID, END_TO_END_ID, INSTRUCTION_ID);

        // then
        verify(requestBuilderMock)
                .post(
                        eq(DomesticScheduledPaymentResponse.class),
                        any(DomesticScheduledPaymentRequest.class));
        verify(ukOpenBankingRequestBuilder)
                .createPisRequestWithJwsHeader(eq(url), any(DomesticScheduledPaymentRequest.class));
        verify(paymentConverter).convertResponseDtoToPaymentResponse(response);
    }
}
