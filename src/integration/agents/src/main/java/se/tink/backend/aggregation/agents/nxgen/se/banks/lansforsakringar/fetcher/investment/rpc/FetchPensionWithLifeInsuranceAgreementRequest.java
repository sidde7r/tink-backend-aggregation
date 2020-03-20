package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities.ProfileEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities.ResponseControlEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchPensionWithLifeInsuranceAgreementRequest {
    private String customerId;
    private ResponseControlEntity responseControl;
    private String id;

    private FetchPensionWithLifeInsuranceAgreementRequest(
            String customerId, ResponseControlEntity responseControl, String id) {
        this.customerId = customerId;
        this.responseControl = responseControl;
        this.id = id;
    }

    public static FetchPensionWithLifeInsuranceAgreementRequest of(
            String customerId, String profileType, String id) {
        ResponseControlEntity responseControlEntity =
                new ResponseControlEntity(new ProfileEntity(profileType));
        return new FetchPensionWithLifeInsuranceAgreementRequest(
                customerId, responseControlEntity, id);
    }
}
