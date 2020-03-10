package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator;

import static java.util.Collections.singletonList;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenApplicationSessionId;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenBaseUrl;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenClientId;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenCode;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenDirectBankingNumber;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenKeyPairMock;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenPin;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenStaticHeaders;
import static se.tink.backend.aggregation.nxgen.http.request.HttpMethod.POST;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.ConfigurationProvider;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.AccessTokenCall.Payload;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.JwkHeader.Jwk;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.scaffold.ExternalApiCallResult;

public class AccessTokenCallTest {

    private static final String SERIALIZED_HEADER = "serializedHeader";
    private static final String SERIALIZED_PAYLOAD = "serializedPayload";
    private static final String SIGNATURE = "signature";
    private static final String ACCESS_TOKEN_VALUE = "accessToken";
    private static final String ACCESS_TOKEN_TYPE = "accessTokenType";
    private static final String ACCESS_TOKEN_SCOPE = "accessTokenScope";
    private static final Long ACCESS_TOKEN_EXPIRES_IN = 1L;

    private TinkHttpClient httpClient = mock(TinkHttpClient.class);
    private ConfigurationProvider configurationProvider = mock(ConfigurationProvider.class);
    private DataEncoder dataEncoder = mock(DataEncoder.class);

    private AccessTokenCall tested =
            new AccessTokenCall(httpClient, configurationProvider, dataEncoder);

    @Test
    public void prepareRequestShouldReturnHttpRequestForValidArg() {
        // given
        when(configurationProvider.getBaseUrl()).thenReturn(givenBaseUrl());
        when(configurationProvider.getStaticHeaders()).thenReturn(givenStaticHeaders());

        JwkHeader expectedJwkHeader = dummyJwkHeader();
        expectedJwkHeader.getJwk().setKid(givenClientId());

        Instant givenInstant = Instant.now();

        when(dataEncoder.serializeAndBase64(expectedJwkHeader)).thenReturn(SERIALIZED_HEADER);
        when(dataEncoder.serializeAndBase64(Payload.of(givenCode(), givenClientId(), givenInstant)))
                .thenReturn(SERIALIZED_PAYLOAD);

        KeyPair givenKeyPair = givenKeyPairMock();
        when(dataEncoder.rsaSha256SignBase64Encode(
                        (RSAPrivateKey) givenKeyPair.getPrivate(),
                        SERIALIZED_HEADER + "." + SERIALIZED_PAYLOAD))
                .thenReturn(SIGNATURE);

        JwkHeader givenJqkHeader = dummyJwkHeader();

        AuthenticationData givenAuthenticationData =
                AuthenticationData.forAuthorization(
                                givenClientId(),
                                givenDirectBankingNumber(),
                                givenPin(),
                                givenKeyPair,
                                givenJqkHeader,
                                givenApplicationSessionId())
                        .setCode(givenCode())
                        .setInstant(givenInstant);

        // when
        HttpRequest results = tested.prepareRequest(givenAuthenticationData);

        // then
        assertThat(results.getBody()).isEqualTo(expectedBody());
        assertThat(results.getUrl()).isEqualTo(new URL(givenBaseUrl() + "/az/v1/token"));
        assertThat(results.getMethod()).isEqualTo(POST);
        assertThat(results.getHeaders())
                .isNotEmpty()
                .contains(
                        entry(CONTENT_TYPE, singletonList(APPLICATION_FORM_URLENCODED)),
                        entry(ACCEPT, singletonList("*/*")));
    }

    private JwkHeader dummyJwkHeader() {
        return new JwkHeader().setJwk(new Jwk());
    }

    private String expectedBody() {
        return "client_assertion="
                + SERIALIZED_HEADER
                + "."
                + SERIALIZED_PAYLOAD
                + "."
                + SIGNATURE
                + "&client_assertion_type=urn%3Aietf%3Aparams%3Aoauth%3Aclient-assertion-type%3Ajwt-bearer"
                + "&code="
                + givenCode()
                + "&grant_type=authorization_code"
                + "&redirect_uri=https%3A%2F%2Fmfpredirecturi";
    }

    @Test
    public void prepareResponseShouldReturnAccessTokenForValidResponse() {
        // given
        int givenStatus = 200;

        HttpResponse givenResponse = mock(HttpResponse.class);
        when(givenResponse.getBody(AccessTokenResponse.class)).thenReturn(givenAccessToken());
        when(givenResponse.getStatus()).thenReturn(givenStatus);

        // when
        ExternalApiCallResult<AccessTokenResponse> result = tested.parseResponse(givenResponse);

        // then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).isEqualTo(givenAccessToken());
    }

    private static AccessTokenResponse givenAccessToken() {
        return new AccessTokenResponse()
                .setAccessToken(ACCESS_TOKEN_VALUE)
                .setExpiresIn(ACCESS_TOKEN_EXPIRES_IN)
                .setScope(ACCESS_TOKEN_SCOPE)
                .setTokenType(ACCESS_TOKEN_TYPE);
    }
}
