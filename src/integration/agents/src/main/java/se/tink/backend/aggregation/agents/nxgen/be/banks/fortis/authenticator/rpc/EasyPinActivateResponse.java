package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.EasyPinActivateValueEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EasyPinActivateResponse extends BusinessMessageResponse<EasyPinActivateValueEntity> {}
