package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.entities.ClientMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.entities.AuthorizeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.configuration.UkOpenBankingPisConfig;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class UkOpenBankingPisAuthApiClient extends OpenIdApiClient {

    private final RandomValueGenerator randomValueGenerator;

    public UkOpenBankingPisAuthApiClient(
            TinkHttpClient httpClient,
            JwtSigner signer,
            SoftwareStatementAssertion softwareStatement,
            String redirectUrl,
            ClientInfo providerConfiguration,
            RandomValueGenerator randomValueGenerator,
            UkOpenBankingPisConfig pisConfig) {
        super(
                httpClient,
                signer,
                softwareStatement,
                redirectUrl,
                providerConfiguration,
                pisConfig.getWellKnownURL(),
                randomValueGenerator);

        this.randomValueGenerator = randomValueGenerator;
    }

    public URL buildAuthorizeUrl(
            String state, String callbackUrl, ClientInfo clientInfo, String intentId) {
        final String nonce = randomValueGenerator.generateRandomHexEncoded(8);
        final URL authorizeUrl =
                super.buildAuthorizeUrl(state, nonce, ClientMode.PAYMENTS, callbackUrl, null);

        return authorizeUrl.queryParam(
                "request",
                AuthorizeRequest.create()
                        .withClientInfo(clientInfo)
                        .withPaymentsScope()
                        .withSoftwareStatement(softwareStatement)
                        .withRedirectUrl(getRedirectUrl())
                        .withState(state)
                        .withNonce(nonce)
                        .withWellKnownConfiguration(getWellKnownConfiguration())
                        .withIntentId(intentId)
                        .build(getSigner()));
    }

    public OAuth2Token requestClientCredentials() {
        return super.requestClientCredentials(ClientMode.PAYMENTS);
    }
}
