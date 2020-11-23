package se.tink.backend.aggregation.agents.agentplatform.authentication;

import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementalWaitRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;

public interface ThirdPartyRequest {

    ThirdPartyAppAuthenticationPayload getPayload();

    SupplementalWaitRequest getSupplementalWaitRequest();
}
