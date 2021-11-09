package se.tink.backend.aggregation.agents.nxgen.ie.openbanking.permanenttsb;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.ukob.rpc.AccountPermissionRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.entities.ClientMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.TinkJwt;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner.Algorithm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.TokenRequestForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.WellKnownResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class PermanentTsbApiClient extends UkOpenBankingApiClient {

    private final UkOpenBankingAisConfig aisConfig;
    private final String qsealPem;

    public PermanentTsbApiClient(
            TinkHttpClient httpClient,
            JwtSigner signer,
            SoftwareStatementAssertion softwareStatement,
            String redirectUrl,
            ClientInfo providerConfiguration,
            RandomValueGenerator randomValueGenerator,
            PersistentStorage persistentStorage,
            UkOpenBankingAisConfig aisConfig,
            AgentComponentProvider componentProvider,
            String qsealcPem) {
        super(
                httpClient,
                signer,
                softwareStatement,
                redirectUrl,
                providerConfiguration,
                randomValueGenerator,
                persistentStorage,
                aisConfig,
                componentProvider);
        this.aisConfig = aisConfig;
        this.qsealPem = qsealcPem;
    }

    @Override
    public OAuth2Token requestClientCredentials(ClientMode scope) {
        TokenRequestForm postData = createTokenRequestForm("client_credentials", scope);
        postData.put("client_assertion", buildSignedClientAssertion());

        return createTokenRequest().body(postData).post(TokenResponse.class).toAccessToken();
    }

    @Override
    protected RequestBuilder createBasicTokenRequest(WellKnownResponse wellKnownConfiguration) {
        return httpClient
                .request(wellKnownConfiguration.getTokenEndpoint())
                .header(
                        "tpp-signature-certificate",
                        Base64.getEncoder().encodeToString(qsealPem.getBytes()))
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }

    @Override
    protected RequestBuilder createConsentRequest(AccountPermissionRequest permissionRequest) {
        return createAisRequest(aisConfig.createConsentRequestURL())
                .queryParam("transactionFromDateTime", LocalDate.now().minusYears(10).toString())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .body(permissionRequest);
    }

    private String buildSignedClientAssertion() {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(TimeUnit.MINUTES.toSeconds(10));

        return TinkJwt.create()
                .withJWTId(UUID.randomUUID().toString())
                .withExpiresAt(expiresAt)
                .signAttached(Algorithm.PS256, signer);
    }

    protected TokenRequestForm createTokenRequestForm(String grantType, ClientMode mode) {
        WellKnownResponse wellKnownConfiguration = getWellKnownConfiguration();

        String scope =
                wellKnownConfiguration
                        .verifyAndGetScopes(createScope(mode))
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Provider does not support the mandatory scopes."));

        TokenRequestForm requestForm =
                new TokenRequestForm()
                        .withGrantType(grantType)
                        .withScope(scope)
                        .withRedirectUri(redirectUrl);

        handleFormAuthentication(requestForm, wellKnownConfiguration);

        return requestForm;
    }

    private List<String> createScope(ClientMode mode) {
        return Arrays.asList("openid", mode.getValue());
    }
}
