package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.CheckLoginResultValue;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CheckLoginResultResponse extends BusinessMessageResponse<CheckLoginResultValue> {}
