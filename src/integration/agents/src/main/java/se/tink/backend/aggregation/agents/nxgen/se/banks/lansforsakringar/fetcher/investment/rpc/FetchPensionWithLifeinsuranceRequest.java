package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities.ProfileEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities.ResponseControlEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchPensionWithLifeinsuranceRequest {
    private String customerId;
    private String requestedView;
    private ResponseControlEntity responseControl;

    private FetchPensionWithLifeinsuranceRequest(
            String customerId, String requestedView, ResponseControlEntity responseControl) {
        this.customerId = customerId;
        this.requestedView = requestedView;
        this.responseControl = responseControl;
    }

    public static FetchPensionWithLifeinsuranceRequest of(
            String customerId, String requestedView, String profileType) {
        ResponseControlEntity responseControlEntity =
                new ResponseControlEntity(new ProfileEntity(profileType));
        return new FetchPensionWithLifeinsuranceRequest(
                customerId, requestedView, responseControlEntity);
    }
}
