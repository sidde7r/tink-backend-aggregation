package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities.SessionBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SessionInfoResponse extends BaseResponse<SessionBodyEntity> {
}
