package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.entities.useractivation.AgreementEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AgreementsResponse {
    private List<AgreementEntity> agreements;

    public List<AgreementEntity> getAgreements() {
        return agreements;
    }
}
