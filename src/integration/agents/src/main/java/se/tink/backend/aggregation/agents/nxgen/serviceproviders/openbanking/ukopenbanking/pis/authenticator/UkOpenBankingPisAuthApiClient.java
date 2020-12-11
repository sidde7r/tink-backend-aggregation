package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator;

import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.entities.ClientMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.entities.AuthorizeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.SigningAlgorithm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.WellKnownResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.configuration.UkOpenBankingPisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.storage.UkOpenBankingPaymentStorage;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class UkOpenBankingPisAuthApiClient extends OpenIdApiClient {

    private static final List<SigningAlgorithm> PREFERRED_ID_TOKEN_SIGNING_ALGORITHM =
            Arrays.asList(SigningAlgorithm.PS256, SigningAlgorithm.RS256);

    private final UkOpenBankingPaymentStorage paymentStorage;
    private final RandomValueGenerator randomValueGenerator;

    public UkOpenBankingPisAuthApiClient(
            TinkHttpClient httpClient,
            JwtSigner signer,
            SoftwareStatementAssertion softwareStatement,
            String redirectUrl,
            ClientInfo providerConfiguration,
            RandomValueGenerator randomValueGenerator,
            UkOpenBankingPisConfig pisConfig,
            UkOpenBankingPaymentStorage paymentStorage) {
        super(
                httpClient,
                signer,
                softwareStatement,
                redirectUrl,
                providerConfiguration,
                pisConfig.getWellKnownURL(),
                randomValueGenerator);

        this.randomValueGenerator = randomValueGenerator;
        this.paymentStorage = paymentStorage;
    }

    public URL buildAuthorizeUrl(
            String state, String callbackUrl, ClientInfo clientInfo, String intentId) {
        final String nonce = randomValueGenerator.generateRandomHexEncoded(8);
        final URL authorizeUrl = super.buildAuthorizeUrl(state, nonce, ClientMode.PAYMENTS, callbackUrl);

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

    @Override
    public WellKnownResponse getWellKnownConfiguration() {
        final WellKnownResponse wellKnownResponse = super.getWellKnownConfiguration();

        paymentStorage.storePreferredSigningAlgorithm(getPreferredAlgorithm(wellKnownResponse));

        return wellKnownResponse;
    }

    OAuth2Token requestClientCredentials() {
        return super.requestClientCredentials(ClientMode.PAYMENTS);
    }

    private SigningAlgorithm getPreferredAlgorithm(WellKnownResponse wellKnownResponse) {
        return wellKnownResponse
                .getPreferredIdTokenSigningAlg(PREFERRED_ID_TOKEN_SIGNING_ALGORITHM)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Preferred signing algorithm unknown: only RS256 and PS256 are supported"));
    }
}
