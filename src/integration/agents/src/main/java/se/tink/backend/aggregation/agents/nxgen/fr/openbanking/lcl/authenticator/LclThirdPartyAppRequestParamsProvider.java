package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.authenticator;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.configuration.LclConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.OAuth2ThirdPartyAppRequestParamsProvider;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class LclThirdPartyAppRequestParamsProvider
        implements OAuth2ThirdPartyAppRequestParamsProvider {

    private final AgentConfiguration<LclConfiguration> agentConfiguration;

    @Override
    public URL getAuthorizeUrl(String state) {
        final LclConfiguration lclConfiguration =
                agentConfiguration.getProviderSpecificConfiguration();
        return new URL(lclConfiguration.getAuthorizeUrl())
                .queryParam("response_type", "code")
                .queryParam("client_id", lclConfiguration.getClientId())
                .queryParam("redirect_uri", agentConfiguration.getRedirectUrl())
                .queryParam("scope", "aisp")
                .queryParam("state", state);
    }

    @Override
    public String getCallbackDataAuthCodeKey() {
        return "code";
    }
}
