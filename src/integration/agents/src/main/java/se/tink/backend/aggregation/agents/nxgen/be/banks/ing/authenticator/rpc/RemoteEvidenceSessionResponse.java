package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc;

import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.SrpServerEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class RemoteEvidenceSessionResponse {

    private SrpServerEntity srp;

    private String extra;
}
