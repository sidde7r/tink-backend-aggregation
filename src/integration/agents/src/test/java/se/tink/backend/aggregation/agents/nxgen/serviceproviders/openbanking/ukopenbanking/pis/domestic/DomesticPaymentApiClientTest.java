package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic;

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
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDomesticPaymentConsentResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDomesticPaymentRequestForNotExecutedPayment;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDomesticPaymentResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.DomesticPaymentApiClient.PAYMENT;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.DomesticPaymentApiClient.PAYMENT_CONSENT;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.DomesticPaymentApiClient.PAYMENT_CONSENT_STATUS;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.DomesticPaymentApiClient.PAYMENT_STATUS;

import java.time.Clock;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.UkOpenBankingRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.converter.DomesticPaymentConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class DomesticPaymentApiClientTest {

    private static final String API_BASE_URL = "/api/base/url";

    private DomesticPaymentApiClient apiClient;
    private UkOpenBankingRequestBuilder ukOpenBankingRequestBuilder;
    private DomesticPaymentConverter paymentConverter;
    private Clock clockMock;

    @Before
    public void setUp() {
        ukOpenBankingRequestBuilder = mock(UkOpenBankingRequestBuilder.class);
        paymentConverter = mock(DomesticPaymentConverter.class);
        apiClient =
                new DomesticPaymentApiClient(
                        ukOpenBankingRequestBuilder, paymentConverter, API_BASE_URL);

        clockMock = createClockMock();
    }

    @Test
    public void shouldGetPayment() {
        // given
        final DomesticPaymentResponse response = createDomesticPaymentResponse();

        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);
        when(requestBuilderMock.get(eq(DomesticPaymentResponse.class))).thenReturn(response);

        final URL url =
                new URL(API_BASE_URL + PAYMENT_STATUS).parameter(PAYMENT_ID_KEY, PAYMENT_ID);
        when(ukOpenBankingRequestBuilder.createPisRequest(eq(url))).thenReturn(requestBuilderMock);

        // when
        apiClient.getPayment(PAYMENT_ID);

        // then
        verify(requestBuilderMock).get(eq(DomesticPaymentResponse.class));
        verify(ukOpenBankingRequestBuilder).createPisRequest(eq(url));
        verify(paymentConverter).convertResponseDtoToPaymentResponse(response);
    }

    @Test
    public void shouldCreatePaymentConsent() {
        // given
        final PaymentRequest paymentRequestMock =
                createDomesticPaymentRequestForNotExecutedPayment(this.clockMock);
        final DomesticPaymentConsentResponse response = createDomesticPaymentConsentResponse();

        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);
        when(requestBuilderMock.post(
                        eq(DomesticPaymentConsentResponse.class),
                        any(DomesticPaymentConsentRequest.class)))
                .thenReturn(response);

        final URL url = new URL(API_BASE_URL + PAYMENT_CONSENT);
        when(ukOpenBankingRequestBuilder.createPisRequestWithJwsHeader(eq(url)))
                .thenReturn(requestBuilderMock);

        // when
        apiClient.createPaymentConsent(paymentRequestMock);

        // then
        verify(requestBuilderMock)
                .post(
                        eq(DomesticPaymentConsentResponse.class),
                        any(DomesticPaymentConsentRequest.class));
        verify(ukOpenBankingRequestBuilder).createPisRequestWithJwsHeader(eq(url));
        verify(paymentConverter).convertConsentResponseDtoToTinkPaymentResponse(response);
    }

    @Test
    public void shouldGetPaymentConsent() {
        // given
        final DomesticPaymentConsentResponse response = createDomesticPaymentConsentResponse();

        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);
        when(requestBuilderMock.get(eq(DomesticPaymentConsentResponse.class))).thenReturn(response);

        final URL url =
                new URL(API_BASE_URL + PAYMENT_CONSENT_STATUS)
                        .parameter(CONSENT_ID_KEY, CONSENT_ID);
        when(ukOpenBankingRequestBuilder.createPisRequest(eq(url))).thenReturn(requestBuilderMock);

        // when
        apiClient.getPaymentConsent(CONSENT_ID);

        // then
        verify(requestBuilderMock).get(eq(DomesticPaymentConsentResponse.class));
        verify(ukOpenBankingRequestBuilder).createPisRequest(eq(url));
        verify(paymentConverter).convertConsentResponseDtoToTinkPaymentResponse(response);
    }

    @Test
    public void shouldExecutePayment() {
        // given
        final PaymentRequest paymentRequestMock =
                createDomesticPaymentRequestForNotExecutedPayment(this.clockMock);
        final DomesticPaymentResponse response = createDomesticPaymentResponse();

        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);
        when(requestBuilderMock.post(
                        eq(DomesticPaymentResponse.class), any(DomesticPaymentRequest.class)))
                .thenReturn(response);

        final URL url = new URL(API_BASE_URL + PAYMENT);
        when(ukOpenBankingRequestBuilder.createPisRequestWithJwsHeader(eq(url)))
                .thenReturn(requestBuilderMock);

        // when
        apiClient.executePayment(paymentRequestMock, CONSENT_ID, END_TO_END_ID, INSTRUCTION_ID);

        // then
        verify(requestBuilderMock)
                .post(eq(DomesticPaymentResponse.class), any(DomesticPaymentRequest.class));
        verify(ukOpenBankingRequestBuilder).createPisRequestWithJwsHeader(eq(url));
        verify(paymentConverter).convertResponseDtoToPaymentResponse(response);
    }
}
