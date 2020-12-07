package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled;

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
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDomesticScheduledPaymentConsentResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDomesticScheduledPaymentRequestForNotExecutedPayment;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDomesticScheduledPaymentResponse;

import java.time.Clock;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.configuration.UkOpenBankingPisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.converter.DomesticScheduledPaymentConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.dto.DomesticScheduledPaymentConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.dto.DomesticScheduledPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.dto.DomesticScheduledPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.dto.DomesticScheduledPaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class DomesticScheduledPaymentApiClientTest {

    private DomesticScheduledPaymentApiClient apiClient;
    private UkOpenBankingRequestBuilder ukOpenBankingRequestBuilder;
    private DomesticScheduledPaymentConverter paymentConverter;
    private UkOpenBankingPisConfig pisConfig;
    private Clock clockMock;

    @Before
    public void setUp() {
        ukOpenBankingRequestBuilder = mock(UkOpenBankingRequestBuilder.class);
        paymentConverter = mock(DomesticScheduledPaymentConverter.class);
        pisConfig = mock(UkOpenBankingPisConfig.class);
        apiClient =
                new DomesticScheduledPaymentApiClient(
                        ukOpenBankingRequestBuilder, paymentConverter, pisConfig);

        clockMock = createClockMock();
    }

    @Test
    public void shouldGetPayment() {
        // given
        final DomesticScheduledPaymentResponse response = createDomesticScheduledPaymentResponse();

        final URL dummyUrl = new URL("dummy.url");
        when(pisConfig.getDomesticScheduledPayment(PAYMENT_ID)).thenReturn(dummyUrl);

        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);
        when(requestBuilderMock.get(eq(DomesticScheduledPaymentResponse.class)))
                .thenReturn(response);

        when(ukOpenBankingRequestBuilder.createPisRequest(eq(dummyUrl)))
                .thenReturn(requestBuilderMock);

        // when
        apiClient.getPayment(PAYMENT_ID);

        // then
        verify(requestBuilderMock).get(eq(DomesticScheduledPaymentResponse.class));
        verify(ukOpenBankingRequestBuilder).createPisRequest(eq(dummyUrl));
        verify(paymentConverter).convertResponseDtoToPaymentResponse(response);
    }

    @Test
    public void shouldCreatePaymentConsent() {
        // given
        final PaymentRequest paymentRequestMock =
                createDomesticScheduledPaymentRequestForNotExecutedPayment(this.clockMock);
        final DomesticScheduledPaymentConsentResponse response =
                createDomesticScheduledPaymentConsentResponse();

        final URL dummyUrl = new URL("dummy.url");
        when(pisConfig.createDomesticScheduledPaymentConsentURL()).thenReturn(dummyUrl);

        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);
        when(requestBuilderMock.post(
                        eq(DomesticScheduledPaymentConsentResponse.class),
                        any(DomesticScheduledPaymentConsentRequest.class)))
                .thenReturn(response);

        when(ukOpenBankingRequestBuilder.createPisRequestWithJwsHeader(
                        eq(dummyUrl), any(DomesticScheduledPaymentConsentRequest.class)))
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
                        eq(dummyUrl), any(DomesticScheduledPaymentConsentRequest.class));
        verify(paymentConverter).convertConsentResponseDtoToTinkPaymentResponse(response);
    }

    @Test
    public void shouldGetPaymentConsent() {
        // given
        final DomesticScheduledPaymentConsentResponse response =
                createDomesticScheduledPaymentConsentResponse();

        final URL dummyUrl = new URL("dummy.url");
        when(pisConfig.getDomesticScheduledPaymentConsentURL(CONSENT_ID)).thenReturn(dummyUrl);

        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);
        when(requestBuilderMock.get(eq(DomesticScheduledPaymentConsentResponse.class)))
                .thenReturn(response);

        when(ukOpenBankingRequestBuilder.createPisRequest(eq(dummyUrl)))
                .thenReturn(requestBuilderMock);

        // when
        apiClient.getPaymentConsent(CONSENT_ID);

        // then
        verify(requestBuilderMock).get(eq(DomesticScheduledPaymentConsentResponse.class));
        verify(ukOpenBankingRequestBuilder).createPisRequest(eq(dummyUrl));
        verify(paymentConverter).convertConsentResponseDtoToTinkPaymentResponse(response);
    }

    @Test
    public void shouldExecutePayment() {
        // given
        final PaymentRequest paymentRequestMock =
                createDomesticScheduledPaymentRequestForNotExecutedPayment(this.clockMock);
        final DomesticScheduledPaymentResponse response = createDomesticScheduledPaymentResponse();

        final URL dummyUrl = new URL("dummy.url");
        when(pisConfig.createDomesticScheduledPaymentURL()).thenReturn(dummyUrl);

        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);
        when(requestBuilderMock.post(
                        eq(DomesticScheduledPaymentResponse.class),
                        any(DomesticScheduledPaymentRequest.class)))
                .thenReturn(response);

        when(ukOpenBankingRequestBuilder.createPisRequestWithJwsHeader(
                        eq(dummyUrl), any(DomesticScheduledPaymentRequest.class)))
                .thenReturn(requestBuilderMock);

        // when
        apiClient.executePayment(paymentRequestMock, CONSENT_ID, END_TO_END_ID, INSTRUCTION_ID);

        // then
        verify(requestBuilderMock)
                .post(
                        eq(DomesticScheduledPaymentResponse.class),
                        any(DomesticScheduledPaymentRequest.class));
        verify(ukOpenBankingRequestBuilder)
                .createPisRequestWithJwsHeader(
                        eq(dummyUrl), any(DomesticScheduledPaymentRequest.class));
        verify(paymentConverter).convertResponseDtoToPaymentResponse(response);
    }
}
