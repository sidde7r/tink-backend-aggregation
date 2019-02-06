package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.entity.AuthenticationResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.entity.BackendUserResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GrantingTicketResponse {
    private BackendUserResponseEntity backendUserResponse;
    private AuthenticationResponseEntity authenticationResponse;
}
