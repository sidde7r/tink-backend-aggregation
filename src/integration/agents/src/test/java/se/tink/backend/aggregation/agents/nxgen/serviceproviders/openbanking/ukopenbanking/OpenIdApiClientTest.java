package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking;

import com.google.common.base.Strings;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.ClientMode;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.LocalKeySigner;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.WellKnownResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.tls.LocalCertificateTlsConfiguration;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.LegacyTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class OpenIdApiClientTest {

    private OpenIdApiClient apiClient;
    private String softwareStatementData = "{}";
    private String providerConfigurationData = "{}";
    private String rootCAData = "";
    private String rootCAPassword = "";
    private String transportKeyId = "";
    private String transportKey = "";
    private String transportPassword = "";
    private String signingKeyId = "";
    private String signingKey = "";

    private final SoftwareStatementAssertion softwareStatement =
            SerializationUtils.deserializeFromString(
                    softwareStatementData, SoftwareStatementAssertion.class);

    private final ProviderConfiguration providerConfiguration =
            SerializationUtils.deserializeFromString(
                    providerConfigurationData, ProviderConfiguration.class);

    @Before
    public void setup() {

        TinkHttpClient httpClient = new LegacyTinkHttpClient();
        httpClient.disableSignatureRequestHeader();
        httpClient.trustRootCaCertificate(
                EncodingUtils.decodeBase64String(rootCAData), rootCAPassword);

        apiClient =
                new OpenIdApiClient(
                        httpClient,
                        new LocalKeySigner(
                                signingKeyId,
                                RSA.getPrivateKeyFromBytes(
                                        EncodingUtils.decodeBase64String(signingKey))),
                        new LocalCertificateTlsConfiguration(
                                transportKeyId, transportKey, transportPassword),
                        softwareStatement,
                        providerConfiguration,
                        new URL(
                                "https://sandbox-obp-api.danskebank.com/sandbox-open-banking/private/.well-known/openid-configuration"));
    }

    @Test
    public void testGetConfiguration() {
        WellKnownResponse conf = apiClient.getWellKnownConfiguration();

        Assert.assertNotNull(conf);
        Assert.assertTrue(
                conf.verifyAndGetScopes(
                                Arrays.asList(
                                        OpenIdConstants.Scopes.OPEN_ID,
                                        OpenIdConstants.Scopes.ACCOUNTS))
                        .isPresent());
    }

    @Test
    public void testCredentialRequest() {
        OAuth2Token clientCredentials = apiClient.requestClientCredentials(ClientMode.ACCOUNTS);
        Assert.assertTrue(clientCredentials.isValid());
        Assert.fail(SerializationUtils.serializeToString(clientCredentials));
    }

    @Test
    public void testAuthorizeConsent() {
        URL authUrl =
                apiClient.buildAuthorizeUrl(
                        "state-test", "nonce-test", ClientMode.ACCOUNTS, "callbackUri-test", null);
        System.out.println(authUrl.toString());
        Assert.assertTrue(!Strings.isNullOrEmpty(authUrl.toString()));
    }
}
