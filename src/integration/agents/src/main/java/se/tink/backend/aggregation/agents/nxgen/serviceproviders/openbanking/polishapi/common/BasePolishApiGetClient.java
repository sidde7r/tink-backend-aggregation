package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.common;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderKeys.ACCEPT;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderKeys.ACCEPT_CHARSET;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderKeys.ACCEPT_ENCODING;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderKeys.ACCEPT_LANGUAGE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderKeys.CLIENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderKeys.COMPANY_CONTEXT;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderKeys.CONTENT_TYPE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderKeys.GetClient.PSU_IP_ADDRESS;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderKeys.GetClient.PSU_IP_PORT;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderKeys.GetClient.PSU_SESSION;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderKeys.GetClient.PSU_USER_AGENT;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderKeys.GetClient.TPP_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderKeys.GetClient.TPP_REQUEST_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderValues.ACCEPT_CHARSET_VAL;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderValues.ACCEPT_ENCODING_VAL;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderValues.GetClient.ACCEPT_VAL;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderValues.GetClient.CONTENT_TYPE_VAL;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderValues.GetClient.PSU_IP_PORT_VAL;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderValues.GetClient.PSU_USER_AGENT_VAL;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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
public class BasePolishApiGetClient {

    private final TinkHttpClient httpClient;
    protected final AgentConfiguration<PolishApiConfiguration> configuration;
    protected final AgentComponentProvider agentComponentProvider;
    protected final PolishApiPersistentStorage persistentStorage;

    @SneakyThrows
    protected RequestBuilder getRequestWithBaseHeaders(URL requestUrl, OAuth2Token token) {
        RequestBuilder header =
                httpClient
                        .request(requestUrl)
                        .header(ACCEPT, ACCEPT_VAL)
                        .header(ACCEPT_CHARSET, ACCEPT_CHARSET_VAL)
                        .header(ACCEPT_ENCODING, ACCEPT_ENCODING_VAL)
                        .header(ACCEPT_LANGUAGE, getLanguageCode())
                        .header(
                                CLIENT_ID,
                                configuration.getProviderSpecificConfiguration().getApiKey())
                        .header(CONTENT_TYPE, CONTENT_TYPE_VAL)
                        .header(COMPANY_CONTEXT, false)
                        .header(TPP_REQUEST_ID, getUuid())
                        .header(PSU_USER_AGENT, PSU_USER_AGENT_VAL)
                        .header(
                                PSU_IP_ADDRESS,
                                agentComponentProvider
                                        .getCredentialsRequest()
                                        .getUserAvailability()
                                        .getOriginatingUserIp())
                        .header(PSU_IP_PORT, PSU_IP_PORT_VAL)
                        .header(PSU_SESSION, isUserPresent())
                        .header(
                                TPP_ID,
                                CertificateUtils.getOrganizationIdentifier(
                                        configuration.getQsealc()));
        if (token != null) {
            header.addBearerToken(token);
        }
        return header;
    }

    private boolean isUserPresent() {
        return agentComponentProvider.getCredentialsRequest().getUserAvailability().isUserPresent();
    }

    protected OAuth2Token getTokenFromStorage() {
        return persistentStorage.getToken();
    }

    protected String getUuid() {
        return agentComponentProvider.getRandomValueGenerator().generateUUIDv1().toString();
    }

    protected ZonedDateTime getNow() {
        return agentComponentProvider.getLocalDateTimeSource().now().atZone(ZoneOffset.UTC);
    }

    private String getLanguageCode() {
        return PolishApiConstants.Localization.getLanguageCode(
                agentComponentProvider.getCredentialsRequest().getUser().getLocale());
    }
}
