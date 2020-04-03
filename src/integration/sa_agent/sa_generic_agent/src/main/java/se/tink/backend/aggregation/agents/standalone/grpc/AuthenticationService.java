package se.tink.backend.aggregation.agents.standalone.grpc;

import io.grpc.ManagedChannel;
import se.tink.backend.aggregation.agents.standalone.GenericAgentConfiguration;
import se.tink.backend.aggregation.agents.standalone.GenericAgentConstants;
import se.tink.backend.aggregation.agents.standalone.entity.ConsentStatus;
import se.tink.backend.aggregation.agents.standalone.mapper.MappingContextKeys;
import se.tink.backend.aggregation.agents.standalone.mapper.auth.agg.ThirdPartyAppAuthenticationPayloadMapper;
import se.tink.backend.aggregation.agents.standalone.mapper.auth.sa.AuthenticationRequestMapper;
import se.tink.backend.aggregation.agents.standalone.mapper.factory.MappersController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.model.auth.AuthenticationRequest;
import se.tink.sa.model.auth.AuthenticationResponse;
import se.tink.sa.model.auth.GetConsentStatusRequest;
import se.tink.sa.model.auth.GetConsentStatusResponse;
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

    public ThirdPartyAppAuthenticationPayload login(
            SteppableAuthenticationRequest request, PersistentStorage persistentStorage) {
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

        persistentStorage.put(
                GenericAgentConstants.PersistentStorageKey.CONSENT_ID,
                response.getSecurityInfo().getConsentId());

        ThirdPartyAppAuthenticationPayloadMapper responseMapper =
                mappersController.thirdPartyAppAuthenticationPayloadMapper();
        ThirdPartyAppAuthenticationPayload payload = responseMapper.map(response);
        return payload;
    }

    public ConsentStatus getConsentStatus(GetConsentStatusRequest getConsentStatusRequest) {

        GetConsentStatusResponse response =
                progressiveAuthAgentServiceBlockingStub.getConsentStatus(getConsentStatusRequest);
        ConsentStatus consentStatus = tmpMap(response);

        return consentStatus;
    }

    // TODO: move this to mapper class
    private ConsentStatus tmpMap(GetConsentStatusResponse response) {
        String consentStatusString = "unknown state";
        try {
            consentStatusString = response.getTransactionStatus();
            return ConsentStatus.valueOf(consentStatusString);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("wrong transaction state=" + consentStatusString, e);
        }
    }
}
