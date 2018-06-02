package se.tink.backend.connector.util.handler;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.connector.exception.RequestException;
import se.tink.backend.connector.exception.error.RequestError;
import se.tink.backend.connector.rpc.WebhookEntity;
import se.tink.backend.core.oauth2.OAuth2Client;
import se.tink.backend.core.oauth2.OAuth2WebHook;

public class WebhookHandler {

    private static final LogUtils log = new LogUtils(WebhookHandler.class);

    public void validateWebhookRequest(WebhookEntity webhookEntity, OAuth2Client client) throws RequestException {
        URL webhookUrl;

        try {
            webhookUrl = new URL(webhookEntity.getUrl());
        } catch (MalformedURLException e) {
            log.warn("The webhook url was incorrect: " + webhookEntity.getUrl(), e);
            throw RequestError.INVALID_URL.exception();
        }

        if (!Objects.equals(webhookUrl.getProtocol(), "https")) {
            throw RequestError.NOT_HTTPS.exception();
        }

        Optional<String> domainsProperty = client.getPayloadValue(OAuth2Client.PayloadKey.WEBHOOK_DOMAINS);
        if (!domainsProperty.isPresent()) {
            throw RequestError.UNREGISTERED_WEBHOOK_DOMAIN.exception();
        }

        Set<URL> validDomainUrls = Sets.newHashSet();
        for (String domain : ImmutableSet.copyOf(Splitter.on(',').trimResults().split(domainsProperty.get()))) {
            try {
                validDomainUrls.add(new URL(domain));
            } catch (MalformedURLException e) {
                log.error("We have somehow stored an invalid webhook domain in the database. Check this up.", e);
            }
        }

        boolean webhookHasValidDomain = validDomainUrls.stream()
                .anyMatch(d -> Objects.equals(d.getHost().toLowerCase(), webhookUrl.getHost().toLowerCase()));

        if (!webhookHasValidDomain) {
            throw RequestError.UNREGISTERED_WEBHOOK_DOMAIN.exception();
        }
    }

    public OAuth2WebHook mapToTinkModel(WebhookEntity webhookEntity, String webhookClientId) {
        OAuth2WebHook oAuth2WebHook = new OAuth2WebHook();

        // For now all webhooks we create for partners are global, meaning they concern all users.
        oAuth2WebHook.setGlobal(true);

        oAuth2WebHook.setSecret(webhookEntity.getSecret());
        oAuth2WebHook.setEvents(Sets.newHashSet(webhookEntity.getEvents()));
        oAuth2WebHook.setClientId(webhookClientId);
        oAuth2WebHook.setUrl(webhookEntity.getUrl());

        return oAuth2WebHook;
    }

    public void validateNoDuplicates(WebhookEntity entity, List<OAuth2WebHook> registeredWebhooks, String clientId)
            throws RequestException {

        if (registeredWebhooks == null || registeredWebhooks.isEmpty()) {
            return;
        }

        for (OAuth2WebHook registeredWebhook : registeredWebhooks) {
            if (isOverlap(entity, registeredWebhook, clientId)) {
                throw RequestError.WEBHOOK_OVERLAP.exception();
            }
        }
    }

    private boolean isOverlap(WebhookEntity webhookEntity, OAuth2WebHook registeredWebhook, String clientId) {
        boolean eventOverlap = webhookEntity.getEvents().stream()
                .anyMatch(e -> registeredWebhook.getEvents().contains(e));

        return Objects.equals(clientId, registeredWebhook.getClientId()) &&
                Objects.equals(webhookEntity.getUrl(), registeredWebhook.getUrl()) &&
                eventOverlap;
    }
}
