package se.tink.backend.connector.api;

import se.tink.backend.connector.exception.RequestException;
import se.tink.backend.connector.rpc.WebhookEntity;
import se.tink.backend.connector.rpc.WebhookListEntity;

public interface ConnectorWebhookService {

    void createWebhook(WebhookEntity webhookEntity) throws RequestException;

    WebhookListEntity getWebhooks();

    void deleteWebhook(String id) throws RequestException;
}
