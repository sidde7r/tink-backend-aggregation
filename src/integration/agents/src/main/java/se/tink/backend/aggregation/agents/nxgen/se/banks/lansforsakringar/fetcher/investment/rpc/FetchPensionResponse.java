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

    //Todo: add log for missing ips pension
}
