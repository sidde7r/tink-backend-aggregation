package se.tink.backend.common.tasks.nsq;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.sproutsocial.nsq.Message;
import com.sproutsocial.nsq.MessageHandler;
import com.sproutsocial.nsq.Subscriber;
import io.dropwizard.lifecycle.Managed;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import se.tink.backend.common.config.TasksQueueConfiguration;
import se.tink.backend.common.tasks.interfaces.GenericTaskHandler;
import se.tink.libraries.metrics.MetricRegistry;

public class NSQConsumer implements Managed {
    private static final Splitter COMMA_SPLITTER = Splitter.on(",").omitEmptyStrings();

    private final TasksQueueConfiguration configuration;
    private final ImmutableList<String> topics;
    private final MetricRegistry metricRegistry;
    private final GenericTaskHandler messageHandler;
    private Subscriber subscriber;

    public NSQConsumer(TasksQueueConfiguration queueConfiguration, List<String> topics,
            GenericTaskHandler messageHandler, MetricRegistry metricRegistry) {

        this.configuration = queueConfiguration;
        this.topics = ImmutableList.copyOf(topics);
        this.messageHandler = messageHandler;
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void start() throws Exception {
        // TODO: Allow configuration to return a list.
        List<String> hosts = StreamSupport.stream(COMMA_SPLITTER.split(configuration.getHosts()).spliterator(), false)
                .map(s -> Splitter.on(":").omitEmptyStrings().split(s).iterator().next())
                .map(s -> s.contains(":") ? s : s + ":4161")
                .collect(Collectors.toList());
        subscriber = new Subscriber(hosts.toArray(new String[hosts.size()]));

        topics.forEach(topic -> subscriber.subscribe(topic, "to-process", new MessageHandler() {
            @Override
            public void accept(Message message) {
                try {
                    messageHandler.handle(message.getData());
                } catch (Exception e) {
                    message.requeue();
                    return;
                }
                message.finish();
            }
        }));
    }

    @Override
    public void stop() throws Exception {
        subscriber.stop();
    }
}
