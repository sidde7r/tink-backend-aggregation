package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc;

import lombok.Builder;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.ExtraEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.SrpClientEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@Builder
@JsonObject
public class RemoteEvidenceSessionRequest {

    private SrpClientEntity srp;

    private ExtraEntity extra;
}
