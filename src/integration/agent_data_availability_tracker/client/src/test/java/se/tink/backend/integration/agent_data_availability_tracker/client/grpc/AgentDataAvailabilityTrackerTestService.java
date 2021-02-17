package se.tink.backend.integration.agent_data_availability_tracker.client.grpc;

import io.grpc.stub.StreamObserver;
import java.util.concurrent.CompletableFuture;
import org.junit.Ignore;
import se.tink.backend.integration.agent_data_availability_tracker.api.AgentDataAvailabilityTrackerServiceGrpc.AgentDataAvailabilityTrackerServiceImplBase;
import se.tink.backend.integration.agent_data_availability_tracker.api.TrackAccountRequest;
import se.tink.backend.integration.agent_data_availability_tracker.api.Void;

@Ignore
public final class AgentDataAvailabilityTrackerTestService
        extends AgentDataAvailabilityTrackerServiceImplBase {

    private final CompletableFuture<String> future;

    AgentDataAvailabilityTrackerTestService(final CompletableFuture<String> future) {
        this.future = future;
    }

    @Override
    public StreamObserver<TrackAccountRequest> trackAccount(StreamObserver<Void> responseObserver) {
        return new StreamObserver<TrackAccountRequest>() {
            @Override
            public void onNext(final TrackAccountRequest trackAccountRequest) {
                future.complete(trackAccountRequest.getAgent());
            }

            @Override
            public void onError(final Throwable throwable) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void onCompleted() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
