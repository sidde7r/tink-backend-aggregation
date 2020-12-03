package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.legacy;

import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.ChallengesValue;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.BusinessMessageResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChallengeResponse extends BusinessMessageResponse<ChallengesValue> {}
