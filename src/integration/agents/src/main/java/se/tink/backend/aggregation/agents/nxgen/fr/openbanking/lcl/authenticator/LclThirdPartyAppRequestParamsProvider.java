package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.authenticator;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.configuration.LclConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.OAuth2ThirdPartyAppRequestParamsProvider;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.unleash.UnleashClient;
import se.tink.libraries.unleash.model.Toggle;

@Slf4j
@RequiredArgsConstructor
public class LclThirdPartyAppRequestParamsProvider
        implements OAuth2ThirdPartyAppRequestParamsProvider {

    private final AgentConfiguration<LclConfiguration> agentConfiguration;
    private final UnleashClient unleashClient;

    @SneakyThrows
    @Override
    public URL getAuthorizeUrl(String state) {
        return new URL(getAuthorizeEndpoint())
                .queryParam("response_type", "code")
                .queryParam(
                        "client_id",
                        agentConfiguration.getProviderSpecificConfiguration().getClientId())
                .queryParam("redirect_uri", agentConfiguration.getRedirectUrl())
                .queryParam("scope", "aisp")
                .queryParam("state", state);
    }

    @Override
    public String getCallbackDataAuthCodeKey() {
        return "code";
    }

    private String getAuthorizeEndpoint() {
        if (unleashClient.isToggleEnabled(Toggle.of("fr-lcl-ais-app2app-redirect").build())) {
            log.info("LCL app to app enabled.");
            return "https://psu.lcl.fr/retail/authorize";
        } else {
            log.info("LCL app to app disabled.");
            return "https://psu.lcl.fr/authorize";
        }
    }
}
