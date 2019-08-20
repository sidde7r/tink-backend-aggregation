package se.tink.backend.aggregation.nxgen.controllers.authentication;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;

/**
 * In progressive authentication, carry the intermediate step and fields. Yet to see if we need to
 * carry Credential object or any data in it.
 */
public final class AuthenticationResponse {

    private ImmutableList<Field> fields;
    private ThirdPartyAppAuthenticationPayload payload;
    private SupplementalWaitRequest supplementalWaitRequest;

    private AuthenticationResponse() {}

    public static AuthenticationResponse fromSupplementalFields(@Nonnull List<Field> fields) {
        final AuthenticationResponse response = new AuthenticationResponse();
        response.fields = ImmutableList.copyOf(fields);
        return response;
    }

    public static AuthenticationResponse openThirdPartyApp(
            final ThirdPartyAppAuthenticationPayload payload) {
        final AuthenticationResponse response = new AuthenticationResponse();
        response.payload = payload;
        return response;
    }

    public static AuthenticationResponse requestWaitingForSupplementalInformation(
            final SupplementalWaitRequest supplementalWaitRequest) {
        final AuthenticationResponse response = new AuthenticationResponse();
        response.supplementalWaitRequest = supplementalWaitRequest;
        return response;
    }

    public ImmutableList<Field> getFields() {
        return fields;
    }

    public Optional<ThirdPartyAppAuthenticationPayload> getThirdPartyAppPayload() {
        return Optional.ofNullable(payload);
    }

    public Optional<SupplementalWaitRequest> getSupplementalWaitRequest() {
        return Optional.ofNullable(supplementalWaitRequest);
    }
}
