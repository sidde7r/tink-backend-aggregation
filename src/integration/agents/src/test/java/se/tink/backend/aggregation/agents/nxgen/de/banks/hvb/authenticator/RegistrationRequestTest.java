package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator;

import static java.util.Collections.singletonList;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenBaseUrl;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenClientId;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenDeviceId;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenKeyPairMock;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenStaticHeaders;
import static se.tink.backend.aggregation.nxgen.http.request.HttpMethod.POST;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.RegistrationRequest.Payload;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.RegistrationRequest.RequestBody;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.RegistrationRequest.RequestBody.SignedRegistrationData;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.scaffold.ExternalApiCallResult;

public class RegistrationRequestTest {

    private static final String SERIALIZED_HEADER = "serializedHeader";
    private static final String SERIALIZED_PAYLOAD = "serializedPayload";
    private static final String SIGNATURE = "signature";

    private TinkHttpClient httpClient = mock(TinkHttpClient.class);
    private ConfigurationProvider configurationProvider = mock(ConfigurationProvider.class);
    private DataEncoder dataEncoder = mock(DataEncoder.class);

    private RegistrationRequest tested =
            new RegistrationRequest(httpClient, configurationProvider, dataEncoder);

    @Test
    public void prepareRequestShouldReturnHttpRequestForValidArg() {
        // given
        Payload givenPayload = Payload.withDefaultValues(givenDeviceId());
        when(dataEncoder.serializeAndBase64(givenPayload)).thenReturn(SERIALIZED_PAYLOAD);

        JwkHeader givenJwkHeader = new JwkHeader();
        when(dataEncoder.serializeAndBase64(givenJwkHeader)).thenReturn(SERIALIZED_HEADER);

        when(configurationProvider.getBaseUrl()).thenReturn(givenBaseUrl());
        when(configurationProvider.getStaticHeaders()).thenReturn(givenStaticHeaders());

        KeyPair givenKeyPair = givenKeyPairMock();

        when(dataEncoder.rsaSha256SignBase64Encode(
                        (RSAPrivateKey) givenKeyPair.getPrivate(),
                        SERIALIZED_HEADER + "." + SERIALIZED_PAYLOAD))
                .thenReturn(SIGNATURE);

        AuthenticationData givenAuthenticationData =
                AuthenticationData.forRegistration(givenDeviceId(), givenKeyPair, givenJwkHeader);

        // when
        HttpRequest results = tested.prepareRequest(givenAuthenticationData);

        // then
        assertThat(results.getBody()).isEqualTo(expectedBody());
        assertThat(results.getUrl()).isEqualTo(new URL(givenBaseUrl() + "/registration/v1/self"));
        assertThat(results.getMethod()).isEqualTo(POST);
        assertThat(results.getHeaders())
                .isNotEmpty()
                .contains(entry(CONTENT_TYPE, singletonList(APPLICATION_JSON)));
        assertThat(results.getHeaders().size()).isGreaterThan(1);
    }

    private static RequestBody expectedBody() {
        return new RequestBody()
                .setSignedRegistrationData(
                        new SignedRegistrationData()
                                .setPayload(SERIALIZED_PAYLOAD)
                                .setHeader(SERIALIZED_HEADER)
                                .setSignature(SIGNATURE));
    }

    @Test
    public void parseResponseShouldReturnClientIdForValidResponse() throws URISyntaxException {
        // given
        String givenLocation = "/mfp/api/registration/clients/" + givenClientId();
        int givenStatus = 200;

        HttpResponse givenResponse = mock(HttpResponse.class);
        when(givenResponse.getLocation()).thenReturn(new URI(givenLocation));
        when(givenResponse.getStatus()).thenReturn(givenStatus);

        // when
        ExternalApiCallResult<String> result = tested.parseResponse(givenResponse);

        // then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).isEqualTo(givenClientId());
    }
}
