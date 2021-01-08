package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication;

import agents_platform_agents_framework.org.springframework.web.util.UriComponentsBuilder;
import com.google.common.collect.Sets;
import java.net.URI;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence.KbcPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectUrlBuilderAuthenticationParameters;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.oauth2.OAuth2RedirectUrlBuilder;
import se.tink.backend.aggregation.agentsplatform.framework.encode.UtfEncoder;
import se.tink.backend.aggregation.api.Psd2Headers;

@AllArgsConstructor
public class KbcRedirectUrlBuilder extends OAuth2RedirectUrlBuilder {

    private final String clientId;
    private final URI redirectUrl;
    private final KbcPersistedDataAccessorFactory persistedDataAccessorFactory;
    private final String state;

    // TODO: Remove when framework fix is available.
    // region to-remove
    @Override
    public String createAuthorizationUrl(RedirectUrlBuilderAuthenticationParameters parameters) {
        UriComponentsBuilder uriComponentsBuilder =
                UriComponentsBuilder.newInstance().uri(this.getAuthorizationEndpoint(parameters));
        this.addQuery(uriComponentsBuilder, parameters);
        return uriComponentsBuilder.build().toUriString();
    }

    private void addQuery(
            UriComponentsBuilder uriComponentsBuilder,
            RedirectUrlBuilderAuthenticationParameters parameters) {
        this.addClientIdQuery(uriComponentsBuilder, parameters);
        this.addRedirectUriQuery(uriComponentsBuilder);
        this.addResponseTypeQuery(uriComponentsBuilder);
        this.addScopeQuery(uriComponentsBuilder, parameters);
        this.addStateQuery(uriComponentsBuilder);
        this.addClientAdditionalParams(uriComponentsBuilder, parameters);
    }

    private void addClientIdQuery(
            UriComponentsBuilder uriComponentsBuilder,
            RedirectUrlBuilderAuthenticationParameters parameters) {
        uriComponentsBuilder.query("client_id=" + this.urlEncode(this.getClientId(parameters)));
    }

    private void addScopeQuery(
            UriComponentsBuilder uriComponentsBuilder,
            RedirectUrlBuilderAuthenticationParameters parameters) {
        Optional.ofNullable(this.getScopes(parameters))
                .map(
                        (scopes) -> {
                            return this.urlEncode(String.join(" ", scopes));
                        })
                .map(
                        (scopes) -> {
                            return uriComponentsBuilder.query("scope=" + scopes);
                        })
                .orElse(uriComponentsBuilder);
    }

    private void addRedirectUriQuery(UriComponentsBuilder uriComponentsBuilder) {
        uriComponentsBuilder.query(
                "redirect_uri=" + this.urlEncode(this.getRedirectUrl().toString()));
    }

    private void addResponseTypeQuery(UriComponentsBuilder uriComponentsBuilder) {
        uriComponentsBuilder.query("response_type=code");
    }

    private void addStateQuery(UriComponentsBuilder uriComponentsBuilder) {
        uriComponentsBuilder.query("state=" + state);
    }

    private void addClientAdditionalParams(
            UriComponentsBuilder uriComponentsBuilder,
            RedirectUrlBuilderAuthenticationParameters parameters) {
        this.getClientAdditionalParams(parameters)
                .entrySet()
                .forEach(
                        (entry) -> {
                            uriComponentsBuilder.queryParam(
                                    (String) entry.getKey(),
                                    new Object[] {this.urlEncode((String) entry.getValue())});
                        });
    }

    private String urlEncode(String value) {
        return UtfEncoder.encodeUtf8(value);
    }

    // endregion

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
