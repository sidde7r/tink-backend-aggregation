package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementalWaitRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;

public interface ThirdPartyAppRequestParamsProvider {

    ThirdPartyAppAuthenticationPayload getPayload();

    SupplementalWaitRequest getWaitingConfiguration();
}
