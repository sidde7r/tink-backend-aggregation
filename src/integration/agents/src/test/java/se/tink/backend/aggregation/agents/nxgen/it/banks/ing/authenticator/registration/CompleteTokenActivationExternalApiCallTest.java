package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.it.banks.ing.TestAsserts.assertHttpRequestsEquals;
import static se.tink.backend.aggregation.agents.nxgen.it.banks.ing.TestFixtures.givenActivationId;
import static se.tink.backend.aggregation.agents.nxgen.it.banks.ing.TestFixtures.givenBaseUrl;
import static se.tink.backend.aggregation.agents.nxgen.it.banks.ing.TestFixtures.givenChallenge;
import static se.tink.backend.aggregation.agents.nxgen.it.banks.ing.TestFixtures.givenDeviceId;
import static se.tink.backend.aggregation.agents.nxgen.it.banks.ing.TestFixtures.givenFingerprint;
import static se.tink.backend.aggregation.agents.nxgen.it.banks.ing.TestFixtures.givenOtp;
import static se.tink.backend.aggregation.agents.nxgen.it.banks.ing.TestFixtures.givenTime;
import static se.tink.backend.aggregation.agents.nxgen.it.banks.ing.TestFixtures.notJailBroken;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.ConfigurationProvider;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.Cryptor;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.EncryptedExternalApiCall.AesKey;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.CompleteTokenActivationExternalApiCall.Arg;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.CompleteTokenActivationExternalApiCall.ResponseBody;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.CompleteTokenActivationExternalApiCall.Result;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.scaffold.ExternalApiCallResult;

public class CompleteTokenActivationExternalApiCallTest {

    private Clock fixedClock =
            Clock.fixed(Instant.parse("2018-04-29T10:15:30.00Z"), ZoneId.of("Europe/Warsaw"));
    private RSAPublicKey rsaPublicKey = mock(RSAPublicKey.class);
    private RSAPrivateKey rsaPrivateKey = mock(RSAPrivateKey.class);
    private TinkHttpClient httpClient = mock(TinkHttpClient.class);
    private Cryptor cryptor = mock(Cryptor.class);
    private CommonDataProvider commonDataProvider = mock(CommonDataProvider.class);
    private ConfigurationProvider configurationProvider = mock(ConfigurationProvider.class);

    private CompleteTokenActivationExternalApiCall sut =
            new CompleteTokenActivationExternalApiCall(
                    httpClient,
                    fixedClock,
                    cryptor,
                    commonDataProvider,
                    configurationProvider,
                    rsaPublicKey,
                    rsaPrivateKey);

    @Before
    public void setupMock() {
        Mockito.reset(httpClient);
        Mockito.reset(cryptor);
        Mockito.reset(rsaPublicKey);
        Mockito.reset(rsaPrivateKey);
        Mockito.reset(commonDataProvider);
        Mockito.reset(configurationProvider);
    }

    @Test
    public void prepareRequestShouldReturnProperHttpRequestWhenProperArgPassed() {
        // given
        AesKey givenAesKey = new AesKey(new byte[] {1, 2, 3}, new byte[] {1, 2, 4});
        String givenEncryptedAK = "aK Encrypted Value";
        String givenEncryptedAI = "aI Encrypted Value";
        String givenEncryptedPayload = "payload Encrypted Value";
        String givenSignedPayload = "payload Signed Value";
        HttpRequest givenHttpRequest =
                givenHttpRequest(
                        givenEncryptedPayload,
                        givenEncryptedAK,
                        givenEncryptedAI,
                        givenSignedPayload);
        Arg arg =
                Arg.builder()
                        .challenge(givenChallenge())
                        .deviceId(givenDeviceId())
                        .otp(givenOtp())
                        .activationId(givenActivationId())
                        .build();

        when(cryptor.rsaEncryptBase64UrlEncode(eq(rsaPublicKey), Matchers.<byte[]>any()))
                .thenReturn(givenEncryptedAK, givenEncryptedAI);
        when(cryptor.rsaSha256SignBase64UrlEncode(eq(rsaPrivateKey), eq(givenEncryptedPayload)))
                .thenReturn(givenSignedPayload);
        when(cryptor.aesEncryptBase64UrlEncode(
                        eq(givenAesKey.getKey()),
                        eq(givenAesKey.getInitializationVector()),
                        eq(givenRequestFormPayloadFieldAsString(fixedClock))))
                .thenReturn(givenEncryptedPayload);
        when(commonDataProvider.prepareFingerprint(givenDeviceId())).thenReturn(givenFingerprint());
        when(configurationProvider.getBaseUrl()).thenReturn(givenBaseUrl());

        // when
        HttpRequest actualHttpRequest = sut.prepareRequest(arg, givenAesKey);

        // then
        assertHttpRequestsEquals(actualHttpRequest, givenHttpRequest);
    }

    @Test
    public void parseResponseShouldReturnProperResultWhenHttpResponsePassed() {
        // given
        String givenResponseResValue = "response res value";
        String givenResponseSign = "response sign value";
        ResponseBody givenResponseBody =
                new ResponseBody(0, givenResponseResValue, givenResponseSign);
        ExternalApiCallResult<Result> givenExternalApiCallResult =
                ExternalApiCallResult.of(givenResult(), 200);
        AesKey givenAesKey = new AesKey(new byte[] {1, 2, 3}, new byte[] {1, 2, 4});

        when(cryptor.base64DecodeAesDecrypt(
                        givenAesKey.getKey(),
                        givenAesKey.getInitializationVector(),
                        givenResponseBody.getResponse()))
                .thenReturn(givenResultAsString());
        HttpResponse givenHttpResponse = mock(HttpResponse.class);
        when(givenHttpResponse.getStatus()).thenReturn(givenExternalApiCallResult.getStatusCode());
        when(givenHttpResponse.getBody(ResponseBody.class)).thenReturn(givenResponseBody);

        // when
        ExternalApiCallResult<Result> actualResult =
                sut.parseResponse(givenHttpResponse, givenAesKey);

        // then
        verify(cryptor)
                .base64DecodeAesDecrypt(
                        givenAesKey.getKey(),
                        givenAesKey.getInitializationVector(),
                        givenResponseBody.getResponse());
        assertThat(actualResult).isEqualTo(givenExternalApiCallResult);
    }

    private static HttpRequest givenHttpRequest(String payload, String aK, String aI, String sign) {
        return new HttpRequestImpl(
                HttpMethod.POST,
                new URL(givenBaseUrl() + "/MobileFlow/completeTokenActivation.htm"),
                String.format(
                        "request={\"payload\":\"%s\",\"aK\":\"%s\",\"aI\":\"%s\",\"sign\":\"%s\"}",
                        payload, aK, aI, sign));
    }

    private static String givenRequestFormPayloadFieldAsString(Clock clock) {
        return String.format(
                "{\"challenge\":\"%s\",\"otp\":\"%s\",\"fingerprint\":\"%s\",\"jailbroken\":%s,\"timestamp\":%s,\"auth_method\":\"%s\",\"activationID\":\"%s\"}",
                givenChallenge(),
                givenOtp(),
                givenFingerprint(),
                notJailBroken(),
                givenTime(clock),
                "smartcode",
                givenActivationId());
    }

    private static Result givenResult() {
        return new Result("ok", 0, "");
    }

    private static String givenResultAsString() {
        Result result = givenResult();
        return String.format(
                "{\"result\":\"%s\",\"code\":%s,\"message\":\"%s\"}",
                result.getResult(), result.getCode(), result.getMessage());
    }
}
