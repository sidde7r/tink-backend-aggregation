package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.entities.PendingAuthCodeResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc.StandardResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class InitBankIdResponse extends StandardResponse {
    private PendingAuthCodeResponseEntity pendingAuthCodeResponse;
}
