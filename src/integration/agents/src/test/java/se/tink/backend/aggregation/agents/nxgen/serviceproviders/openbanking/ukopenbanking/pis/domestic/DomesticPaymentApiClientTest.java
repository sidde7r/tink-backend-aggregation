package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.CONSENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.END_TO_END_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.INSTRUCTION_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.PAYMENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createClockMock;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDomesticPaymentConsentResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDomesticPaymentRequestForNotExecutedPayment;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDomesticPaymentResponse;

import java.time.Clock;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.configuration.UkOpenBankingPisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.converter.DomesticPaymentConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class DomesticPaymentApiClientTest {

    private DomesticPaymentApiClient apiClient;
    private UkOpenBankingRequestBuilder ukOpenBankingRequestBuilder;
    private DomesticPaymentConverter paymentConverter;
    private UkOpenBankingPisConfig pisConfig;
    private Clock clockMock;

    @Before
    public void setUp() {
        ukOpenBankingRequestBuilder = mock(UkOpenBankingRequestBuilder.class);
        paymentConverter = mock(DomesticPaymentConverter.class);
        pisConfig = mock(UkOpenBankingPisConfig.class);
        apiClient =
                new DomesticPaymentApiClient(
                        ukOpenBankingRequestBuilder, paymentConverter, pisConfig);

        clockMock = createClockMock();
    }

    @Test
    public void shouldGetPayment() {
        // given
        final DomesticPaymentResponse response = createDomesticPaymentResponse();

        final URL dummyUrl = new URL("dummy.url");
        when(pisConfig.getDomesticPayment(PAYMENT_ID)).thenReturn(dummyUrl);

        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);
        when(requestBuilderMock.get(eq(DomesticPaymentResponse.class))).thenReturn(response);

        when(ukOpenBankingRequestBuilder.createPisRequest(eq(dummyUrl)))
                .thenReturn(requestBuilderMock);

        // when
        apiClient.getPayment(PAYMENT_ID);

        // then
        verify(requestBuilderMock).get(eq(DomesticPaymentResponse.class));
        verify(ukOpenBankingRequestBuilder).createPisRequest(eq(dummyUrl));
        verify(paymentConverter).convertResponseDtoToPaymentResponse(response);
    }

    @Test
    public void shouldCreatePaymentConsent() {
        // given
        final PaymentRequest paymentRequestMock =
                createDomesticPaymentRequestForNotExecutedPayment(this.clockMock);
        final DomesticPaymentConsentResponse response = createDomesticPaymentConsentResponse();

        final URL dummyUrl = new URL("dummy.url");
        when(pisConfig.createDomesticPaymentConsentURL()).thenReturn(dummyUrl);

        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);
        when(requestBuilderMock.post(
                        eq(DomesticPaymentConsentResponse.class),
                        any(DomesticPaymentConsentRequest.class)))
                .thenReturn(response);

        when(ukOpenBankingRequestBuilder.createPisRequestWithJwsHeader(
                        eq(dummyUrl), any(DomesticPaymentConsentRequest.class)))
                .thenReturn(requestBuilderMock);

        // when
        apiClient.createPaymentConsent(paymentRequestMock);

        // then
        verify(requestBuilderMock)
                .post(
                        eq(DomesticPaymentConsentResponse.class),
                        any(DomesticPaymentConsentRequest.class));
        verify(ukOpenBankingRequestBuilder)
                .createPisRequestWithJwsHeader(
                        eq(dummyUrl), any(DomesticPaymentConsentRequest.class));
        verify(paymentConverter).convertConsentResponseDtoToTinkPaymentResponse(response);
    }

    @Test
    public void shouldGetPaymentConsent() {
        // given
        final DomesticPaymentConsentResponse response = createDomesticPaymentConsentResponse();

        final URL dummyUrl = new URL("dummy.url");
        when(pisConfig.getDomesticPaymentConsentURL(CONSENT_ID)).thenReturn(dummyUrl);

        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);
        when(requestBuilderMock.get(eq(DomesticPaymentConsentResponse.class))).thenReturn(response);

        when(ukOpenBankingRequestBuilder.createPisRequest(eq(dummyUrl)))
                .thenReturn(requestBuilderMock);

        // when
        apiClient.getPaymentConsent(CONSENT_ID);

        // then
        verify(requestBuilderMock).get(eq(DomesticPaymentConsentResponse.class));
        verify(ukOpenBankingRequestBuilder).createPisRequest(eq(dummyUrl));
        verify(paymentConverter).convertConsentResponseDtoToTinkPaymentResponse(response);
    }

    @Test
    public void shouldExecutePayment() {
        // given
        final PaymentRequest paymentRequestMock =
                createDomesticPaymentRequestForNotExecutedPayment(this.clockMock);
        final DomesticPaymentResponse response = createDomesticPaymentResponse();

        final URL dummyUrl = new URL("dummy.url");
        when(pisConfig.createDomesticPaymentURL()).thenReturn(dummyUrl);

        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);
        when(requestBuilderMock.post(
                        eq(DomesticPaymentResponse.class), any(DomesticPaymentRequest.class)))
                .thenReturn(response);

        when(ukOpenBankingRequestBuilder.createPisRequestWithJwsHeader(
                        eq(dummyUrl), any(DomesticPaymentRequest.class)))
                .thenReturn(requestBuilderMock);

        // when
        apiClient.executePayment(paymentRequestMock, CONSENT_ID, END_TO_END_ID, INSTRUCTION_ID);

        // then
        verify(requestBuilderMock)
                .post(eq(DomesticPaymentResponse.class), any(DomesticPaymentRequest.class));
        verify(ukOpenBankingRequestBuilder)
                .createPisRequestWithJwsHeader(eq(dummyUrl), any(DomesticPaymentRequest.class));
        verify(paymentConverter).convertResponseDtoToPaymentResponse(response);
    }
}
