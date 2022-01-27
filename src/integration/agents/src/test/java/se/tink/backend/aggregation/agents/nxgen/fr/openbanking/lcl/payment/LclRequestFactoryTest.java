package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.mockito.ArgumentMatcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.LclHeaderValueProvider;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment.rpc.ConfirmPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment.rpc.PaymentRequestResource;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.unleash.UnleashClient;
import se.tink.libraries.unleash.model.Toggle;

public class LclRequestFactoryTest {

    @Test
    public void shouldCreateGetPaymentRequest() {

        // given:
        LclHeaderValueProvider headerValueProvider = mock(LclHeaderValueProvider.class);
        TinkHttpClient tinkHttpClient = mock(TinkHttpClient.class);
        UnleashClient unleashClient = mock(UnleashClient.class);
        TokenFetcher tokenFetcher = mock(TokenFetcher.class);
        LclRequestFactory lclRequestFactory =
                new LclRequestFactory(
                        headerValueProvider, tinkHttpClient, unleashClient, tokenFetcher);

        given(headerValueProvider.getDateHeaderValue()).willReturn("date");
        given(headerValueProvider.getDigestHeaderValue(isNull())).willReturn("digest");
        given(headerValueProvider.getSignatureHeaderValue(any(), eq("date"), eq("digest")))
                .willReturn("signature");
        String paymentId = "paymentIdValue";

        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        given(requestBuilder.header(eq("X-Request-ID"), anyString())).willReturn(requestBuilder);
        given(requestBuilder.header(eq("Date"), eq("date"))).willReturn(requestBuilder);
        given(requestBuilder.header(eq("Digest"), eq("digest"))).willReturn(requestBuilder);
        given(requestBuilder.header(eq("Signature"), eq("signature"))).willReturn(requestBuilder);
        ArgumentMatcher<URL> urlArgumentMatcher =
                argument -> argument.getUrl().get().contains("/pisp/payment-requests/" + paymentId);
        given(tinkHttpClient.request(argThat(urlArgumentMatcher))).willReturn(requestBuilder);
        OAuth2Token oAuth2Token = mock(OAuth2Token.class);
        given(tokenFetcher.reuseTokenOrRefetch()).willReturn(oAuth2Token);
        given(requestBuilder.addBearerToken(oAuth2Token)).willReturn(requestBuilder);
        given(requestBuilder.accept("application/json")).willReturn(requestBuilder);
        given(requestBuilder.type("application/json")).willReturn(requestBuilder);

        // when:
        RequestBuilder actual = lclRequestFactory.getPaymentRequest(paymentId);

        // then:
        assertThat(actual).isEqualTo(requestBuilder);
        then(headerValueProvider).should().getDateHeaderValue();
        then(headerValueProvider).should().getDigestHeaderValue(isNull());
        then(headerValueProvider).should().getSignatureHeaderValue(any(), eq("date"), eq("digest"));
        then(tinkHttpClient).should().request(argThat(urlArgumentMatcher));
        then(requestBuilder).should().header(eq("X-Request-ID"), anyString());
        then(requestBuilder).should().header(eq("Date"), eq("date"));
        then(requestBuilder).should().header(eq("Digest"), eq("digest"));
        then(requestBuilder).should().header(eq("Signature"), eq("signature"));
        then(requestBuilder).should().addBearerToken(oAuth2Token);
        then(requestBuilder).should().accept(eq("application/json"));
        then(requestBuilder).should().type(eq("application/json"));
    }

    @Test
    public void shouldCreateConfirmPaymentRequest() {

        // given:
        LclHeaderValueProvider headerValueProvider = mock(LclHeaderValueProvider.class);
        TinkHttpClient tinkHttpClient = mock(TinkHttpClient.class);
        UnleashClient unleashClient = mock(UnleashClient.class);
        TokenFetcher tokenFetcher = mock(TokenFetcher.class);
        LclRequestFactory lclRequestFactory =
                new LclRequestFactory(
                        headerValueProvider, tinkHttpClient, unleashClient, tokenFetcher);

        ConfirmPaymentRequest confirmPaymentRequest = mock(ConfirmPaymentRequest.class);

        given(headerValueProvider.getDateHeaderValue()).willReturn("date");
        given(headerValueProvider.getDigestHeaderValue(eq(confirmPaymentRequest)))
                .willReturn("digest");
        given(headerValueProvider.getSignatureHeaderValue(any(), eq("date"), eq("digest")))
                .willReturn("signature");
        String paymentId = "paymentIdValue";

        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        given(requestBuilder.header(eq("X-Request-ID"), anyString())).willReturn(requestBuilder);
        given(requestBuilder.header(eq("Date"), eq("date"))).willReturn(requestBuilder);
        given(requestBuilder.header(eq("Digest"), eq("digest"))).willReturn(requestBuilder);
        given(requestBuilder.header(eq("Signature"), eq("signature"))).willReturn(requestBuilder);
        ArgumentMatcher<URL> urlArgumentMatcher =
                argument ->
                        argument.getUrl()
                                .get()
                                .contains("/pisp/payment-requests/" + paymentId + "/confirmation");
        given(tinkHttpClient.request(argThat(urlArgumentMatcher))).willReturn(requestBuilder);
        OAuth2Token oAuth2Token = mock(OAuth2Token.class);
        given(tokenFetcher.reuseTokenOrRefetch()).willReturn(oAuth2Token);
        given(requestBuilder.addBearerToken(oAuth2Token)).willReturn(requestBuilder);
        given(requestBuilder.accept("application/json")).willReturn(requestBuilder);
        given(requestBuilder.type("application/json")).willReturn(requestBuilder);

        // when:
        RequestBuilder actual =
                lclRequestFactory.confirmPaymentRequest(paymentId, confirmPaymentRequest);

        // then:
        assertThat(actual).isEqualTo(requestBuilder);
        then(headerValueProvider).should().getDateHeaderValue();
        then(headerValueProvider).should().getDigestHeaderValue(eq(confirmPaymentRequest));
        then(headerValueProvider).should().getSignatureHeaderValue(any(), eq("date"), eq("digest"));
        then(tinkHttpClient).should().request(argThat(urlArgumentMatcher));
        then(requestBuilder).should().header(eq("X-Request-ID"), anyString());
        then(requestBuilder).should().header(eq("Date"), eq("date"));
        then(requestBuilder).should().header(eq("Digest"), eq("digest"));
        then(requestBuilder).should().header(eq("Signature"), eq("signature"));
        then(requestBuilder).should().addBearerToken(oAuth2Token);
        then(requestBuilder).should().accept(eq("application/json"));
        then(requestBuilder).should().type(eq("application/json"));
    }

    @Test
    public void shouldCreatePaymentRequest() {

        // given:
        LclHeaderValueProvider headerValueProvider = mock(LclHeaderValueProvider.class);
        TinkHttpClient tinkHttpClient = mock(TinkHttpClient.class);
        UnleashClient unleashClient = mock(UnleashClient.class);
        TokenFetcher tokenFetcher = mock(TokenFetcher.class);
        LclRequestFactory lclRequestFactory =
                new LclRequestFactory(
                        headerValueProvider, tinkHttpClient, unleashClient, tokenFetcher);

        PaymentRequestResource paymentRequestResource = mock(PaymentRequestResource.class);

        given(headerValueProvider.getDateHeaderValue()).willReturn("date");
        given(headerValueProvider.getDigestHeaderValue(eq(paymentRequestResource)))
                .willReturn("digest");
        given(headerValueProvider.getSignatureHeaderValue(any(), eq("date"), eq("digest")))
                .willReturn("signature");

        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        given(requestBuilder.header(eq("X-Request-ID"), anyString())).willReturn(requestBuilder);
        given(requestBuilder.header(eq("Date"), eq("date"))).willReturn(requestBuilder);
        given(requestBuilder.header(eq("Digest"), eq("digest"))).willReturn(requestBuilder);
        given(requestBuilder.header(eq("Signature"), eq("signature"))).willReturn(requestBuilder);
        ArgumentMatcher<URL> urlArgumentMatcher =
                argument -> argument.getUrl().get().contains("/pisp/payment-requests");
        given(tinkHttpClient.request(argThat(urlArgumentMatcher))).willReturn(requestBuilder);
        OAuth2Token oAuth2Token = mock(OAuth2Token.class);
        given(tokenFetcher.reuseTokenOrRefetch()).willReturn(oAuth2Token);
        given(requestBuilder.addBearerToken(oAuth2Token)).willReturn(requestBuilder);
        given(requestBuilder.accept("application/json")).willReturn(requestBuilder);
        given(requestBuilder.type("application/json")).willReturn(requestBuilder);

        // when:
        RequestBuilder actual = lclRequestFactory.createPaymentRequest(paymentRequestResource);

        // then:
        assertThat(actual).isEqualTo(requestBuilder);
        then(headerValueProvider).should().getDateHeaderValue();
        then(headerValueProvider).should().getDigestHeaderValue(eq(paymentRequestResource));
        then(headerValueProvider).should().getSignatureHeaderValue(any(), eq("date"), eq("digest"));
        then(tinkHttpClient).should().request(argThat(urlArgumentMatcher));
        then(requestBuilder).should().header(eq("X-Request-ID"), anyString());
        then(requestBuilder).should().header(eq("Date"), eq("date"));
        then(requestBuilder).should().header(eq("Digest"), eq("digest"));
        then(requestBuilder).should().header(eq("Signature"), eq("signature"));
        then(requestBuilder).should().addBearerToken(oAuth2Token);
        then(requestBuilder).should().accept(eq("application/json"));
        then(requestBuilder).should().type(eq("application/json"));
    }

    @Test
    public void shouldCreatePaymentRequestForApp2App() {

        // given:
        LclHeaderValueProvider headerValueProvider = mock(LclHeaderValueProvider.class);
        TinkHttpClient tinkHttpClient = mock(TinkHttpClient.class);
        UnleashClient unleashClient = mock(UnleashClient.class);
        TokenFetcher tokenFetcher = mock(TokenFetcher.class);
        LclRequestFactory lclRequestFactory =
                new LclRequestFactory(
                        headerValueProvider, tinkHttpClient, unleashClient, tokenFetcher);

        PaymentRequestResource paymentRequestResource = mock(PaymentRequestResource.class);

        given(unleashClient.isToggleEnabled(any(Toggle.class)))
                .willReturn(
                        true); // Toggle class do not have equals or getters to check toggle name

        given(headerValueProvider.getDateHeaderValue()).willReturn("date");
        given(headerValueProvider.getDigestHeaderValue(eq(paymentRequestResource)))
                .willReturn("digest");
        given(headerValueProvider.getSignatureHeaderValue(any(), eq("date"), eq("digest")))
                .willReturn("signature");

        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        given(requestBuilder.header(eq("X-Request-ID"), anyString())).willReturn(requestBuilder);
        given(requestBuilder.header(eq("Date"), eq("date"))).willReturn(requestBuilder);
        given(requestBuilder.header(eq("Digest"), eq("digest"))).willReturn(requestBuilder);
        given(requestBuilder.header(eq("Signature"), eq("signature"))).willReturn(requestBuilder);
        given(requestBuilder.header(eq("PSU-App2app-Client-Type"), eq("retail")))
                .willReturn(requestBuilder);
        ArgumentMatcher<URL> urlArgumentMatcher =
                argument -> argument.getUrl().get().contains("/pisp/payment-requests");
        given(tinkHttpClient.request(argThat(urlArgumentMatcher))).willReturn(requestBuilder);
        OAuth2Token oAuth2Token = mock(OAuth2Token.class);
        given(tokenFetcher.reuseTokenOrRefetch()).willReturn(oAuth2Token);
        given(requestBuilder.addBearerToken(oAuth2Token)).willReturn(requestBuilder);
        given(requestBuilder.accept("application/json")).willReturn(requestBuilder);
        given(requestBuilder.type("application/json")).willReturn(requestBuilder);

        // when:
        RequestBuilder actual = lclRequestFactory.createPaymentRequest(paymentRequestResource);

        // then:
        assertThat(actual).isEqualTo(requestBuilder);
        then(headerValueProvider).should().getDateHeaderValue();
        then(headerValueProvider).should().getDigestHeaderValue(eq(paymentRequestResource));
        then(headerValueProvider).should().getSignatureHeaderValue(any(), eq("date"), eq("digest"));
        then(tinkHttpClient).should().request(argThat(urlArgumentMatcher));
        then(requestBuilder).should().header(eq("X-Request-ID"), anyString());
        then(requestBuilder).should().header(eq("Date"), eq("date"));
        then(requestBuilder).should().header(eq("Digest"), eq("digest"));
        then(requestBuilder).should().header(eq("Signature"), eq("signature"));
        then(requestBuilder).should().addBearerToken(oAuth2Token);
        then(requestBuilder).should().accept(eq("application/json"));
        then(requestBuilder).should().type(eq("application/json"));
        then(requestBuilder).should().header(eq("PSU-App2app-Client-Type"), eq("retail"));
    }
}
