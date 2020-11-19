package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import lombok.Setter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.entities.LoanDetailEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.rpc.AbstractResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanDetailsResponse extends AbstractResponse {
    @Setter private LoanDetailEntity loanDetail;
    private String loanNumber;
    private String realEstateNumber;
    private String lastUpdated;

    public LoanDetailEntity getLoanDetail() {
        return loanDetail;
    }
}
