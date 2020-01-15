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
import static se.tink.backend.aggregation.agents.nxgen.it.banks.ing.TestFixtures.givenPin;
import static se.tink.backend.aggregation.agents.nxgen.it.banks.ing.TestFixtures.givenTime;
import static se.tink.backend.aggregation.agents.nxgen.it.banks.ing.TestFixtures.givenUserId;
import static se.tink.backend.aggregation.agents.nxgen.it.banks.ing.TestFixtures.notJailBroken;

import java.nio.charset.StandardCharsets;
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
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.ActivateTokenExternalApiCall.Arg;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.ActivateTokenExternalApiCall.ResponseBody;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.ActivateTokenExternalApiCall.Result;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.scaffold.ExternalApiCallResult;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class ActivateTokenExternalApiCallTest {

    private Clock fixedClock =
            Clock.fixed(Instant.parse("2018-04-29T10:15:30.00Z"), ZoneId.of("Europe/Warsaw"));
    private RSAPublicKey rsaPublicKey = mock(RSAPublicKey.class);
    private RSAPrivateKey rsaPrivateKey = mock(RSAPrivateKey.class);
    private TinkHttpClient httpClient = mock(TinkHttpClient.class);
    private Cryptor cryptor = mock(Cryptor.class);
    private CommonDataProvider commonDataProvider = mock(CommonDataProvider.class);
    private ConfigurationProvider configurationProvider = mock(ConfigurationProvider.class);

    private ActivateTokenExternalApiCall sut =
            new ActivateTokenExternalApiCall(
                    httpClient,
                    fixedClock,
                    cryptor,
                    commonDataProvider,
                    configurationProvider,
                    rsaPublicKey);

    @Before
    public void resetMock() {
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
        String givenPasswordFieldValue = "40be852219859b5e72d1cfe014c9637e";
        Arg arg =
                Arg.builder()
                        .pin(givenPin())
                        .userId(givenUserId())
                        .deviceId(givenDeviceId())
                        .build();
        HttpRequest givenHttpRequest =
                givenHttpRequest(givenEncryptedPayload, givenEncryptedAK, givenEncryptedAI);

        when(cryptor.rsaEncryptBase64UrlEncode(eq(rsaPublicKey), Matchers.<byte[]>any()))
                .thenReturn(givenEncryptedAK, givenEncryptedAI);
        when(cryptor.aesEncryptBase64UrlEncode(
                        eq(givenAesKey.getKey()),
                        eq(givenAesKey.getInitializationVector()),
                        eq(
                                givenRequestFormPayloadFieldAsString(
                                        fixedClock, givenPasswordFieldValue))))
                .thenReturn(givenEncryptedPayload);
        when(cryptor.generateRandomAesIv())
                .thenReturn(givenPasswordFieldValue.getBytes(StandardCharsets.UTF_8));
        when(commonDataProvider.prepareFingerprint(givenDeviceId())).thenReturn(givenFingerprint());
        when(configurationProvider.getBaseUrl()).thenReturn(givenBaseUrl());

        // when
        HttpRequest httpRequest = sut.prepareRequest(arg, givenAesKey);

        // then
        assertHttpRequestsEquals(httpRequest, givenHttpRequest);
    }

    @Test
    public void parseResponseShouldReturnProperResultWhenHttpResponsePassed() {
        // given
        String givenResponseResValue = "response res value";
        String givenResponseSign = "response sign value";
        ResponseBody givenResponseBody =
                ResponseBody.builder()
                        .activationId(givenActivationId())
                        .code(0)
                        .sign(givenResponseSign)
                        .response(givenResponseResValue)
                        .userId(givenUserId())
                        .build();
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

    private static HttpRequest givenHttpRequest(String payload, String aK, String aI) {
        return new HttpRequestImpl(
                HttpMethod.POST,
                new URL(givenBaseUrl() + "/MobileFlow/activateToken.htm"),
                String.format(
                        "request={\"payload\":\"%s\",\"aI\":\"%s\",\"aK\":\"%s\"}",
                        payload, aI, aK));
    }

    private static String givenRequestFormPayloadFieldAsString(Clock clock, String password) {
        return String.format(
                "{\"password\":\"%s\",\"fingerprint\":\"%s\",\"jailbroken\":%s,\"timestamp\":%s,\"pin\":\"%s\",\"auth_method\":\"%s\",\"username\":\"%s\"}",
                password,
                givenFingerprint(),
                notJailBroken(),
                givenTime(clock),
                givenPin(),
                "smartcode",
                givenUserId());
    }

    private static Result givenResult() {
        return Result.builder()
                .activationId(givenActivationId())
                .code(0)
                .result("ok")
                .message("")
                .challengeId(givenChallenge())
                .build();
    }

    private static String givenResultAsString() {
        Result result = givenResult();
        return String.format(
                "{\"activationID\":\"%s\",\"code\":%s,\"message\":\"\",\"result\":\"%s\",\"challenges_id\":\"%s\", \"certificateBase64\":\"%s\"}",
                result.getActivationId(),
                result.getCode(),
                result.getResult(),
                result.getChallengeId(),
                "MIIJrwIBAzCCCWgGCSqGSIb3DQEHAaCCCVkEggl");
    }
}
