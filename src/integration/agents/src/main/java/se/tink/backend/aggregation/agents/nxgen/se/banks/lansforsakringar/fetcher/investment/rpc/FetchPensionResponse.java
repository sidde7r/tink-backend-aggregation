package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities.FootNotesEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities.IpsPensionsResponseModelEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities.LivPensionsResponseModelEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchPensionResponse {
    private IpsPensionsResponseModelEntity ipsPensionsResponseModel;
    private LivPensionsResponseModelEntity livPensionsResponseModel;
    private FootNotesEntity footNotes;
    private double totalValue;

    public IpsPensionsResponseModelEntity getIpsPensionsResponseModel() {
        return ipsPensionsResponseModel;
    }

    public LivPensionsResponseModelEntity getLivPensionsResponseModel() {
        return livPensionsResponseModel;
    }

    public FootNotesEntity getFootNotes() {
        return footNotes;
    }

    public double getTotalValue() {
        return totalValue;
    }

}
