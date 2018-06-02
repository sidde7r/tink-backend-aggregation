package se.tink.backend.grpc.v1.streaming;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.BiMap;
import com.google.common.collect.Queues;
import io.grpc.stub.ServerCallStreamObserver;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.core.Provider;
import se.tink.backend.core.User;
import se.tink.backend.firehose.v1.rpc.FirehoseMessage;
import se.tink.backend.grpc.v1.converter.streaming.StreamingConverters;
import se.tink.backend.queue.QueueConsumer;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.ProviderImageMap;
import se.tink.grpc.v1.rpc.StreamingResponse;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;
import se.tink.libraries.uuid.UUIDUtils;

public class StreamingResponseHandler implements QueueConsumer.QueueConsumerHandler<FirehoseMessage> {
    private static final LogUtils log = new LogUtils(StreamingResponseHandler.class);
    private final String id;
    private final String userId;
    private final String deviceId;
    private final ServerCallStreamObserver<StreamingResponse> responseObserver;
    private final StreamingFirehoseMessageHandler firehoseMessageHandler;
    private final StreamingConverters userConverters;
    private final String logFormat;
    private final Timer messageDelayTimer;

    private final AtomicBoolean contextWasSent = new AtomicBoolean(false);
    private final ConcurrentLinkedQueue<StreamingResponse> streamingResponseQueue = Queues.newConcurrentLinkedQueue();

    public StreamingResponseHandler(User user, ServerCallStreamObserver<StreamingResponse> responseObserver,
            StreamingFirehoseMessageHandler firehoseMessageHandler,
            Map<String, Provider> providersByCredentialIds,
            BiMap<String, String> categoryCodeById, String deviceId, ProviderImageMap providerImageMap,
            CredentialsRepository credentialsRepository, ProviderDao providerDao, MetricRegistry metricRegistry) {
        this.id = UUIDUtils.toTinkUUID(UUID.randomUUID());
        this.userId = user.getId();
        this.responseObserver = responseObserver;
        this.firehoseMessageHandler = firehoseMessageHandler;
        this.userConverters = new StreamingConverters(categoryCodeById, providersByCredentialIds,
                user.getProfile().getCurrency(), providerImageMap, credentialsRepository, providerDao);
        this.deviceId = deviceId;
        this.messageDelayTimer = metricRegistry.timer(MetricId.newId("streaming_queue_message_delay"));

        this.logFormat = "[userId:" + userId + " deviceId:" + deviceId + " streamingHandlerId:" + id + " %s] : ";
    }

    @Override
    public void handle(FirehoseMessage message, Instant timestamp) {
        try {
            firehoseMessageHandler.handle(message, userConverters).forEach(this::sendFirehoseMessage);

            // Duration from when the message was created until it was send to clients.
            messageDelayTimer.update(Instant.now().toEpochMilli() - timestamp.toEpochMilli(), TimeUnit.MILLISECONDS);
        } catch (Throwable e) {
            log.error(String.format(logFormat, "Error while handle firehose message."), e);
            onError(e);
        }
    }

    @VisibleForTesting
    void sendFirehoseMessage(StreamingResponse streamingResponse) {
        if (!contextWasSent.get()) {
            streamingResponseQueue.add(streamingResponse);
            return;
        }

        if (!streamingResponseQueue.isEmpty()) {
            streamingResponseQueue.add(streamingResponse);
            sendQueuedMessages();
        } else {
            onNext(streamingResponse);
        }
    }

    public void sendContext(StreamingResponse response) {
        onNext(response);
        sendQueuedMessages();
        contextWasSent.set(true);
    }

    public void onError(Throwable throwable) {
        if (!isCancelled()) {
            responseObserver.onError(throwable);
            responseObserver.onCompleted();
            log.error(String.format(logFormat, "Error on streaming request."));
        } else {
            log.warn(String.format(logFormat, "Cannot send error for streaming for it was cancelled."));
        }
    }

    /**
     * Do not remove synchronized.
     * Messages coming from Kafka are executed on different threads.
     * The response objects were overlapping through the wire, resulting in a invalid response object in the client.
     * Synchronizing here should lock the message sending per device!
     */
    private synchronized void onNext(StreamingResponse response) {
        if (!responseObserver.isCancelled()) {
            try {
                responseObserver.onNext(response);
            } catch (IllegalStateException e) {
                log.warn("Cannot sent streaming message for it was cancelled.", e);
            }
        } else {
            log.warn(String.format(logFormat, "Cannot sent streaming message for it was cancelled."));
        }
    }

    private void sendQueuedMessages() {
        while (!streamingResponseQueue.isEmpty()) {
            Optional.ofNullable(streamingResponseQueue.poll()).ifPresent(this::onNext);
        }
    }

    public boolean complete() {
        if (!isCancelled()) {
            responseObserver.onCompleted();
            return true;
        }
        log.warn(String.format(logFormat, "Cannot complete streaming for it was cancelled."));
        return false;
    }

    public boolean isCancelled() {
        return responseObserver.isCancelled();
    }

    public String getUserId() {
        return userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getId(){
        return id;
    }

    public void sendKeepAliveMessage() {
        sendFirehoseMessage(StreamingResponse.newBuilder().setType(StreamingResponse.Type.UPDATE).build());
    }
}
