package se.tink.backend.webhook.configuration;

import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.sun.jersey.api.client.Client;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import java.util.List;
import java.util.concurrent.TimeUnit;
import se.tink.backend.common.VersionInformation;
import se.tink.backend.firehose.v1.queue.FirehoseTopics;
import se.tink.backend.firehose.v1.rpc.FirehoseMessage;
import se.tink.backend.queue.QueueConsumer;
import se.tink.backend.queue.kafka.KafkaQueueConsumer;
import se.tink.backend.queue.kafka.KafkaQueueConsumerProperties;
import se.tink.backend.webhook.HealthResource;
import se.tink.backend.webhook.WebHookExecutor;
import se.tink.backend.webhook.WebhookHandler;
import se.tink.libraries.dropwizard.ObjectMapperFactory;
import se.tink.libraries.jersey.guice.JerseyResourceRegistrar;
import se.tink.libraries.metrics.HeapDumpGauge;
import se.tink.libraries.metrics.MetricCollector;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.PrometheusConfiguration;
import se.tink.libraries.metrics.PrometheusExportServer;
import se.tink.libraries.net.BasicJerseyClientFactory;

public class WebhookServiceModule extends AbstractModule {

    private final WebhookConfiguration configuration;
    private JerseyEnvironment jersey;

    WebhookServiceModule(WebhookConfiguration configuration, JerseyEnvironment jersey) {
        this.configuration = configuration;
        this.jersey = jersey;
    }

    @Override
    protected void configure() {
        bind(MetricRegistry.class).in(Scopes.SINGLETON);
        bind(VersionInformation.class).in(Scopes.SINGLETON);
        bind(MetricCollector.class).in(Scopes.SINGLETON);
        bind(PrometheusExportServer.class).in(Scopes.SINGLETON);
        bind(PrometheusConfiguration.class).toInstance(configuration.getPrometheus());
        bind(HeapDumpGauge.class).in(Scopes.SINGLETON);

        JerseyResourceRegistrar.build()
                .binder(binder())
                .jersey(jersey)
                .addResources(HealthResource.class)
                .bind();
    }

    @Provides
    @Singleton
    public QueueConsumer provideKafkaQueueConsumer(MetricRegistry metricRegistry, WebhookHandler queueConsumerHandler) {
        List<KafkaQueueConsumer.KafkaQueueConsumerSubscriber> subscribers = ImmutableList.of(
                new KafkaQueueConsumer.KafkaQueueConsumerSubscriber<>(FirehoseTopics.TRANSACTIONS,
                        FirehoseMessage.parser(), queueConsumerHandler),
                new KafkaQueueConsumer.KafkaQueueConsumerSubscriber<>(FirehoseTopics.SIGNABLE_OPERATIONS,
                        FirehoseMessage.parser(), queueConsumerHandler),
                new KafkaQueueConsumer.KafkaQueueConsumerSubscriber<>(FirehoseTopics.CREDENTIALS,
                        FirehoseMessage.parser(), queueConsumerHandler),
                new KafkaQueueConsumer.KafkaQueueConsumerSubscriber<>(FirehoseTopics.ACTIVITIES,
                        FirehoseMessage.parser(), queueConsumerHandler));

        KafkaQueueConsumerProperties properties = new KafkaQueueConsumerProperties();
        properties.setGroupId("webhook-group");
        properties.setHosts(configuration.getQueueHosts());

        // This could probably be removed later if we can poll multiple messages at once
        properties.setMaxPollRecords(1);

        return new KafkaQueueConsumer(metricRegistry, subscribers, properties);
    }

    @Provides
    @Singleton
    public WebHookExecutor provideWebhookExecutor(MetricRegistry metricRegistry) {
        Client client = new BasicJerseyClientFactory().createBasicClient(ObjectMapperFactory.createForApiUse());
        client.setFollowRedirects(false);

        return new WebHookExecutor(client, StopStrategies.stopAfterAttempt(10),
                WaitStrategies.fixedWait(7, TimeUnit.SECONDS), metricRegistry);
    }
}
