package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.google.common.collect.ImmutableList;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.configuration.LclConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment.rpc.ConfirmablePayment;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment.rpc.PaymentRequestResource;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.SupplementaryDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RunWith(MockitoJUnitRunner.class)
public class LclPaymentApiClientTest {

    @Mock private LclConfiguration lclConfiguration;
    @Mock private ConfirmablePayment getPaymentRequest;
    @Mock private PaymentRequestResource paymentRequestResource;
    @Mock private GetPaymentResponse expectedPaymentResponse;
    @Mock private RequestBuilder confirmRequestBuilder;
    @Mock private PaymentRequestResource confirmingPaymentRequestResource;
    @Mock private ConfirmablePayment confirmingGetPaymentRequest;
    @Mock private CreatePaymentRequest createPaymentRequest;
    @Mock private SupplementaryDataEntity dataEntity;
    @Mock private RequestBuilder requestBuilder;
    @Mock private HttpResponse httpResponse;
    @Mock private MultivaluedMap<String, String> headers;
    @Mock private CreatePaymentResponse expectedResponse;
    @Mock private SessionStorage sessionStorage;
    @Mock private AgentConfiguration<LclConfiguration> agentConfiguration;
    @Mock private LclRequestFactory lclRequestFactory;
    @Mock private TokenFetcher tokenFetcher;
    private LclPaymentApiClient paymentApiClient;

    @Before
    public void setUp() throws Exception {
        paymentApiClient =
                new LclPaymentApiClient(
                        sessionStorage, agentConfiguration, lclRequestFactory, tokenFetcher);
    }

    @Test
    public void shouldCreatePaymentRequestAndExecuteIt() {
        // given:
        given(dataEntity.getSuccessfulReportUrl()).willReturn("testUrl");
        given(createPaymentRequest.getSupplementaryData()).willReturn(dataEntity);
        given(lclRequestFactory.createPaymentRequest(createPaymentRequest))
                .willReturn(requestBuilder);
        given(requestBuilder.post(eq(HttpResponse.class))).willReturn(httpResponse);
        given(headers.get("location")).willReturn(ImmutableList.of("ignore/ignore/locationValue"));
        given(httpResponse.getHeaders()).willReturn(headers);
        given(httpResponse.getBody(CreatePaymentResponse.class)).willReturn(expectedResponse);

        // when:
        CreatePaymentResponse actualResponse = paymentApiClient.createPayment(createPaymentRequest);

        // then:
        then(sessionStorage).should().put(eq("FULL_REDIRECT_URL"), eq("testUrl&code=code"));
        then(lclRequestFactory).should().createPaymentRequest(createPaymentRequest);
        then(requestBuilder).should().post(eq(HttpResponse.class));
        then(sessionStorage).should().put(eq("PAYMENT_ID"), eq("locationValue"));
        then(httpResponse).should().getBody(eq(CreatePaymentResponse.class));
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    public void shouldThrowExceptionWhenThereIsNoLocationInResponse() {
        // given:
        given(dataEntity.getSuccessfulReportUrl()).willReturn("testUrl");
        given(createPaymentRequest.getSupplementaryData()).willReturn(dataEntity);
        given(lclRequestFactory.createPaymentRequest(createPaymentRequest))
                .willReturn(requestBuilder);
        given(requestBuilder.post(eq(HttpResponse.class))).willReturn(httpResponse);
        given(headers.get("location")).willReturn(ImmutableList.of());
        given(httpResponse.getHeaders()).willReturn(headers);

        // when:
        Throwable throwable =
                catchThrowable(() -> paymentApiClient.createPayment(createPaymentRequest));

        // then:
        then(sessionStorage).should().put(eq("FULL_REDIRECT_URL"), eq("testUrl&code=code"));
        then(lclRequestFactory).should().createPaymentRequest(createPaymentRequest);
        then(requestBuilder).should().post(eq(HttpResponse.class));

        assertThat(throwable).isNotNull();
        assertThat(throwable).isInstanceOf(MissingLocationException.class);
    }

    @Test
    public void shouldFetchToken() {
        // when:
        paymentApiClient.fetchToken();

        // then:
        then(tokenFetcher).should().fetchToken();
    }

    @Test
    public void shouldFindPayment() {

        String authUrl = "http://something.com/redirect?id=123";
        given(lclConfiguration.getClientId()).willReturn("clientIdValue");
        given(agentConfiguration.getProviderSpecificConfiguration()).willReturn(lclConfiguration);
        given(sessionStorage.get("FULL_REDIRECT_URL")).willReturn("redirectUrlValue");
        String expectedPaymentId = "expectedPaymentId";
        given(sessionStorage.get("PAYMENT_ID")).willReturn(expectedPaymentId);

        // when:
        String paymentId = paymentApiClient.findPaymentId(authUrl);

        // then:
        then(sessionStorage).should().get(eq("FULL_REDIRECT_URL"));
        then(sessionStorage)
                .should()
                .put(
                        eq("payment_authorization_url"),
                        eq(
                                "http://something.com/redirect?id=123&client_id=clientIdValue&redirect_uri=redirectUrlValue"));
        then(sessionStorage).should().get("PAYMENT_ID");
        assertThat(paymentId).isEqualTo(expectedPaymentId);
    }

    @Test
    public void shouldGetPaymentByPaymentId() {
        // given:
        String paymentId = "paymentIdValue";
        given(paymentRequestResource.getPaymentInformationStatus()).willReturn("statusValue");
        given(getPaymentRequest.getPaymentRequest()).willReturn(paymentRequestResource);
        given(requestBuilder.get(eq(ConfirmablePayment.class))).willReturn(getPaymentRequest);
        given(lclRequestFactory.getPaymentRequest(eq(paymentId))).willReturn(requestBuilder);
        given(paymentRequestResource.toPaymentResponse()).willReturn(expectedPaymentResponse);

        // when:
        GetPaymentResponse actualPaymentResponse = paymentApiClient.getPayment(paymentId);

        // then:
        then(lclRequestFactory).should().getPaymentRequest(eq(paymentId));
        assertThat(actualPaymentResponse).isEqualTo(expectedPaymentResponse);
    }

    @Test
    public void shouldConfirmPaymentWhenWaitingForConfirmation() {
        // given:
        String paymentId = "paymentIdValue";
        given(confirmingGetPaymentRequest.getPaymentRequest())
                .willReturn(confirmingPaymentRequestResource);
        given(paymentRequestResource.getPaymentInformationStatus()).willReturn("ACTC");
        given(getPaymentRequest.getPaymentRequest()).willReturn(paymentRequestResource);
        given(requestBuilder.get(eq(ConfirmablePayment.class))).willReturn(getPaymentRequest);
        given(lclRequestFactory.getPaymentRequest(eq(paymentId))).willReturn(requestBuilder);
        given(confirmingPaymentRequestResource.toPaymentResponse())
                .willReturn(expectedPaymentResponse);
        given(lclRequestFactory.confirmPaymentRequest(eq(paymentId)))
                .willReturn(confirmRequestBuilder);
        given(confirmRequestBuilder.post(eq(ConfirmablePayment.class)))
                .willReturn(confirmingGetPaymentRequest);

        // when:
        GetPaymentResponse actualPaymentResponse = paymentApiClient.getPayment(paymentId);

        // then:
        then(lclRequestFactory).should().getPaymentRequest(eq(paymentId));
        assertThat(actualPaymentResponse).isEqualTo(expectedPaymentResponse);
    }
}
