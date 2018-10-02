package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid;

import com.google.common.base.Strings;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatement;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.configuration.UkOpenBankingConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.RegistrationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.WellKnownResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class OpenIdApiClientTest {

    private TinkHttpClient httpClient;
    private OpenIdApiClient apiClient;
    private SoftwareStatement softwareStatement;
    private ProviderConfiguration providerConfiguration;

    private static final UkOpenBankingConfiguration UKOB_TEST_CONFIG = SerializationUtils.deserializeFromString("",
            UkOpenBankingConfiguration.class);

    @Before
    public void setup() {
        UKOB_TEST_CONFIG.validate();

        httpClient = new TinkHttpClient(null, null);
        httpClient.disableSignatureRequestHeader();
        httpClient.trustRootCaCertificate(UKOB_TEST_CONFIG.getRootCAData(),
                UKOB_TEST_CONFIG.getRootCAPassword());


        softwareStatement = UKOB_TEST_CONFIG.getSoftwareStatement("tink")
                .orElseThrow(AssertionError::new);

        providerConfiguration = softwareStatement.getProviderConfiguration("modelo")
                .orElseThrow(AssertionError::new);

        apiClient = new OpenIdApiClient(httpClient, softwareStatement, providerConfiguration);
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
        OAuth2Token clientCredentials = apiClient.requestClientCredentials();
        Assert.assertTrue(clientCredentials.isValid());
        Assert.fail(SerializationUtils.serializeToString(clientCredentials));
    }

    @Test
    public void testAuthorizeConsent() {
        URL authUrl = apiClient.buildAuthorizeUrl("state-test", "nonce-test");
        System.out.println(authUrl.toString());
        Assert.assertTrue(!Strings.isNullOrEmpty(authUrl.toString()));
    }
}
