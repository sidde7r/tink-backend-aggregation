package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication;

import com.google.common.collect.Sets;
import java.net.URI;
import java.util.Set;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence.KbcPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectUrlBuilderAuthenticationParameters;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.oauth2.OAuth2RedirectUrlBuilder;
import se.tink.backend.aggregation.api.Psd2Headers;

@AllArgsConstructor
public class KbcRedirectUrlBuilder extends OAuth2RedirectUrlBuilder {

    private final String clientId;
    private final URI redirectUrl;
    private final KbcPersistedDataAccessorFactory persistedDataAccessorFactory;

    @Override
    protected Set<String> getScopes(RedirectUrlBuilderAuthenticationParameters input) {
        return Sets.newHashSet(
                "AIS:"
                        + persistedDataAccessorFactory
                                .createKbcAuthenticationPersistedDataAccessor(
                                        input.getPersistedData())
                                .getKbcAuthenticationData()
                                .getConsentId());
    }

    @Override
    protected URI getAuthorizationEndpoint(RedirectUrlBuilderAuthenticationParameters input) {
        String urlTemplate =
                "https://idp.kbc.com/ASK/oauth/authorize/1?code_challenge=%s&code_challenge_method=S256";
        final String codeChallenge =
                Psd2Headers.generateCodeChallenge(
                        persistedDataAccessorFactory
                                .createKbcAuthenticationPersistedDataAccessor(
                                        input.getPersistedData())
                                .getKbcAuthenticationData()
                                .getCodeVerifier());
        return URI.create(String.format(urlTemplate, codeChallenge));
    }

    @Override
    protected String getClientId(RedirectUrlBuilderAuthenticationParameters input) {
        return clientId;
    }

    @Override
    protected URI getRedirectUrl() {
        return redirectUrl;
    }
}
