package se.tink.backend.common.tasks.nsq;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.Futures;
import com.sproutsocial.nsq.Publisher;
import java.util.concurrent.Future;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.xerial.snappy.Snappy;
import se.tink.backend.common.config.TasksQueueConfiguration;
import se.tink.backend.common.tasks.interfaces.TaskSubmitter;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.system.tasks.Task;
import se.tink.libraries.metrics.Histogram;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class NSQTaskSubmitter implements TaskSubmitter {
    private static final LogUtils log = new LogUtils(NSQTaskSubmitter.class);

    private final Histogram recordSizeHistogram;
    private Publisher publisher;

    @Inject
    public NSQTaskSubmitter(MetricRegistry metricRegistry, TasksQueueConfiguration queueConfiguration) {
        this.recordSizeHistogram = metricRegistry.histogram(MetricId.newId("nsq_record_size"));
    }

    @Override
    public Future<Void> submit(Task<?> task) {
        byte[] bytePayload;

        try {
            bytePayload = Snappy.compress(SerializationUtils.serializeToBinary(task));
            recordSizeHistogram.update(bytePayload.length);
        } catch (Exception e) {
            log.error("Could not serialize task", e);
            return Futures.immediateFailedFuture(e);
        }

        Stopwatch watch = Stopwatch.createStarted();
        try {

            publisher.publish(task.getTopic(), bytePayload);

        } finally {
            log.info("Submission to NSQ took: " + watch);
        }

        return Futures.immediateFuture(null);
    }

    @PostConstruct
    public void start() throws Exception {
        publisher = new Publisher("localhost:4150");
    }

    @PreDestroy
    public void stop() {
        publisher.stop();
    }
}
