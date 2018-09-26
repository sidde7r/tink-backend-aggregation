package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.UkOpenBankingConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.RegistrationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.WellKnownResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class OpenIdApiClientTest {

    private TinkHttpClient httpClient;
    private OpenIdApiClient apiClient;
    private static final UkOpenBankingConfiguration UKOB_TEST_CONFIG = fromJson("CENCORED", UkOpenBankingConfiguration.class);

    @Before
    public void setup() {

        httpClient = new TinkHttpClient(null, null);
        httpClient.disableSignatureRequestHeader();
        httpClient.trustRootCaCertificate(UKOB_TEST_CONFIG.getRootCAData(), UKOB_TEST_CONFIG.getRootCAPassword());

        apiClient = new OpenIdApiClient(httpClient,
                UKOB_TEST_CONFIG.getSoftwareStatement("tink"),
                UKOB_TEST_CONFIG.getSoftwareStatement("tink")
                        .getProviderConfiguration("modelo"));
    }

    private static <T extends Object> T fromJson(String json, Class<T> type) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, type);
        }catch (IOException e){
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void testGetConfiguration() {
        WellKnownResponse conf = apiClient.getProviderConfiguration();

        Assert.assertNotNull(conf);
        Assert.assertTrue(conf.verifyAndGetResponseTypes(
                Arrays.asList(OpenIdConstants.RESPONSE_TYPES))
                .isPresent());
        Assert.assertTrue(conf.verifyAndGetScopes(
                OpenIdConstants.SCOPES)
                .isPresent());
    }

    @Test
    public void testRegistration() {

        //TODO: Validate response
        RegistrationResponse response = apiClient.registerClient();
        Assert.assertNotNull(response);
    }

    @Test(expected = HttpResponseException.class)
    public void testTrustRootCA() {
        httpClient.request("https://modelobank2018.o3bank.co.uk:4201/token").get(String.class);
    }

    @Test
    public void testClientCertificate() {
        byte[] clientP12 = UKOB_TEST_CONFIG.getSoftwareStatement("tink").getTransportKeyP12();

        httpClient.setSslClientCertificate(clientP12, "");
        try {
            httpClient.request("https://modelobank2018.o3bank.co.uk:4201/token").get(String.class);
        } catch (HttpResponseException hre) {
            Assert.assertEquals(hre.getResponse().getStatus(), HttpStatus.SC_NOT_FOUND);
            return;
        }

        Assert.fail("should not reach this code");
    }
}
