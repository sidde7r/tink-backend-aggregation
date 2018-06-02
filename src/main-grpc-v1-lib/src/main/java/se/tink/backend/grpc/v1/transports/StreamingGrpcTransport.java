package se.tink.backend.grpc.v1.transports;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import java.util.Map;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.common.controllers.AnalyticsController;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.tracking.PersistingTracker;
import se.tink.backend.core.User;
import se.tink.backend.grpc.v1.errors.ApiError;
import se.tink.backend.grpc.v1.interceptors.AuthenticationInterceptor;
import se.tink.backend.grpc.v1.streaming.StreamingFirehoseMessageHandler;
import se.tink.backend.grpc.v1.streaming.StreamingQueueConsumerHandler;
import se.tink.backend.grpc.v1.streaming.StreamingResponseHandler;
import se.tink.backend.grpc.v1.streaming.context.ContextGenerator;
import se.tink.backend.main.controllers.CredentialServiceController;
import se.tink.backend.main.controllers.UserTrackerController;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.ProviderImageMap;
import se.tink.grpc.v1.rpc.StreamingRequest;
import se.tink.grpc.v1.rpc.StreamingResponse;
import se.tink.grpc.v1.services.StreamingServiceGrpc;
import se.tink.libraries.metrics.MetricRegistry;

public class StreamingGrpcTransport extends StreamingServiceGrpc.StreamingServiceImplBase {
    private static final String DEVICE_BY_USER_ID_PREFIX = "device-by-userId:";
    private static final LogUtils log = new LogUtils(StreamingGrpcTransport.class);

    private final StreamingQueueConsumerHandler queueConsumerHandler;
    private final ProviderImageMap providerImageMap;

    private final CredentialServiceController credentialServiceController;

    private final StreamingFirehoseMessageHandler firehoseMessageHandler;

    private final ContextGenerator contextGenerator;
    private final BiMap<String, String> categoryCodeById;

    private final CredentialsRepository credentialsRepository;
    private final ProviderDao providerDao;
    private final AnalyticsController analyticsController;
    private final MetricRegistry metricRegistry;
    private final UserTrackerController userTrackerController;

    @Inject
    StreamingGrpcTransport(CredentialServiceController credentialServiceController,
            StreamingQueueConsumerHandler queueConsumerHandler,
            StreamingFirehoseMessageHandler firehoseMessageHandler,
            ContextGenerator contextGenerator,
            @Named("categoryCodeById") BiMap<String, String> categoryCodeById,
            ProviderImageMap providerImageMap, CredentialsRepository credentialsRepository, ProviderDao providerDao,
            AnalyticsController analyticsController, MetricRegistry metricRegistry,
            UserTrackerController userTrackerController) {
        this.credentialServiceController = credentialServiceController;
        this.queueConsumerHandler = queueConsumerHandler;
        this.firehoseMessageHandler = firehoseMessageHandler;
        this.contextGenerator = contextGenerator;
        this.categoryCodeById = categoryCodeById;
        this.providerImageMap = providerImageMap;
        this.credentialsRepository = credentialsRepository;
        this.providerDao = providerDao;
        this.analyticsController = analyticsController;
        this.metricRegistry = metricRegistry;
        this.userTrackerController = userTrackerController;
    }

    @Override
    @Authenticated
    public StreamObserver<StreamingRequest> stream(StreamObserver<StreamingResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();

        final User user = authenticationContext.getUser();
        final String userId = user.getId();
        final String deviceId = getDeviceId(authenticationContext);

        removeQueueConsumerAndCloseStream(userId, deviceId);

        // Identify the user
        userTrackerController.identify(user, authenticationContext.getUserAgent().orElse(null),
                authenticationContext.getRemoteAddress());

        analyticsController.trackUserEvent(user, "user.stream.open", authenticationContext.getRemoteAddress(),
                ImmutableSet.of(PersistingTracker.class));

        ServerCallStreamObserver<StreamingResponse> openConnection = (ServerCallStreamObserver<StreamingResponse>) streamObserver;

        Map<String, se.tink.backend.core.Provider> providersByCredentialIds = credentialServiceController
                .getProvidersByCredentialIds(userId);

        StreamingResponseHandler streamingResponseHandler = new StreamingResponseHandler(user, openConnection,
                firehoseMessageHandler, providersByCredentialIds, categoryCodeById, deviceId, providerImageMap,
                credentialsRepository, providerDao, metricRegistry);

        try {
            queueConsumerHandler.register(streamingResponseHandler);

            // Generate first context
            StreamingResponse streamingContext = contextGenerator.generateContext(authenticationContext.getUser(),
                        providersByCredentialIds);

            streamingResponseHandler.sendContext(streamingContext);

            // Refresh credentials when the context is generated
            refreshCredentials(user);
        } catch (Throwable t) {
            queueConsumerHandler.remove(streamingResponseHandler);
            throw ApiError.Streaming.INTERNAL_ERROR.withCause(t).exception();
        }

        return new StreamObserver<StreamingRequest>() {
            /**
             * Clients use this method to keep the stream alive. Failure in the keep alive and refresh of credentials
             * will not fail the stream.
             */
            @Override
            public void onNext(StreamingRequest streamingRequest) {
                queueConsumerHandler.updateExpiration(userId, deviceId);

                keepCredentialsAlive(user);
                refreshCredentials(user);
            }

            /**
             * This error is received when for example the client cancels the stream. The queue consumer will then
             * be removed from the cache.
             */
            @Override
            public void onError(Throwable t) {
                queueConsumerHandler.remove(streamingResponseHandler);
            }

            @Override
            public void onCompleted() {
                streamObserver.onCompleted();
            }
        };
    }

    /**
     * Remove from memory any existing handlers for the same user + device. This also completes (closes) the stream
     * if it still is open.
     */
    private void removeQueueConsumerAndCloseStream(String userId, String deviceId) {
        queueConsumerHandler.remove(userId, deviceId).ifPresent(StreamingResponseHandler::complete);
    }

    private String getDeviceId(AuthenticationContext authenticationContext) {
        return authenticationContext.getUserDeviceId()
                .orElse(DEVICE_BY_USER_ID_PREFIX + authenticationContext.getUser().getId());
    }

    /**
     * Refresh credentials. Only log exceptions since we we don't want to stop the streaming in case of any errors.
     */
    private void refreshCredentials(User user) {
        try {
            credentialServiceController.refresh(user);
        } catch (Exception e) {
            log.error(user.getId(), "Could not refresh credentials.", e);
        }
    }

    /**
     * Keep Mobile BankID credentials alive. Only log exceptions since we we don't want to stop the streaming in
     * case of any errors.
     */
    private void keepCredentialsAlive(User user) {
        try {
            credentialServiceController.keepAllAlive(user);
        } catch (Exception e) {
            log.error(user.getId(), "Could not keep credentials alive.", e);
        }
    }
}
