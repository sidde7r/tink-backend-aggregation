package se.tink.backend.aggregation.agents.standalone.grpc;

import io.grpc.ManagedChannel;
import java.util.*;
import se.tink.backend.aggregation.agents.standalone.GenericAgentConfiguration;
import se.tink.backend.aggregation.agents.standalone.mapper.MappingContextKeys;
import se.tink.backend.aggregation.agents.standalone.mapper.auth.agg.ThirdPartyAppAuthenticationPayloadMapper;
import se.tink.backend.aggregation.agents.standalone.mapper.auth.sa.AuthenticationRequestMapper;
import se.tink.backend.aggregation.agents.standalone.mapper.factory.MappersController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.model.auth.AuthenticationRequest;
import se.tink.sa.model.auth.AuthenticationResponse;
import se.tink.sa.services.auth.ProgressiveAuthAgentServiceGrpc;

public class AuthenticationService {
    private final ProgressiveAuthAgentServiceGrpc.ProgressiveAuthAgentServiceBlockingStub
            progressiveAuthAgentServiceBlockingStub;

    private final GenericAgentConfiguration configuration;
    private final StrongAuthenticationState strongAuthenticationState;
    private final MappersController mappersController;

    public AuthenticationService(
            final ManagedChannel channel,
            StrongAuthenticationState strongAuthenticationState,
            GenericAgentConfiguration configuration,
            MappersController mappersController) {

        progressiveAuthAgentServiceBlockingStub =
                ProgressiveAuthAgentServiceGrpc.newBlockingStub(channel);
        this.configuration = configuration;
        this.strongAuthenticationState = strongAuthenticationState;
        this.mappersController = mappersController;
    }

    public ThirdPartyAppAuthenticationPayload login(SteppableAuthenticationRequest request) {
        AuthenticationRequestMapper authenticationRequestMapper =
                mappersController.authenticationRequestMapper();
        MappingContext mappingContext =
                MappingContext.newInstance()
                        .put(MappingContextKeys.PROVIDE_STATE_FLAG, true)
                        .put(MappingContextKeys.STATE, strongAuthenticationState.getState());
        AuthenticationRequest authenticationRequest =
                authenticationRequestMapper.map(request, mappingContext);
        AuthenticationResponse response =
                progressiveAuthAgentServiceBlockingStub.login(authenticationRequest);

        ThirdPartyAppAuthenticationPayloadMapper responseMapper =
                mappersController.thirdPartyAppAuthenticationPayloadMapper();
        ThirdPartyAppAuthenticationPayload payload = responseMapper.map(response);
        return payload;
    }
}
