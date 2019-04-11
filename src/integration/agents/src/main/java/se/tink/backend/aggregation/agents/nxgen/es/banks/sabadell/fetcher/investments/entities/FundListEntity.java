package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FundListEntity {
    private List<FundModelListEntity> fundModelList;
}
