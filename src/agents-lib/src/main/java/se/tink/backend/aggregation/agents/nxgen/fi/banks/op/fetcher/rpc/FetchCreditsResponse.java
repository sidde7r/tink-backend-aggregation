package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities.OpBankCreditEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.rpc.OpBankResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchCreditsResponse extends OpBankResponseEntity {
    private List<OpBankCreditEntity> credits;

    public List<OpBankCreditEntity> getCredits() {
        return credits;
    }
}
