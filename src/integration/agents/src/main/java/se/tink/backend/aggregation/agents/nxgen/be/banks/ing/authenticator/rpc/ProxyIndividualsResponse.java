package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.IndividualsResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.rpc.ProxyResponseMessage;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProxyIndividualsResponse extends ProxyResponseMessage<IndividualsResponseEntity> {}
