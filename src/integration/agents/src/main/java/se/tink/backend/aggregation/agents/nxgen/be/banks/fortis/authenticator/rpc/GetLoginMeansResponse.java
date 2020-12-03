package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.GetLoginMeansValueEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetLoginMeansResponse extends BusinessMessageResponse<GetLoginMeansValueEntity> {}
