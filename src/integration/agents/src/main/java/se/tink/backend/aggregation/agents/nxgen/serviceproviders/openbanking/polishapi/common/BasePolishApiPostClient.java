package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.common;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderKeys.ACCEPT;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderKeys.ACCEPT_CHARSET;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderKeys.ACCEPT_ENCODING;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderKeys.ACCEPT_LANGUAGE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderKeys.DATE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderKeys.X_IBM_CLIENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderKeys.X_IBM_CLIENT_SECRET;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderValues.ACCEPT_CHARSET_VAL;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderValues.GetClient.PSU_USER_AGENT_VAL;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Localization.DATE_TIME_FORMATTER_HEADERS;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Localization.DATE_TIME_FORMATTER_REQUEST_HEADERS;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.common.dto.requests.RequestHeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.concreteagents.PolishApiAgentCreator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiPersistentStorage;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class BasePolishApiPostClient {

    private final TinkHttpClient httpClient;
    private final AgentComponentProvider agentComponentProvider;
    protected final AgentConfiguration<PolishApiConfiguration> configuration;
    protected final PolishApiPersistentStorage persistentStorage;
    protected final PolishApiAgentCreator polishApiAgentCreator;

    protected RequestBuilder getRequestWithBaseHeaders(
            URL requestUrl, ZonedDateTime requestTime, OAuth2Token token) {
        PolishApiConfiguration apiConfiguration = configuration.getProviderSpecificConfiguration();
        RequestBuilder header =
                httpClient
                        .request(requestUrl)
                        .header(ACCEPT, MediaType.APPLICATION_JSON)
                        .header(ACCEPT_CHARSET, ACCEPT_CHARSET_VAL)
                        .header(ACCEPT_ENCODING, "deflate")
                        .header(ACCEPT_LANGUAGE, getLanguageCode())
                        .header(DATE, requestTime.format(DATE_TIME_FORMATTER_HEADERS))
                        .header(X_IBM_CLIENT_ID, apiConfiguration.getApiKey())
                        .header(X_IBM_CLIENT_SECRET, apiConfiguration.getClientSecret());
        if (token != null) {
            header.addBearerToken(token);
        }
        return header;
    }

    @SneakyThrows
    protected RequestHeaderEntity getRequestHeaderEntity(
            String requestId, ZonedDateTime requestTime, String token) {
        String apiKey = configuration.getProviderSpecificConfiguration().getApiKey();
        RequestHeaderEntity.RequestHeaderEntityBuilder<?, ?> builder =
                RequestHeaderEntity.builder()
                        .apiKey(apiKey)
                        .callbackURL(configuration.getRedirectUrl())
                        .isDirectPsu(isUserPresent())
                        .sendDate(requestTime.format(DATE_TIME_FORMATTER_REQUEST_HEADERS))
                        .tppId(
                                CertificateUtils.getOrganizationIdentifier(
                                        configuration.getQsealc()))
                        .requestId(requestId)
                        .isCompanyContext(false)
                        .token(setToken(token));
        if (isUserPresent()) {
            builder.ipAddress(agentComponentProvider.getCredentialsRequest().getOriginatingUserIp())
                    .userAgent(PSU_USER_AGENT_VAL);
        }

        if (polishApiAgentCreator.shouldSentClientIdInRequestHeaderBody()) {
            builder.clientId(apiKey);
        }

        return builder.build();
    }

    private String setToken(String token) {
        if (token == null) {
            return null;
        }
        if (polishApiAgentCreator.shouldAddBearerStringInTokenInRequestBody()) {
            return "Bearer " + token;
        } else {
            return token;
        }
    }

    protected String getUuid() {
        return agentComponentProvider.getRandomValueGenerator().generateUUIDv1().toString();
    }

    protected boolean isUserPresent() {
        return agentComponentProvider.getCredentialsRequest().getUserAvailability().isUserPresent();
    }

    protected String getAccessTokenFromStorage() {
        return getTokenFromStorage().getAccessToken();
    }

    protected OAuth2Token getTokenFromStorage() {
        return persistentStorage.getToken();
    }

    protected ZonedDateTime getNow() {
        return agentComponentProvider.getLocalDateTimeSource().now().atZone(ZoneOffset.UTC);
    }

    private String getLanguageCode() {
        return PolishApiConstants.Localization.getLanguageCode(
                agentComponentProvider.getCredentialsRequest().getUser().getLocale());
    }
}
