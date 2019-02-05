package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking;

import com.google.common.base.Strings;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.configuration.UkOpenBankingConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.WellKnownResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class OpenIdApiClientTest {

    private OpenIdApiClient apiClient;

    private final UkOpenBankingConfiguration UKOB_TEST_CONFIG = SerializationUtils.deserializeFromString("{}",
            UkOpenBankingConfiguration.class);

    @Before
    public void setup() {
        UKOB_TEST_CONFIG.validate();

        TinkHttpClient httpClient = new TinkHttpClient();
        httpClient.disableSignatureRequestHeader();
        httpClient.trustRootCaCertificate(UKOB_TEST_CONFIG.getRootCAData(),
                UKOB_TEST_CONFIG.getRootCAPassword());

        SoftwareStatement softwareStatement = UKOB_TEST_CONFIG.getSoftwareStatement("tink")
                .orElseThrow(AssertionError::new);

        ProviderConfiguration providerConfiguration = softwareStatement.getProviderConfiguration("modelo")
                .orElseThrow(AssertionError::new);

        apiClient = new OpenIdApiClient(httpClient, softwareStatement, providerConfiguration,
                OpenIdConstants.ClientMode.ACCOUNTS);
    }

    @Test
    public void testGetConfiguration() {
        WellKnownResponse conf = apiClient.getWellKnownConfiguration();

        Assert.assertNotNull(conf);
        Assert.assertTrue(conf.verifyAndGetScopes(
                Arrays.asList(OpenIdConstants.Scopes.OPEN_ID, OpenIdConstants.Scopes.ACCOUNTS))
                .isPresent());
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
