package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.entities.useractivation.AgreementEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AgreementsResponse {
    private List<AgreementEntity> agreements;

    public List<AgreementEntity> getAgreements() {
        return agreements;
    }

    public void setAgreements(
            List<AgreementEntity> agreements) {
        this.agreements = agreements;
    }
}
