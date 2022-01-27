package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.google.common.collect.ImmutableList;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.configuration.LclConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment.rpc.ConfirmPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment.rpc.GetPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment.rpc.PaymentRequestResource;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.SupplementaryDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class LclPaymentApiClientTest {

    @Test
    public void shouldCreatePaymentRequestAndExecuteIt() {
        // given:
        SessionStorage sessionStorage = Mockito.mock(SessionStorage.class);
        AgentConfiguration<LclConfiguration> agentConfiguration =
                Mockito.mock(AgentConfiguration.class);
        LclRequestFactory lclRequestFactory = Mockito.mock(LclRequestFactory.class);
        TokenFetcher tokenFetcher = Mockito.mock(TokenFetcher.class);
        PaymentRequestResourceFactory resourceFactory =
                Mockito.mock(PaymentRequestResourceFactory.class);
        LclPaymentApiClient paymentApiClient =
                new LclPaymentApiClient(
                        sessionStorage,
                        agentConfiguration,
                        lclRequestFactory,
                        tokenFetcher,
                        resourceFactory);
        CreatePaymentRequest createPaymentRequest = Mockito.mock(CreatePaymentRequest.class);
        PaymentRequestResource requestResource = Mockito.mock(PaymentRequestResource.class);
        SupplementaryDataEntity dataEntity = Mockito.mock(SupplementaryDataEntity.class);
        given(dataEntity.getSuccessfulReportUrl()).willReturn("testUrl");
        given(createPaymentRequest.getSupplementaryData()).willReturn(dataEntity);
        given(resourceFactory.createPaymentRequestResource(createPaymentRequest))
                .willReturn(requestResource);
        RequestBuilder requestBuilder = Mockito.mock(RequestBuilder.class);
        given(lclRequestFactory.createPaymentRequest(requestResource)).willReturn(requestBuilder);
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        given(resourceFactory.serializeBody(requestResource)).willReturn("body");

        given(requestBuilder.post(eq(HttpResponse.class), eq("body"))).willReturn(httpResponse);
        MultivaluedMap<String, String> headers = Mockito.mock(MultivaluedMap.class);
        given(headers.get("location")).willReturn(ImmutableList.of("ignore/ignore/locationValue"));
        given(httpResponse.getHeaders()).willReturn(headers);
        CreatePaymentResponse expectedResponse = Mockito.mock(CreatePaymentResponse.class);
        given(httpResponse.getBody(CreatePaymentResponse.class)).willReturn(expectedResponse);

        // when:
        CreatePaymentResponse actualResponse = paymentApiClient.createPayment(createPaymentRequest);

        // then:
        then(sessionStorage).should().put(eq("FULL_REDIRECT_URL"), eq("testUrl&code=code"));
        then(lclRequestFactory).should().createPaymentRequest(requestResource);
        then(requestBuilder).should().post(eq(HttpResponse.class), eq("body"));
        then(sessionStorage).should().put(eq("PAYMENT_ID"), eq("locationValue"));
        then(httpResponse).should().getBody(eq(CreatePaymentResponse.class));
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    public void shouldThrowExceptionWhenThereIsNoLocationInResponse() {
        // given:
        SessionStorage sessionStorage = Mockito.mock(SessionStorage.class);
        AgentConfiguration<LclConfiguration> agentConfiguration =
                Mockito.mock(AgentConfiguration.class);
        LclRequestFactory lclRequestFactory = Mockito.mock(LclRequestFactory.class);
        TokenFetcher tokenFetcher = Mockito.mock(TokenFetcher.class);
        PaymentRequestResourceFactory resourceFactory =
                Mockito.mock(PaymentRequestResourceFactory.class);
        LclPaymentApiClient paymentApiClient =
                new LclPaymentApiClient(
                        sessionStorage,
                        agentConfiguration,
                        lclRequestFactory,
                        tokenFetcher,
                        resourceFactory);
        CreatePaymentRequest createPaymentRequest = Mockito.mock(CreatePaymentRequest.class);
        PaymentRequestResource requestResource = Mockito.mock(PaymentRequestResource.class);
        SupplementaryDataEntity dataEntity = Mockito.mock(SupplementaryDataEntity.class);
        given(dataEntity.getSuccessfulReportUrl()).willReturn("testUrl");
        given(createPaymentRequest.getSupplementaryData()).willReturn(dataEntity);
        given(resourceFactory.createPaymentRequestResource(createPaymentRequest))
                .willReturn(requestResource);
        RequestBuilder requestBuilder = Mockito.mock(RequestBuilder.class);
        given(lclRequestFactory.createPaymentRequest(requestResource)).willReturn(requestBuilder);
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        given(resourceFactory.serializeBody(requestResource)).willReturn("body");
        given(requestBuilder.post(eq(HttpResponse.class), eq("body"))).willReturn(httpResponse);
        MultivaluedMap<String, String> headers = Mockito.mock(MultivaluedMap.class);
        given(headers.get("location")).willReturn(ImmutableList.of());
        given(httpResponse.getHeaders()).willReturn(headers);

        // when:
        Throwable throwable =
                catchThrowable(() -> paymentApiClient.createPayment(createPaymentRequest));

        // then:
        then(sessionStorage).should().put(eq("FULL_REDIRECT_URL"), eq("testUrl&code=code"));
        then(lclRequestFactory).should().createPaymentRequest(requestResource);
        then(requestBuilder).should().post(eq(HttpResponse.class), eq("body"));

        assertThat(throwable).isNotNull();
        assertThat(throwable).isInstanceOf(MissingLocationException.class);
    }

    @Test
    public void shouldFetchToken() {
        // given:
        SessionStorage sessionStorage = Mockito.mock(SessionStorage.class);
        AgentConfiguration<LclConfiguration> agentConfiguration =
                Mockito.mock(AgentConfiguration.class);
        LclRequestFactory lclRequestFactory = Mockito.mock(LclRequestFactory.class);
        TokenFetcher tokenFetcher = Mockito.mock(TokenFetcher.class);
        PaymentRequestResourceFactory resourceFactory =
                Mockito.mock(PaymentRequestResourceFactory.class);
        LclPaymentApiClient paymentApiClient =
                new LclPaymentApiClient(
                        sessionStorage,
                        agentConfiguration,
                        lclRequestFactory,
                        tokenFetcher,
                        resourceFactory);

        // when:
        paymentApiClient.fetchToken();

        // then:
        then(tokenFetcher).should().fetchToken();
    }

    @Test
    public void shouldFindPayment() {
        // given:
        SessionStorage sessionStorage = Mockito.mock(SessionStorage.class);
        AgentConfiguration<LclConfiguration> agentConfiguration =
                Mockito.mock(AgentConfiguration.class);
        LclRequestFactory lclRequestFactory = Mockito.mock(LclRequestFactory.class);
        TokenFetcher tokenFetcher = Mockito.mock(TokenFetcher.class);
        PaymentRequestResourceFactory resourceFactory =
                Mockito.mock(PaymentRequestResourceFactory.class);
        LclPaymentApiClient paymentApiClient =
                new LclPaymentApiClient(
                        sessionStorage,
                        agentConfiguration,
                        lclRequestFactory,
                        tokenFetcher,
                        resourceFactory);

        String authUrl = "http://something.com/redirect?id=123";
        LclConfiguration lclConfiguration = Mockito.mock(LclConfiguration.class);
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
        SessionStorage sessionStorage = Mockito.mock(SessionStorage.class);
        AgentConfiguration<LclConfiguration> agentConfiguration =
                Mockito.mock(AgentConfiguration.class);
        LclRequestFactory lclRequestFactory = Mockito.mock(LclRequestFactory.class);
        TokenFetcher tokenFetcher = Mockito.mock(TokenFetcher.class);
        PaymentRequestResourceFactory resourceFactory =
                Mockito.mock(PaymentRequestResourceFactory.class);
        LclPaymentApiClient paymentApiClient =
                new LclPaymentApiClient(
                        sessionStorage,
                        agentConfiguration,
                        lclRequestFactory,
                        tokenFetcher,
                        resourceFactory);

        String paymentId = "paymentIdValue";
        RequestBuilder requestBuilder = Mockito.mock(RequestBuilder.class);
        GetPaymentRequest getPaymentRequest = Mockito.mock(GetPaymentRequest.class);
        PaymentRequestResource paymentRequestResource = Mockito.mock(PaymentRequestResource.class);
        given(paymentRequestResource.getPaymentInformationStatus()).willReturn("statusValue");
        given(getPaymentRequest.getPaymentRequest()).willReturn(paymentRequestResource);
        given(requestBuilder.get(eq(GetPaymentRequest.class))).willReturn(getPaymentRequest);
        given(lclRequestFactory.getPaymentRequest(eq(paymentId))).willReturn(requestBuilder);
        GetPaymentResponse expectedPaymentResponse = Mockito.mock(GetPaymentResponse.class);
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
        SessionStorage sessionStorage = Mockito.mock(SessionStorage.class);
        AgentConfiguration<LclConfiguration> agentConfiguration =
                Mockito.mock(AgentConfiguration.class);
        LclRequestFactory lclRequestFactory = Mockito.mock(LclRequestFactory.class);
        TokenFetcher tokenFetcher = Mockito.mock(TokenFetcher.class);
        PaymentRequestResourceFactory resourceFactory =
                Mockito.mock(PaymentRequestResourceFactory.class);
        LclPaymentApiClient paymentApiClient =
                new LclPaymentApiClient(
                        sessionStorage,
                        agentConfiguration,
                        lclRequestFactory,
                        tokenFetcher,
                        resourceFactory);

        String paymentId = "paymentIdValue";
        RequestBuilder requestBuilder = Mockito.mock(RequestBuilder.class);
        GetPaymentRequest getPaymentRequest = Mockito.mock(GetPaymentRequest.class);
        RequestBuilder confirmRequestBuilder = Mockito.mock(RequestBuilder.class);

        PaymentRequestResource confirmingPaymentRequestResource =
                Mockito.mock(PaymentRequestResource.class);
        GetPaymentRequest confirmingGetPaymentRequest = Mockito.mock(GetPaymentRequest.class);
        given(confirmingGetPaymentRequest.getPaymentRequest())
                .willReturn(confirmingPaymentRequestResource);

        PaymentRequestResource paymentRequestResource = Mockito.mock(PaymentRequestResource.class);
        given(paymentRequestResource.getPaymentInformationStatus()).willReturn("ACTC");
        given(getPaymentRequest.getPaymentRequest()).willReturn(paymentRequestResource);
        given(requestBuilder.get(eq(GetPaymentRequest.class))).willReturn(getPaymentRequest);
        given(lclRequestFactory.getPaymentRequest(eq(paymentId))).willReturn(requestBuilder);
        GetPaymentResponse expectedPaymentResponse = Mockito.mock(GetPaymentResponse.class);
        given(confirmingPaymentRequestResource.toPaymentResponse())
                .willReturn(expectedPaymentResponse);
        ConfirmPaymentRequest confirmPaymentRequest = Mockito.mock(ConfirmPaymentRequest.class);
        given(lclRequestFactory.createConfirmPaymentRequest()).willReturn(confirmPaymentRequest);
        given(lclRequestFactory.confirmPaymentRequest(eq(paymentId), eq(confirmPaymentRequest)))
                .willReturn(confirmRequestBuilder);
        given(confirmRequestBuilder.post(eq(GetPaymentRequest.class), eq(confirmPaymentRequest)))
                .willReturn(confirmingGetPaymentRequest);

        // when:
        GetPaymentResponse actualPaymentResponse = paymentApiClient.getPayment(paymentId);

        // then:
        then(lclRequestFactory).should().getPaymentRequest(eq(paymentId));
        then(lclRequestFactory).should().createConfirmPaymentRequest();
        assertThat(actualPaymentResponse).isEqualTo(expectedPaymentResponse);
    }
}
