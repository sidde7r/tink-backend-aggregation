package se.tink.aggregation.client;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.ExecutionException;
import se.tink.aggregation.grpc.CreateCredentialRequest;
import se.tink.aggregation.grpc.CreateCredentialResponse;
import se.tink.aggregation.grpc.Credential;
import se.tink.backend.core.Credentials;

public class CredentialGrpcClient {
    private static final int RETRIES = 5;
    private final Retryer<CreateCredentialResponse> retryer = RetryerBuilder.<CreateCredentialResponse>newBuilder()
            .retryIfExceptionOfType(StatusRuntimeException.class)
            .withStopStrategy(StopStrategies.stopAfterAttempt(RETRIES))
            .build();
    private final CredentialGrpcRouter router;

    CredentialGrpcClient(CredentialGrpcRouter router) {
        this.router = router;
    }

    public CreateCredentialResponse create(Credentials credentials) {
        CreateCredentialRequest request = CreateCredentialRequest.newBuilder()
                .setCredential(Credential.newBuilder().setId(credentials.getId()))
                .build();
        try {
            return retryer.call(()
                    -> router.pickServer().createCredential(request));
        } catch (ExecutionException | RetryException e) {
            throw new GrpcClientException(e);
        }
    }
}
