package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.AuthenticateRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.AuthenticationContextEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.ProxyAuthenticateRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.crypto.IngCryptoUtils;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RunWith(MockitoJUnitRunner.class)
public class ProxyFilterTest {

    private static final byte[] ENCRYPTION_KEY =
            Hex.decode("052c17a3e22473467fefd3b4041e1a8cd4e86cf5bd6b733a4d5edf550971cec7");

    private static final byte[] SIGNING_KEY =
            Hex.decode(
                    "a9051f4ab4923bf1d4b98b4aa73d7d56a911aa5fbd14980fb886e129e8ec75695c2fc9bd711209dbb7393dc3cf8e858f5cf83512330fa456be7c2345e9ed62b0");

    private static final byte[] ENCRYPTED_RESPONSE_BODY =
            Hex.decode(
                    "8186083175f1acefd54a2eecf694a2014bcb2a80e8b4e1344ebf6ab0daa3c313"
                            + "364afdaa27ee113dda9a8883fa07c8f5762aa340c80514cf5c18461bcd1211a7"
                            + "ae507b9e66626a79aeca23bbe210e0ee3f975d28bf21d6a5ea6cdcfd4163abe7"
                            + "e115e6c7c88409b2a9a01610000f5613f57c5c25f495860911b0792f4c4e7a1f"
                            + "2f3f90b873a0a4dd451f4005b8f0b642de32971bc6164e05ecfb5a3b71fba40c"
                            + "5903a003946b60993d25ebe6ae21515c4127441fb4ac3e513e22a2898a82d08c"
                            + "f964dab9bad74e7a033eae578a2c28b78f32b030b4d256a560e8aeb520f01ef7"
                            + "32e9b239b1ceac9ee4b16003c853daa05c7f7de6671dfe3eadbba6f226c66b7d"
                            + "57c89e104312e18a7d64129f8929296028441c17bdcb29cfdca05e32c7681846"
                            + "e4a81fccf337a066f54f721acf1ae66446cac283a5b6705e4bc164f5706ca706"
                            + "d03921670e99f223027af12c7d6d7b391cb9f2224c679c788e88d934c91f3342"
                            + "16a45720ac5f895298470404207e987e9f99bcf23a96a9391efdab12e73b15fb"
                            + "bdc82990ae12e23f6940b078bdb41067774640fce606b55f156816594d745ef9"
                            + "1b29678a2c379d15f6d6fa230697de59ea6d484b70085f77cf08f11b4758bf52"
                            + "4f14cc9944204bc39ee743dafac2ebcf7f327d4a7c6b4e3d2e0784ee8e751d41"
                            + "27cf7292d163eaa0d59ee99358c57e4c8b904bdc74cc7b3954291c0b4222169f"
                            + "1dc6a7dd1d2eef760848bcbaaa755094ea9fe6ab9cab3965efe5a82b5c0ec4e2"
                            + "0ae4b2dbf00d147e26febfed04869bc7e1a809f355e85dc775b53cc379a714a0"
                            + "2b03627ad6adeb1154c9709ff4e6dd7ec1f8be0ce3101db90f4817dafaf169d0"
                            + "7b8a52ad35751047a7a4f9df131710437424f11fc0c6df483a94963f824096b4"
                            + "9c263f87793e3c1208cabd445694a44745347709902cdd9fa9730f95178244e7"
                            + "1c490c0d83c6c13e0ce200d36fdb4c3be9004754c56e7ed061092ec314d9da36"
                            + "5fc250f9c2097e336626207dfcac1237151ea949538aeae7868425dc11f5d9fc"
                            + "526febf111653b3d5254ba46ce7afffffab64c8db6062d1226713075daddb59f"
                            + "988c305813a08c894bdabb1e1506ff14");

    @Mock private IngStorage ingStorage;

    @Mock private IngCryptoUtils ingCryptoUtils;

    @Mock private IngLoggingAdapter ingLoggingAdapter;

    @InjectMocks private ProxyFilter proxyFilter;

    private ArgumentCaptor<ByteArrayInputStream> responseBodyArgumentCaptor =
            ArgumentCaptor.forClass(ByteArrayInputStream.class);

    @Test
    public void shouldTransformBody() {
        when(ingStorage.getSigningKey()).thenReturn(new SecretKeySpec(SIGNING_KEY, "HmacSHA256"));
        when(ingStorage.getEncryptionKey())
                .thenReturn(new SecretKeySpec(ENCRYPTION_KEY, "HmacSHA256"));

        when(ingCryptoUtils.getRandomBytes(16))
                .thenReturn(Hex.decode("85996d143ddafddc288eb105dbcdfdf5"));

        String signatureForRequest = "signature_for_request";
        when(ingCryptoUtils.calculateSignature(any(), any(SecretKeySpec.class)))
                .thenReturn(
                        signatureForRequest.getBytes(),
                        Hex.decode(
                                "151ea949538aeae7868425dc11f5d9fc526febf111653b3d5254ba46ce7affff"
                                        + "fab64c8db6062d1226713075daddb59f988c305813a08c894bdabb1e1506ff14"));

        ProxyAuthenticateRequest request = mockRequest();
        HttpRequest httpRequest =
                new HttpRequestImpl(HttpMethod.POST, URL.of("https://localhost:3232"), request);

        Filter mockedNext = mock(Filter.class);
        proxyFilter.setNext(mockedNext);

        HttpResponse mockedResponse = mockResponse();

        when(mockedNext.handle(any())).thenReturn(mockedResponse);

        proxyFilter.handle(httpRequest);

        // request was transformed into encrypted array
        byte[] requestBody = (byte[]) httpRequest.getBody();
        assertThat(requestBody).isNotNull().isExactlyInstanceOf(byte[].class);
        assertThat(requestBody[0]).isEqualTo(Hex.decode("a3")[0]);

        // request was signed
        byte[] lastBytes =
                Arrays.copyOfRange(
                        requestBody,
                        requestBody.length - signatureForRequest.length(),
                        requestBody.length);
        assertThat(new String(lastBytes)).isEqualTo(signatureForRequest);

        // response was transformed into decrypted JSON
        assertThat(mockedResponse.getHeaders()).hasSize(1);
        assertThat(mockedResponse.getHeaders().getFirst("Content-Type"))
                .isEqualTo(MediaType.APPLICATION_JSON);

        verify(mockedResponse.getInternalResponse())
                .setEntityInputStream(responseBodyArgumentCaptor.capture());

        ByteArrayInputStream value = responseBodyArgumentCaptor.getValue();

        byte[] shouldBeObjectStart = new byte[1];
        int read = value.read(shouldBeObjectStart, 0, 1);
        assertThat(read).isOne();
        assertThat((char) shouldBeObjectStart[0]).isEqualTo('{');
    }

    private ProxyAuthenticateRequest mockRequest() {
        return new ProxyAuthenticateRequest(
                AuthenticateRequestEntity.builder()
                        .responseCode("12345678")
                        .ingId("0354319677")
                        .cardId("67033032508605014")
                        .encryptedId("")
                        .authenticationContext(
                                AuthenticationContextEntity.builder()
                                        .requiredLevelOfAssurance(1)
                                        .clientId("706711ed-8111-4d65-a035-7441083e2079")
                                        .identifyeeType("customer")
                                        .scopes(new String[] {"personal_data"})
                                        .build())
                        .keyId("")
                        .build());
    }

    private HttpResponse mockResponse() {
        HttpResponse mockedResponse = mock(HttpResponse.class);
        when(mockedResponse.getBody(any())).thenReturn(ENCRYPTED_RESPONSE_BODY);
        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
        when(mockedResponse.getHeaders()).thenReturn(headers);
        ClientResponse mockedClientResponse = mock(ClientResponse.class);
        when(mockedResponse.getInternalResponse()).thenReturn(mockedClientResponse);

        return mockedResponse;
    }
}
