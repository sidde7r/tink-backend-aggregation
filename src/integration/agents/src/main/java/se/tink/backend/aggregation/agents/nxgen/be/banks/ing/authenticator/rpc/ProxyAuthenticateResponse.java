package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.AuthenticateResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.rpc.ProxyResponseMessage;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProxyAuthenticateResponse extends ProxyResponseMessage<AuthenticateResponseEntity> {}
