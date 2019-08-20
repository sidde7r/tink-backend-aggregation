package se.tink.backend.aggregation.nxgen.controllers.authentication;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
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

    private final ImmutableList<Field> fields;
    private ThirdPartyAppAuthenticationPayload payload;
    private SupplementalWaitRequest supplementalWaitRequest;

    public AuthenticationResponse(@Nonnull List<Field> fields) {
        this.fields = ImmutableList.copyOf(fields);
    }

    public static AuthenticationResponse openThirdPartyApp(
            final ThirdPartyAppAuthenticationPayload payload) {
        final AuthenticationResponse response = new AuthenticationResponse(Collections.emptyList());
        response.payload = payload;
        return response;
    }

    public static AuthenticationResponse requestWaitingForSupplementalInformation(
            final SupplementalWaitRequest supplementalWaitRequest) {
        final AuthenticationResponse response = new AuthenticationResponse(Collections.emptyList());
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
