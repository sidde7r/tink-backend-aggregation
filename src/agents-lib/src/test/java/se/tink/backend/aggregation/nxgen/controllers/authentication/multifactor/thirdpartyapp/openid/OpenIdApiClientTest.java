package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import java.io.IOException;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.UkOpenBankingConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.RegistrationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.WellKnownResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class OpenIdApiClientTest {

    private TinkHttpClient httpClient;
    private OpenIdApiClient apiClient;

    private static final UkOpenBankingConfiguration UKOB_TEST_CONFIG = fromJson("",
            UkOpenBankingConfiguration.class);

    @Before
    public void setup() {
        UKOB_TEST_CONFIG.validate();

        httpClient = new TinkHttpClient(null, null);
        httpClient.disableSignatureRequestHeader();
        httpClient.trustRootCaCertificate(UKOB_TEST_CONFIG.getRootCAData(),
                UKOB_TEST_CONFIG.getRootCAPassword());

        apiClient = new OpenIdApiClient(httpClient,
                UKOB_TEST_CONFIG.getSoftwareStatement("tink"),
                UKOB_TEST_CONFIG.getSoftwareStatement("tink")
                        .getProviderConfiguration("modelo"));
    }

    private static <T> T fromJson(String json, Class<T> type) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void testGetConfiguration() {
        WellKnownResponse conf = apiClient.getWellKnownConfiguration();

        Assert.assertNotNull(conf);
        Assert.assertTrue(conf.verifyAndGetScopes(
                OpenIdConstants.SCOPES)
                .isPresent());
    }

    @Test
    public void testRegistration() {

        //TODO: Validate response
        RegistrationResponse response = apiClient.registerClient();
        System.out.println(response.toString());
        Assert.assertNotNull(response);
    }

    @Test
    public void testCredentialRequest() {
        AuthenticationToken clientCredentials = apiClient.requestClientCredentials();
        Assert.assertTrue(clientCredentials.isValid());
        Assert.fail(SerializationUtils.serializeToString(clientCredentials));
    }

    @Test
    public void TestAccountPermissionRequest() {
        AuthenticationToken clientCredentials = apiClient.requestClientCredentials();
        AccountPermissionResponse response = apiClient.requestAccountsApi(clientCredentials);
        Assert.assertNotNull(response);
    }

    @Test
    public void testAuthorizeConsent(){
        AuthenticationToken clientCredentials = apiClient.requestClientCredentials();
        AccountPermissionResponse response = apiClient.requestAccountsApi(clientCredentials);
        URL authUrl = apiClient.buildAuthorizeUrl("test", response.getData().getAccountRequestId());
        System.out.println(authUrl.toString());
        Assert.assertTrue(!Strings.isNullOrEmpty(authUrl.toString()));
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
