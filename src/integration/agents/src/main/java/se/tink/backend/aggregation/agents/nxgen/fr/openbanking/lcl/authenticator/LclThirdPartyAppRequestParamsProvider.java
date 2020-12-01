package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.authenticator;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclAgent;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.configuration.LclConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.OAuth2ThirdPartyAppRequestParamsProvider;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class LclThirdPartyAppRequestParamsProvider
        implements OAuth2ThirdPartyAppRequestParamsProvider {

    private final AgentConfiguration<LclConfiguration> agentConfiguration;

    @SneakyThrows
    @Override
    public URL getAuthorizeUrl(String state) {
        return new URL("https://psu.lcl.fr/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", LclAgent.CLIENT_ID)
                .queryParam("redirect_uri", agentConfiguration.getRedirectUrl())
                .queryParam("scope", "aisp")
                .queryParam("state", state);
    }

    @Override
    public String getCallbackDataAuthCodeKey() {
        return "code";
    }
}
