package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc;

import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.entities.AgreementEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class AgreementsResponse {
    private List<AgreementEntity> agreements;
}
