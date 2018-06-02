package se.tink.backend.webhook;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.common.providers.OAuth2ClientProvider;
import se.tink.backend.common.repository.mysql.main.OAuth2WebHookRepository;
import se.tink.backend.core.oauth2.OAuth2AuthorizationScopeTypes;
import se.tink.backend.core.oauth2.OAuth2Client;
import se.tink.backend.core.oauth2.OAuth2WebHook;
import se.tink.backend.core.oauth2.OAuth2WebHookEvent;
import se.tink.backend.firehose.v1.rpc.FirehoseMessage;
import se.tink.backend.queue.QueueConsumer;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.webhook.rpc.WebHookRequest;
import se.tink.backend.webhook.rpc.WebhookActivity;
import se.tink.backend.webhook.rpc.WebhookCredentials;
import se.tink.backend.webhook.rpc.WebhookSignableOperation;
import se.tink.backend.webhook.rpc.WebhookTransaction;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

public class WebhookHandler implements QueueConsumer.QueueConsumerHandler<FirehoseMessage> {

    private static final MetricId WEBHOOK = MetricId.newId("webhook_handler");
    private static final MetricId.MetricLabels RECEIVED_MESSAGE_LABEL = new MetricId.MetricLabels()
            .add("event", "received_message");
    private static final MetricId.MetricLabels RECEIVED_EXECUTABLE_MESSAGE_LABEL = new MetricId.MetricLabels()
            .add("event", "received_executable_message");
    private static final MetricId.MetricLabels FAILED_MAPPING_LABEL = new MetricId.MetricLabels()
            .add("event", "failed_mapping");

    private final OAuth2WebHookRepository webHookRepository;
    private final OAuth2ClientProvider oAuth2ClientProvider;
    private final WebHookExecutor executor;
    private final Counter receivedMessageCounter;
    private final Counter receivedExecutableMessageCounter;
    private static final LogUtils log = new LogUtils(WebhookHandler.class);

    @Inject
    public WebhookHandler(OAuth2WebHookRepository webHookRepository, OAuth2ClientProvider oAuth2ClientProvider,
            WebHookExecutor executor, MetricRegistry metricRegistry) {
        this.webHookRepository = webHookRepository;
        this.oAuth2ClientProvider = oAuth2ClientProvider;
        this.executor = executor;

        receivedMessageCounter = metricRegistry.meter(WEBHOOK.label(RECEIVED_MESSAGE_LABEL));
        receivedExecutableMessageCounter = metricRegistry.meter(WEBHOOK.label(RECEIVED_EXECUTABLE_MESSAGE_LABEL));
    }

    @Override
    public void handle(FirehoseMessage message, Instant timestamp) {
        receivedMessageCounter.inc();

        List<String> webhookEventTypes = getWebhookEventTypes(message);

        if (webhookEventTypes.isEmpty()) {
            // Not a message we should execute a webhook for.
            return;
        }

        receivedExecutableMessageCounter.inc();

        Map<String, OAuth2Client> clientsById = oAuth2ClientProvider.get();

        for (String clientId : clientsById.keySet()) {

            OAuth2Client oauth2Client = clientsById.get(clientId);

            if (!oauth2Client.getOAuth2Scope().isRequestedScopeValid(OAuth2AuthorizationScopeTypes.USER_WEB_HOOKS)) {
                // Not an oauth2 client that may execute webhooks.
                continue;
            }

            List<OAuth2WebHook> webhooks = webHookRepository.findByUserIdAndClientId(message.getUserId(), clientId);
            List<OAuth2WebHook> globalWebhooks = webHookRepository.findByClientIdAndGlobal(clientId, true);

            if (globalWebhooks != null) {
                webhooks.addAll(globalWebhooks);
            }

            for (OAuth2WebHook webhook : webhooks) {

                for (String eventType : webhookEventTypes) {
                    if (webhook.getEvents().contains(eventType)) {
                        List<Object> objects = getObjects(message, eventType);

                        for (Object object : objects) {
                            executor.execute(new WebHookRequest(webhook, eventType, object));
                        }
                    }
                }
            }
        }
    }

    private List<Object> getObjects(FirehoseMessage message, String event) {
        switch (event) {
        case OAuth2WebHookEvent.ACTIVITY_UPDATE:
            return message.getActivitiesList().stream().map(WebhookActivity::fromFirehoseActivity).collect(
                    Collectors.toList());
        case OAuth2WebHookEvent.TRANSACTION_CREATE:
        case OAuth2WebHookEvent.TRANSACTION_UPDATE:
            return message.getTransactionsList().stream().map(WebhookTransaction::fromFirehoseTransaction)
                    .collect(Collectors.toList());
        case OAuth2WebHookEvent.SIGNABLE_OPERATION_UPDATE:
            return message.getSignableOperationsList().stream()
                    .map(WebhookSignableOperation::fromFirehoseSignableOperation).collect(Collectors.toList());
        case OAuth2WebHookEvent.CREDENTIALS_UPDATE:
        case OAuth2WebHookEvent.CREDENTIALS_CREATE:
            return message.getCredentialsList().stream().map(WebhookCredentials::fromFirehoseCredentials)
                    .collect(Collectors.toList());
        default:
            throw new IllegalArgumentException("Unknown webhook event type: " + event);
        }
    }

    private List<String> getWebhookEventTypes(FirehoseMessage message) {
        switch (message.getType()) {
        case CREATE:
            return getWebhookEventTypesForCreate(message);
        case UPDATE:
            return getWebhookEventTypesForUpdate(message);
        case DELETE:
            return Lists.newArrayList();
        default:
            throw new IllegalArgumentException("Not implemented yet!");
        }
    }

    private List<String> getWebhookEventTypesForCreate(FirehoseMessage message) {
        List<String> events = Lists.newArrayListWithCapacity(2);

        if (message.getCredentialsCount() > 0) {
            events.add(OAuth2WebHookEvent.CREDENTIALS_CREATE);
        }

        if (message.getTransactionsCount() > 0) {
            events.add(OAuth2WebHookEvent.TRANSACTION_CREATE);
        }

        return events;
    }

    private List<String> getWebhookEventTypesForUpdate(FirehoseMessage message) {
        List<String> events = Lists.newArrayListWithCapacity(4);

        if (message.getTransactionsCount() > 0) {
            events.add(OAuth2WebHookEvent.TRANSACTION_UPDATE);
        }

        if (message.getActivitiesCount() > 0) {
            events.add(OAuth2WebHookEvent.ACTIVITY_UPDATE);
        }

        if (message.getSignableOperationsCount() > 0) {
            switch (message.getSignableOperationsList().get(0).getStatus()) {
            case STATUS_CANCELLED:
            case STATUS_FAILED:
            case STATUS_EXECUTED:
                events.add(OAuth2WebHookEvent.SIGNABLE_OPERATION_UPDATE);
                break;
            case UNRECOGNIZED:
                log.warn(message.getUserId(),
                        "Received webhook with unknown signable operation type. Check up on this!");
                break;
            case STATUS_CREATED:
            default:
                // Do nothing.
            }
        }

        if (message.getCredentialsCount() > 0) {
            events.add(OAuth2WebHookEvent.CREDENTIALS_UPDATE);
        }

        return events;
    }
}
