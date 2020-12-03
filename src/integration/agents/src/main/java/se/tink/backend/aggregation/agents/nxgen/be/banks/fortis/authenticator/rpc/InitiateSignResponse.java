package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.InitiateSignValueEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitiateSignResponse extends BusinessMessageResponse<InitiateSignValueEntity> {}
