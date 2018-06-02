package se.tink.backend.grpc.v1.converter.authentication;

import org.junit.Test;
import se.tink.backend.core.auth.AuthenticationStatus;
import se.tink.backend.rpc.auth.AuthenticationResponse;
import se.tink.grpc.v1.rpc.SignedChallengeAuthenticationResponse;
import se.tink.libraries.uuid.UUIDUtils;
import static org.assertj.core.api.Assertions.assertThat;

public class SignedChallengeAuthenticationResponseConverterTest {
    @Test
    public void convertFromCoreToGrpc() {
        String authenticationToken = UUIDUtils.generateUUID();
        AuthenticationResponse coreResponse = new AuthenticationResponse();
        coreResponse.setAuthenticationToken(authenticationToken);
        coreResponse.setStatus(AuthenticationStatus.AUTHENTICATED);

        SignedChallengeAuthenticationResponse grpcResponse =
                new SignedChallengeAuthenticationResponseConverter().convertFrom(coreResponse);

        assertThat(grpcResponse.getAuthenticationToken()).isEqualTo(authenticationToken);
        assertThat(grpcResponse.getStatus()).isEqualTo(
                se.tink.grpc.v1.models.AuthenticationStatus.AUTHENTICATION_STATUS_AUTHENTICATED);
    }

    @Test
    public void convertFromCoreToGrpc_correctStatuses() {
        AuthenticationResponse coreResponse = new AuthenticationResponse();
        SignedChallengeAuthenticationResponse grpcResponse;
        SignedChallengeAuthenticationResponseConverter converter = new SignedChallengeAuthenticationResponseConverter();

        coreResponse.setStatus(AuthenticationStatus.AUTHENTICATED);
        grpcResponse = converter.convertFrom(coreResponse);
        assertThat(grpcResponse.getStatus()).isEqualTo(
                se.tink.grpc.v1.models.AuthenticationStatus.AUTHENTICATION_STATUS_AUTHENTICATED);

        coreResponse.setStatus(AuthenticationStatus.AUTHENTICATION_ERROR);
        grpcResponse = converter.convertFrom(coreResponse);
        assertThat(grpcResponse.getStatus()).isEqualTo(
                se.tink.grpc.v1.models.AuthenticationStatus.AUTHENTICATION_STATUS_AUTHENTICATION_ERROR);

        coreResponse.setStatus(AuthenticationStatus.USER_BLOCKED);
        grpcResponse = converter.convertFrom(coreResponse);
        assertThat(grpcResponse.getStatus()).isEqualTo(
                se.tink.grpc.v1.models.AuthenticationStatus.AUTHENTICATION_STATUS_USER_BLOCKED);

    }

    @Test
    public void convertFromCoreToGrpc_testDefaults() {
        AuthenticationResponse coreResponse = new AuthenticationResponse();
        SignedChallengeAuthenticationResponseConverter converter = new SignedChallengeAuthenticationResponseConverter();
        SignedChallengeAuthenticationResponse grpcResponse = converter.convertFrom(coreResponse);

        assertThat(grpcResponse.getStatus()).isEqualTo(
                se.tink.grpc.v1.models.AuthenticationStatus.AUTHENTICATION_STATUS_UNKNOWN);
        assertThat(grpcResponse.getAuthenticationToken()).isNullOrEmpty();
    }
}
