package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.LclHeaderValueProvider;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment.rpc.ConfirmPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment.rpc.PaymentRequestResource;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.unleash.UnleashClient;
import se.tink.libraries.unleash.model.Toggle;

@RunWith(MockitoJUnitRunner.class)
public class LclRequestFactoryTest {

    @Mock private ConfirmPaymentRequest confirmPaymentRequest;
    @Mock private PaymentRequestResource paymentRequestResource;
    @Mock private OAuth2Token oAuth2Token;
    @Mock private LclHeaderValueProvider headerValueProvider;
    @Mock private TinkHttpClient tinkHttpClient;
    @Mock private UnleashClient unleashClient;
    @Mock private TokenFetcher tokenFetcher;
    @Mock private RequestBuilder requestBuilder;

    @Test
    public void shouldCreateGetPaymentRequest() {

        // given:
        LclRequestFactory lclRequestFactory =
                new LclRequestFactory(
                        headerValueProvider, tinkHttpClient, unleashClient, tokenFetcher);

        mockHeaderProviderWithNullBody();
        String paymentId = "paymentIdValue";
        ArgumentMatcher<URL> urlArgumentMatcher =
                argument -> argument.getUrl().get().contains("/pisp/payment-requests/" + paymentId);
        mockRequestBuilder(urlArgumentMatcher);

        // when:
        RequestBuilder actual = lclRequestFactory.getPaymentRequest(paymentId);

        // then:
        assertThat(actual).isEqualTo(requestBuilder);
        verifyHeaderProviderWithoutDigest();
        verifyRequestBuilder(urlArgumentMatcher);
    }

    @Test
    public void shouldCreateConfirmPaymentRequest() {

        // given:
        LclRequestFactory lclRequestFactory =
                new LclRequestFactory(
                        headerValueProvider, tinkHttpClient, unleashClient, tokenFetcher);

        mockHeaderProviderWithBody(confirmPaymentRequest);

        String paymentId = "paymentIdValue";
        ArgumentMatcher<URL> urlArgumentMatcher =
                argument ->
                        argument.getUrl()
                                .get()
                                .contains("/pisp/payment-requests/" + paymentId + "/confirmation");

        mockRequestBuilder(urlArgumentMatcher);

        // when:
        RequestBuilder actual =
                lclRequestFactory.confirmPaymentRequest(paymentId, confirmPaymentRequest);

        // then:
        assertThat(actual).isEqualTo(requestBuilder);
        verifyHeaderProviderWithDigest(confirmPaymentRequest);
        verifyRequestBuilder(urlArgumentMatcher);
    }

    @Test
    public void shouldCreatePaymentRequest() {

        // given:
        LclRequestFactory lclRequestFactory =
                new LclRequestFactory(
                        headerValueProvider, tinkHttpClient, unleashClient, tokenFetcher);

        mockHeaderProviderWithBody(paymentRequestResource);

        ArgumentMatcher<URL> urlArgumentMatcher =
                argument -> argument.getUrl().get().contains("/pisp/payment-requests");

        mockRequestBuilder(urlArgumentMatcher);

        // when:
        RequestBuilder actual = lclRequestFactory.createPaymentRequest(paymentRequestResource);

        // then:
        assertThat(actual).isEqualTo(requestBuilder);
        verifyHeaderProviderWithDigest(paymentRequestResource);
        verifyRequestBuilder(urlArgumentMatcher);
    }

    @Test
    public void shouldCreatePaymentRequestForApp2App() {

        // given:
        LclRequestFactory lclRequestFactory =
                new LclRequestFactory(
                        headerValueProvider, tinkHttpClient, unleashClient, tokenFetcher);

        given(unleashClient.isToggleEnabled(any(Toggle.class)))
                .willReturn(
                        true); // Toggle class do not have equals or getters to check toggle name

        mockHeaderProviderWithBody(paymentRequestResource);
        ArgumentMatcher<URL> urlArgumentMatcher =
                argument -> argument.getUrl().get().contains("/pisp/payment-requests");

        mockRequestBuilder(urlArgumentMatcher);

        given(requestBuilder.header(eq("PSU-App2app-Client-Type"), eq("retail")))
                .willReturn(requestBuilder);

        // when:
        RequestBuilder actual = lclRequestFactory.createPaymentRequest(paymentRequestResource);

        // then:
        assertThat(actual).isEqualTo(requestBuilder);
        verifyHeaderProviderWithDigest(paymentRequestResource);
        verifyRequestBuilder(urlArgumentMatcher);
        then(requestBuilder).should().header(eq("PSU-App2app-Client-Type"), eq("retail"));
    }

    private void verifyHeaderProviderWithDigest(Object equalObj) {
        then(headerValueProvider).should().getDateHeaderValue();
        then(headerValueProvider).should().getDigestHeaderValue(eq(equalObj));
        then(headerValueProvider).should().getSignatureHeaderValue(any(), eq("date"), eq("digest"));
    }

    private void verifyHeaderProviderWithoutDigest() {
        then(headerValueProvider).should().getDateHeaderValue();
        then(headerValueProvider).should().getDigestHeaderValue(isNull());
        then(headerValueProvider).should().getSignatureHeaderValue(any(), eq("date"), eq("digest"));
    }

    private void mockHeaderProviderWithBody(Object equalObj) {
        given(headerValueProvider.getDateHeaderValue()).willReturn("date");
        given(headerValueProvider.getDigestHeaderValue(eq(equalObj))).willReturn("digest");
        given(headerValueProvider.getSignatureHeaderValue(any(), eq("date"), eq("digest")))
                .willReturn("signature");
    }

    private void mockHeaderProviderWithNullBody() {
        given(headerValueProvider.getDateHeaderValue()).willReturn("date");
        given(headerValueProvider.getDigestHeaderValue(isNull())).willReturn("digest");
        given(headerValueProvider.getSignatureHeaderValue(any(), eq("date"), eq("digest")))
                .willReturn("signature");
    }

    private void verifyRequestBuilder(ArgumentMatcher<URL> urlArgumentMatcher) {
        then(tinkHttpClient).should().request(argThat(urlArgumentMatcher));
        then(requestBuilder).should().header(eq("X-Request-ID"), anyString());
        then(requestBuilder).should().header(eq("Date"), eq("date"));
        then(requestBuilder).should().header(eq("Digest"), eq("digest"));
        then(requestBuilder).should().header(eq("Signature"), eq("signature"));
        then(requestBuilder).should().addBearerToken(oAuth2Token);
        then(requestBuilder).should().accept(eq("application/json"));
        then(requestBuilder).should().type(eq("application/json"));
    }

    private void mockRequestBuilder(ArgumentMatcher<URL> urlArgumentMatcher) {
        given(requestBuilder.header(eq("X-Request-ID"), anyString())).willReturn(requestBuilder);
        given(requestBuilder.header(eq("Date"), eq("date"))).willReturn(requestBuilder);
        given(requestBuilder.header(eq("Digest"), eq("digest"))).willReturn(requestBuilder);
        given(requestBuilder.header(eq("Signature"), eq("signature"))).willReturn(requestBuilder);
        given(tinkHttpClient.request(argThat(urlArgumentMatcher))).willReturn(requestBuilder);
        given(tokenFetcher.reuseTokenOrRefetch()).willReturn(oAuth2Token);
        given(requestBuilder.addBearerToken(oAuth2Token)).willReturn(requestBuilder);
        given(requestBuilder.accept("application/json")).willReturn(requestBuilder);
        given(requestBuilder.type("application/json")).willReturn(requestBuilder);
    }
}
