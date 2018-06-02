package se.tink.backend.connector.controller;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.common.config.ConnectorConfiguration;
import se.tink.backend.common.repository.mysql.main.OAuth2ClientRepository;
import se.tink.backend.common.repository.mysql.main.OAuth2WebHookRepository;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.connector.exception.RequestException;
import se.tink.backend.connector.exception.error.RequestError;
import se.tink.backend.connector.mapper.CoreWebhookMapper;
import se.tink.backend.connector.rpc.WebhookEntity;
import se.tink.backend.connector.util.handler.WebhookHandler;
import se.tink.backend.core.oauth2.OAuth2Client;
import se.tink.backend.core.oauth2.OAuth2WebHook;

public class ConnectorWebhookServiceController {

    private static final LogUtils log = new LogUtils(ConnectorWebhookServiceController.class);
    private final OAuth2ClientRepository clientRepository;
    private final String webhookClientId;
    private final WebhookHandler webhookHandler;
    private OAuth2WebHookRepository webHookRepository;

    @Inject
    public ConnectorWebhookServiceController(OAuth2WebHookRepository webHookRepository,
            OAuth2ClientRepository clientRepository, ConnectorConfiguration connectorConfiguration,
            WebhookHandler webhookHandler) {

        webhookClientId = connectorConfiguration.getWebhookClientId();
        Preconditions.checkArgument(!Strings.isNullOrEmpty(webhookClientId), "Config must have webhook client id set.");

        this.webHookRepository = webHookRepository;
        this.clientRepository = clientRepository;
        this.webhookHandler = webhookHandler;
    }

    public void createGlobalWebhook(WebhookEntity webhookEntity) throws RequestException {
        OAuth2Client client = clientRepository.findOne(webhookClientId);
        Preconditions.checkNotNull(client);

        webhookHandler.validateWebhookRequest(webhookEntity, client);
        webhookHandler.validateNoDuplicates(webhookEntity, webHookRepository.findByClientId(webhookClientId),
                webhookClientId);

        OAuth2WebHook oAuth2WebHook = webhookHandler.mapToTinkModel(webhookEntity, webhookClientId);

        webHookRepository.save(oAuth2WebHook);
        log.info("Saved new webhook for url: " + oAuth2WebHook.getUrl());
    }

    public List<WebhookEntity> getWebhooks() {
        OAuth2Client client = clientRepository.findOne(webhookClientId);

        if (client == null) {
            return Lists.newArrayList();
        }

        // Only returning global webhooks here since that's all we have allowed for now.
        List<OAuth2WebHook> webhooks = webHookRepository.findByClientIdAndGlobal(webhookClientId, true);

        return webhooks == null ?
                Lists.newArrayList() :
                webhooks.stream().map(CoreWebhookMapper::fromCoreToConnector).collect(Collectors.toList());
    }

    public void deleteWebhook(String id) throws RequestException {
        OAuth2WebHook webhook = webHookRepository.findOne(id);

        if (webhook == null) {
            throw RequestError.WEBHOOK_NOT_FOUND.exception();
        }

        webHookRepository.delete(id);
    }
}
