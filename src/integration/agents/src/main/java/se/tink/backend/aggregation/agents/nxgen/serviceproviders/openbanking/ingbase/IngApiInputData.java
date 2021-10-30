package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase;

import lombok.Builder;
import lombok.Value;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.libraries.credentials.service.CredentialsRequest;

@Builder
@Value
public class IngApiInputData {

    IngUserAuthenticationData userAuthenticationData;
    StrongAuthenticationState strongAuthenticationState;
    CredentialsRequest credentialsRequest;
}
