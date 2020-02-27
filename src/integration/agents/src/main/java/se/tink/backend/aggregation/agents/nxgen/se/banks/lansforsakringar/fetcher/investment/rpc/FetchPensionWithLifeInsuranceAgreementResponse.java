package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities.AgreementResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchPensionWithLifeInsuranceAgreementResponse {
    private AgreementResponseEntity response;

    public AgreementResponseEntity getResponse() {
        return response;
    }
}
